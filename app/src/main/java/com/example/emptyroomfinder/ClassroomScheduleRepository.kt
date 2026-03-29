package com.example.emptyroomfinder

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class ClassroomScheduleRepository(private val dao: ClassroomScheduleDao) {

    // 獲取所有課表資料，回傳 Flow 類型，讓 UI 可以即時觀察資料庫內容的變動。
    fun getAllSchedules(): Flow<List<ClassroomSchedule>> {
        return dao.getAll()
    }

    // --- 以下為基本的資料庫操作 (CRUD)，使用suspend確保在協程中執行 ---
    @WorkerThread
    suspend fun insert(schedule: ClassroomSchedule) {
        dao.insert(schedule)
    }

    @WorkerThread
    suspend fun insertAll(schedules: List<ClassroomSchedule>) {
        dao.insertAll(schedules)
    }

    @WorkerThread
    suspend fun update(schedule: ClassroomSchedule) {
        dao.update(schedule)
    }

    @WorkerThread
    suspend fun delete(schedule: ClassroomSchedule) {
        dao.delete(schedule)
    }

    // 根據名稱尋找特定教室（一次性查詢）
    @WorkerThread
    suspend fun findByName(name: String): ClassroomSchedule? {
        return dao.findByName(name)
    }

    // 核心功能：查詢特定大樓、樓層且該時段為空的教室
    suspend fun queryEmptyRooms(
        buildingName: String, // buildingName 大樓 (如 SF, ES)
        day: String, // day星期 (如 mon, tue)
        timeSlotKey: String, // timeSlotKey時段 (如 1, 2)
        floorQueryValue: String // floorQueryValue樓層 (如 1, 2, N)
    ): List<ClassroomSchedule> {
        // 組合資料庫查詢字串（例如：SF3% 代表SF大樓3樓的所有教室）
        val buildingAndFloorPrefix = "$buildingName$floorQueryValue%"
        val schedules = dao.getSchedulesByBuildingAndFloor(buildingAndFloorPrefix)

        // 對查詢結果進行篩選，找出該時段狀態為空閒的教室
        return schedules.filter { schedule ->
            val columnKey = "${day.lowercase()}$timeSlotKey"
            val scheduleStatus = getScheduleStatus(schedule, columnKey)
            // CSV 中 "O" 代表空教室
            scheduleStatus == "O"
        }
    }

    // 輔助方法：根據傳入的Key(如 "mon1") 動態取得物件中對應的時段欄位值
    private fun getScheduleStatus(schedule: ClassroomSchedule, key: String): String? {
        return when (key) {
            "mon1" -> schedule.mon1; "mon2" -> schedule.mon2; "mon3" -> schedule.mon3; "mon4" -> schedule.mon4; "monn" -> schedule.monN; "mon5" -> schedule.mon5; "mon6" -> schedule.mon6; "mon7" -> schedule.mon7; "mon8" -> schedule.mon8;
            "tue1" -> schedule.tue1; "tue2" -> schedule.tue2; "tue3" -> schedule.tue3; "tue4" -> schedule.tue4; "tuen" -> schedule.tueN; "tue5" -> schedule.tue5; "tue6" -> schedule.tue6; "tue7" -> schedule.tue7; "tue8" -> schedule.tue8;
            "wed1" -> schedule.wed1; "wed2" -> schedule.wed2; "wed3" -> schedule.wed3; "wed4" -> schedule.wed4; "wedn" -> schedule.wedN; "wed5" -> schedule.wed5; "wed6" -> schedule.wed6; "wed7" -> schedule.wed7; "wed8" -> schedule.wed8;
            "thu1" -> schedule.thu1; "thu2" -> schedule.thu2; "thu3" -> schedule.thu3; "thu4" -> schedule.thu4; "thun" -> schedule.thuN; "thu5" -> schedule.thu5; "thu6" -> schedule.thu6; "thu7" -> schedule.thu7; "thu8" -> schedule.thu8;
            "fri1" -> schedule.fri1; "fri2" -> schedule.fri2; "fri3" -> schedule.fri3; "fri4" -> schedule.fri4; "frin" -> schedule.friN; "fri5" -> schedule.fri5; "fri6" -> schedule.fri6; "fri7" -> schedule.fri7; "fri8" -> schedule.fri8;
            else -> "occupied" // 若Key不匹配，視為已佔用
        }
    }

    // 獲取單一教室的詳細課表資訊（以 Flow 形式回傳，支援持續監測）
    fun getClassroomDetails(classRoomName: String): Flow<ClassroomSchedule?> {
        return dao.getClassroomDetails(classRoomName)
    }
}

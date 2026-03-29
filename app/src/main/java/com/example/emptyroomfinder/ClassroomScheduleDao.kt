package com.example.emptyroomfinder

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 資料存取物件 (DAO)
 * 定義對 ClassroomSchedule 資料表進行增刪查改 (CRUD) 的方法
 */
@Dao
interface ClassroomScheduleDao {

    /**
     * 取得所有課表資料，並按教室名稱升冪排序
     * 回傳 Flow 類型，當資料庫內容變動時，UI 會自動收到通知
     */
    @Query("SELECT * FROM classroom_schedules ORDER BY classroom ASC")
    fun getAll(): Flow<List<ClassroomSchedule>>

    /**
     * 插入單筆資料
     * 若資料已存在（主鍵衝突），則忽略此次插入動作
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(schedule: ClassroomSchedule)

    /**
     * 批次插入多筆資料
     * 若資料已存在（主鍵衝突），則直接覆蓋舊資料 (REPLACE)
     * 這在預填資料或同步 CSV 時非常實用
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<ClassroomSchedule>)

    /**
     * 更新現有的資料
     */
    @Update
    suspend fun update(schedule: ClassroomSchedule)

    /**
     * 刪除特定資料
     */
    @Delete
    suspend fun delete(schedule: ClassroomSchedule)

    /**
     * 根據大樓與樓層前綴進行模糊查詢
     *  buildingAndFloorPrefix 格式如： "SF3%"（代表聖言樓 3 樓的所有教室）。
     */
    @Query("SELECT * FROM classroom_schedules WHERE classroom LIKE :buildingAndFloorPrefix ORDER BY classroom ASC")
    suspend fun getSchedulesByBuildingAndFloor(buildingAndFloorPrefix: String): List<ClassroomSchedule>

    /**
     * 獲取單一教室的詳細課表資料
     * 使用 Flow 觀察，適合用於「教室詳細頁面」
     */
    @Query("SELECT * FROM classroom_schedules WHERE classroom = :classRoomName")
    fun getClassroomDetails(classRoomName: String): Flow<ClassroomSchedule?>

    /**
     * 根據教室名稱查找單一物件（一次性查詢）
     * LIMIT 1 確保只回傳第一筆匹配的結果
     */
    @Query("SELECT * FROM classroom_schedules WHERE classroom = :classroomName LIMIT 1")
    suspend fun findByName(classroomName: String): ClassroomSchedule?
}

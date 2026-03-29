package com.example.emptyroomfinder

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.savedstate.SavedStateRegistryOwner

// 建構函數：接收Repository實例讓ViewModel可以獲得資料，並接收SavedStateHandle以處理Activity重建時的資料恢復
class QueryResultViewModel(
    repository: ClassroomScheduleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 從Activity傳來的Intent Extras獲取建築物名稱
    private val selectedBuildingName = savedStateHandle.getLiveData<String>("building")

    // UI中Spinner的選擇狀態（星期、時間、樓層）
    val selectedDay = MutableLiveData<String>()
    val selectedTime = MutableLiveData<String>()
    val selectedFloor = MutableLiveData<String>()

    // 樓層清單LiveData：根據選定的建築物自動計算可選樓層
    val floors: LiveData<List<String>> = selectedBuildingName.switchMap { building ->
        repository.getAllSchedules().asLiveData().map { schedules ->
            // // 根據建築物名稱設定編號前綴（聖言樓為 SF，進修部大樓為 ES）
            val buildingPrefix = if (building == "聖言樓") "SF" else "ES"
            schedules.map { it.classroom }
                .filter { it.startsWith(buildingPrefix) } // 只留下該大樓(SF/ES)的教室
                .mapNotNull { name ->
                    // 提取樓層數字(ex. 從"SF130"提取"1")
                    val numberPart = name.removePrefix(buildingPrefix)
                    if (numberPart.isNotEmpty()) numberPart.substring(0, 1) else null
                }
                .distinct() // 去除重複樓層
                .sortedBy { it.toIntOrNull() ?: Int.MAX_VALUE } // 按樓層順序
                .map { "${it}樓" } // 轉換為顯示文字
                .let { listOf("全部樓層") + it } // 在清單最前面加上「全部樓層」選項
        }
    }

    // 最終的空教室列表LiveData：當任何篩選條件變更時，都會重新計算結果
    val emptyRooms: LiveData<List<ClassroomSchedule>> = object : MediatorLiveData<List<ClassroomSchedule>>() {
        private var allSchedules: List<ClassroomSchedule>? = null
        private var day: String? = null
        private var time: String? = null
        private var floor: String? = null
        private var building: String? = null

        init {
            // 監聽所有資料來源，只要任一條件改便，就呼叫"update()"
            addSource(repository.getAllSchedules().asLiveData()) { schedules -> allSchedules = schedules; update() }
            addSource(selectedBuildingName) { newBuilding -> building = newBuilding; update() }
            addSource(selectedDay) { newDay -> day = newDay; update() }
            addSource(selectedTime) { newTime -> time = newTime; update() }
            addSource(selectedFloor) { newFloor -> floor = newFloor; update() }
        }

        private fun update() {
            // 確保所有必要參數都已具備，才進行篩選
            val schedules = allSchedules ?: return
            val currentBuilding = building ?: return
            val currentDay = day ?: return
            val currentTime = time ?: return
            val currentFloor = floor ?: return

            val buildingPrefix = if (currentBuilding == "聖言樓") "SF" else "ES"
            // 取得對應的資料庫欄位代碼(ex Mon_1)
            val timeSlotColumn = getTimeSlotColumn(currentDay, currentTime) ?: return

            val filteredList = schedules.filter { schedule ->
                // 1.檢查大樓的前綴(SF/ES)
                val matchesBuilding = schedule.classroom.startsWith(buildingPrefix)
                // 2.檢查樓層(如果選擇全部則跳過)
                val floorNumber = if (currentFloor == "全部樓層") null else currentFloor.removeSuffix("樓")
                val matchesFloor = floorNumber?.let {
                    schedule.classroom.removePrefix(buildingPrefix).startsWith(it)
                } ?: true

                // 3.檢查該時段是否可用
                val isAvailable = isRoomAvailable(schedule, timeSlotColumn)
                matchesBuilding && matchesFloor && isAvailable
            }
            value = filteredList // 更新LiveData的值
        }
    }.distinctUntilChanged() // 只有當結果內容真的改變時才會通知觀察者，避免不必要的UI更新

    // 將中文的「星期」和「節次」轉換為資料室欄位名稱(ex. 星期一、第一節 -> Mon_1)
    private fun getTimeSlotColumn(day: String, time: String): String? {
        val dayCode = when (day) {
            "星期一" -> "mon"; "星期二" -> "tue"; "星期三" -> "wed"; "星期四" -> "thu"; "星期五" -> "fri"; else -> return null
        }
        val timeCode = when (time) {
            "第一節" -> "1"; "第二節" -> "2"; "第三節" -> "3"; "第四節" -> "4"; "中午" -> "N"; "第五節" -> "5"; "第六節" -> "6"; "第七節" -> "7"; "第八節" -> "8"; else -> return null
        }
        return dayCode + timeCode
    }

    //檢查特定時段欄位是否可用（"X" 代表該時段已被佔用，"O" 則代表是空教室）
    private fun isRoomAvailable(schedule: ClassroomSchedule, timeSlotColumn: String): Boolean {
        return when (timeSlotColumn) {
            "mon1" -> schedule.mon1 != "X"; "mon2" -> schedule.mon2 != "X"; "mon3" -> schedule.mon3 != "X"; "mon4" -> schedule.mon4 != "X"; "monN" -> schedule.monN != "X"; "mon5" -> schedule.mon5 != "X"; "mon6" -> schedule.mon6 != "X"; "mon7" -> schedule.mon7 != "X"; "mon8" -> schedule.mon8 != "X"
            "tue1" -> schedule.tue1 != "X"; "tue2" -> schedule.tue2 != "X"; "tue3" -> schedule.tue3 != "X"; "tue4" -> schedule.tue4 != "X"; "tueN" -> schedule.tueN != "X"; "tue5" -> schedule.tue5 != "X"; "tue6" -> schedule.tue6 != "X"; "tue7" -> schedule.tue7 != "X"; "tue8" -> schedule.tue8 != "X"
            "wed1" -> schedule.wed1 != "X"; "wed2" -> schedule.wed2 != "X"; "wed3" -> schedule.wed3 != "X"; "wed4" -> schedule.wed4 != "X"; "wedN" -> schedule.wedN != "X"; "wed5" -> schedule.wed5 != "X"; "wed6" -> schedule.wed6 != "X"; "wed7" -> schedule.wed7 != "X"; "wed8" -> schedule.wed8 != "X"
            "thu1" -> schedule.thu1 != "X"; "thu2" -> schedule.thu2 != "X"; "thu3" -> schedule.thu3 != "X"; "thu4" -> schedule.thu4 != "X"; "thuN" -> schedule.thuN != "X"; "thu5" -> schedule.thu5 != "X"; "thu6" -> schedule.thu6 != "X"; "thu7" -> schedule.thu7 != "X"; "thu8" -> schedule.thu8 != "X"
            "fri1" -> schedule.fri1 != "X"; "fri2" -> schedule.fri2 != "X"; "fri3" -> schedule.fri3 != "X"; "fri4" -> schedule.fri4 != "X"; "friN" -> schedule.friN != "X"; "fri5" -> schedule.fri5 != "X"; "fri6" -> schedule.fri6 != "X"; "fri7" -> schedule.fri7 != "X"; "fri8" -> schedule.fri8 != "X"
            else -> false
        }
    }
}

// ViewModelFactory：用來產生成有參數(Repository和SavedStateHandle)的ViewModel
class QueryResultViewModelFactory(
    private val repository: ClassroomScheduleRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(QueryResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QueryResultViewModel(repository, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

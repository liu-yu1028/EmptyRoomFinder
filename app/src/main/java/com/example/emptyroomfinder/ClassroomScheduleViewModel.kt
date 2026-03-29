package com.example.emptyroomfinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData // Flow 轉 LiveData 的關鍵
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow

// 建構函數：透過注入Repository來處理資料存取邏輯
class ClassroomScheduleViewModel(private val repository: ClassroomScheduleRepository): ViewModel() {

    // 暫存用的教室列表
    private val schedules: MutableList<ClassroomSchedule> = mutableListOf()

    // 內部使用的MutableLiveData，用來儲存查詢到的教室結果
    private val _schedulesLiveData = MutableLiveData<List<ClassroomSchedule>>()

    // 對外公開的LiveData，提供Activity或觀察 (Observe)，確保UI不會直接修改資料　
    val schedulesLiveData: LiveData<List<ClassroomSchedule>> = _schedulesLiveData

    // 觸發「空教室查詢」的功能，函式會在viewModelScope中啟動一個協程)Coroutine)，呼叫Repository的suspend函式，並將結果傳送到到LiveData
    fun queryEmptyRooms(
        buildingName: String, // 大樓名稱
        day: String, // 星期
        timeSlotKey: String, // 時段key (ex. Mon_1)
        floorQueryValue: String //樓 層查詢值
    ) {
        // 在ViewModel的生命週期範圍內啟動協程，當ViewModel銷毀時，協程也會自動取消
        viewModelScope.launch {
            // 呼叫Repository執行耗時的資料庫查詢動作
            val result = repository.queryEmptyRooms(
                buildingName,
                day,
                timeSlotKey,
                floorQueryValue
            )
            // 使用postValue在後台執行緒更新資料，並通知UI層
            _schedulesLiveData.postValue(result)
        }
    }
    
    // 根據教室獲取詳細資訊，將Repository回傳的Flow轉換為LiveData，供UI觀察
    fun getDetails(roomId: String): LiveData<ClassroomSchedule?> {
        return repository.getClassroomDetails(roomId).asLiveData()
    }

    // 批次插入教室課表資料，使用viewModelScope確保在背景執行緒進行資料庫寫入
    fun insertSchedules(schedules: List<ClassroomSchedule>) = viewModelScope.launch {
         repository.insertAll(schedules)
    }
}

package com.example.emptyroomfinder

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData


// 教室詳細頁面的ViewModel：負責處理教室詳細資訊相關的邏輯和資料持有
class RoomDetailViewModel(private val repository: ClassroomScheduleRepository) : ViewModel() {

    // 獲取特定教室的詳細資訊，並將其轉換為LiveData，供UI觀察，Repository會回傳一個Flow，在這裡將其轉換為LiveData
    fun getDetails(classroomName: String): LiveData<ClassroomSchedule?> {
        // 使用"asLiveData()"將Kotlin協程的Flow轉換成Android架構組件的LiveData
        return repository.getClassroomDetails(classroomName).asLiveData()
    }
}

// RoomDetailViewModel的Factory
// 由於 ViewModel 預設無法在構造函數中傳入參數(如 Repository)， 因此需要透過Factory來實例化具有依賴關係的ViewModel
class RoomDetailViewModelFactory(private val repository: ClassroomScheduleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // // 檢查傳入的Class是否為RoomDetailViewModel或其子類別
        if (modelClass.isAssignableFrom(RoomDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // 成功轉型後回傳實例化的ViewModel並注入repository
            return RoomDetailViewModel(repository) as T
        }
        // 如果傳入的類別不符合，則拋出異常
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

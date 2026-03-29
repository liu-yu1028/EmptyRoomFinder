package com.example.emptyroomfinder

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map

/**
 * 首頁 ViewModel
 * 負責從資料庫抓取原始資料，並轉換為 UI 需要的過濾選項（如：樓層清單）
 */
class MainViewModel(repository: ClassroomScheduleRepository) : ViewModel() {

    // 從 Repository 取得所有課表資料流，並轉換為 LiveData
    // 這是所有後續轉換動作的原始資料源
    private val allSchedules: LiveData<List<ClassroomSchedule>> = repository.getAllSchedules().asLiveData()

    /**
     * 自動轉換後的樓層清單 LiveData
     * 當資料庫中的教室資料變動時，此清單會自動重新計算更新
     */
    val floors: LiveData<List<String>> = allSchedules.map { schedules ->
        val floorNumbers = schedules.map { it.classroom }
            .mapNotNull { name ->
                val numberPart = when {
                    name.startsWith("SF") -> name.removePrefix("SF")
                    name.startsWith("ES") -> name.removePrefix("ES")
                    else -> null
                }

                if (numberPart != null && numberPart.isNotEmpty()) {
                    numberPart.substring(0, 1)
                } else {
                    null
                }
            }
            .distinct()
            .sortedBy { it.toIntOrNull() ?: 0 }
            .map { "${it}樓" } // Add "樓" suffix

        listOf("全部樓層") + floorNumbers
    }
}

class MainViewModelFactory(private val repository: ClassroomScheduleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

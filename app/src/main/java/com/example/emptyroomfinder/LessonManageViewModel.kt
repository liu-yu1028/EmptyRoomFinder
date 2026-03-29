package com.example.emptyroomfinder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * 管理課表時段的 ViewModel
 * 核心功能：透過 MediatorLiveData 實作「大樓 -> 樓層 -> 教室」的三級連動篩選
 */
class LessonManageViewModel(private val repository: ClassroomScheduleRepository) : ViewModel() {

    // --- Inputs from UI --- (MutableLiveData)
    val selectedBuilding = MutableLiveData<String>()
    val selectedFloor = MutableLiveData<String>()

    // --- Data sources ---
    private val allSchedules: LiveData<List<ClassroomSchedule>> = repository.getAllSchedules().asLiveData()

    // --- Outputs for UI Spinners --- (LiveData)

    val buildings: LiveData<List<String>> = MutableLiveData(listOf("聖言樓", "進修部大樓"))

    val floors: LiveData<List<String>> = MediatorLiveData<List<String>>().apply {
        addSource(allSchedules) { updateFloors() }
        addSource(selectedBuilding) { updateFloors() }
    }

    val classrooms: LiveData<List<String>> = MediatorLiveData<List<String>>().apply {
        addSource(allSchedules) { updateClassrooms() }
        addSource(selectedBuilding) { updateClassrooms() }
        addSource(selectedFloor) { updateClassrooms() }
    }

    private fun getBuildingPrefix(building: String?) = if (building == "聖言樓") "SF" else "ES"

    /**
     * 核心邏輯：更新樓層選單內容
     */
    private fun updateFloors() {
        val building = selectedBuilding.value ?: return
        val schedules = allSchedules.value ?: return
        val prefix = getBuildingPrefix(building)

        val floorNumbers = schedules
            .filter { it.classroom.startsWith(prefix) }
            .mapNotNull { it.classroom.removePrefix(prefix).getOrNull(0)?.toString() }
            .distinct()
            .sortedBy { it.toIntOrNull() ?: Int.MAX_VALUE }
            .map { "${it}樓" }

        (floors as MutableLiveData).value = listOf("所有樓層") + floorNumbers
    }

    /**
     * 核心邏輯：根據目前大樓與樓層，過濾出具體的教室名稱清單
     */
    private fun updateClassrooms() {
        val building = selectedBuilding.value ?: return
        val floor = selectedFloor.value ?: return
        val schedules = allSchedules.value ?: return
        val prefix = getBuildingPrefix(building)
        val floorNumber = if (floor == "所有樓層") null else floor.removeSuffix("樓")

        val filteredClassrooms = schedules
            .filter { it.classroom.startsWith(prefix) }
            .filter { floorNumber == null || it.classroom.removePrefix(prefix).startsWith(floorNumber) }
            .map { it.classroom }
            .distinct()
            .sorted()

        (classrooms as MutableLiveData).value = filteredClassrooms
    }

    /**
     * 取得特定教室的完整物件，以便進行編輯或刪除
     */
    fun getScheduleForClassroom(name: String): ClassroomSchedule? {
        return allSchedules.value?.find { it.classroom == name }
    }

    // --- Database Actions ---
    fun update(schedule: ClassroomSchedule) = viewModelScope.launch {
        repository.update(schedule)
    }

    fun delete(schedule: ClassroomSchedule) = viewModelScope.launch {
        repository.delete(schedule)
    }
}

class LessonManageViewModelFactory(private val repository: ClassroomScheduleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LessonManageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LessonManageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example.emptyroomfinder

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.launch

/**
 * 教室管理編輯頁面：負責修改特定教室的類型與屬性
 */
class ClassroomManageActivity : AppCompatActivity() {

    private lateinit var roomNameTextView: TextView
    private lateinit var roomTypeSpinner: Spinner
    private lateinit var foodAllowedSpinner: Spinner

    // 使用 AbstractSavedStateViewModelFactory 來初始化 ViewModel
    // 這樣可以透過 SavedStateHandle 自動取得 Intent 傳過來的 "CLASSROOM_NAME"
    private val viewModel: ClassroomManageViewModel by viewModels {
        ClassroomManageViewModelFactory(
            (application as EmptyRoomFinderApp).appContainer.classroomScheduleRepository,
            this,
            intent.extras
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_setup)

        // --- 1. 初始化 UI 元件 ---
        roomNameTextView = findViewById(R.id.room_name)
        roomTypeSpinner = findViewById(R.id.room_type_spinner)
        foodAllowedSpinner = findViewById(R.id.food_allowed_spinner)

        findViewById<Button>(R.id.modify_button).setOnClickListener { confirmModify() }
        findViewById<Button>(R.id.exit3).setOnClickListener { finish() }

        // Observe ViewModel
        viewModel.classroomSchedule.observe(this, Observer { schedule ->
            if (schedule != null) {
                roomNameTextView.text = schedule.classroom
                setupSpinners(schedule)
            } else {
                // 如果找不到教室，不要閃退，而是提示並退出
                if (viewModel.classroomName.isNotEmpty()) {
                    Toast.makeText(this, "錯誤：找不到教室 '${viewModel.classroomName}' 的資料", Toast.LENGTH_LONG).show()
                }
                finish()
            }
        })
    }

    /**
     * 設定教室類型與是否允許飲食的下拉選單
     */
    private fun setupSpinners(schedule: ClassroomSchedule) {
        val roomTypes = arrayOf("一般教室", "電腦教室", "製圖教室", "實驗室", "研討室")
        val roomTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roomTypes).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        roomTypeSpinner.adapter = roomTypeAdapter
        val roomTypePosition = roomTypes.indexOf(mapClassroomTypeToDisplay(schedule.classroomType))
        if (roomTypePosition >= 0) {
            roomTypeSpinner.setSelection(roomTypePosition)
        }

        val foodAllowedOptions = arrayOf("是", "否")
        val foodAllowedAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, foodAllowedOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        foodAllowedSpinner.adapter = foodAllowedAdapter
        val foodAllowedPosition = if (schedule.classroomType == "Normal") 0 else 1
        foodAllowedSpinner.setSelection(foodAllowedPosition)
        // 鎖定飲食選單不讓使用者手動更改（根據程式邏輯，飲食權限由教室類型自動決定）
        foodAllowedSpinner.isEnabled = false
    }

    /**
     * 點擊修改按鈕後，彈出確認對話框
     */
    private fun confirmModify() {
        AlertDialog.Builder(this)
            .setTitle("確認修改")
            .setMessage("您確定要儲存對 '${roomNameTextView.text}' 的修改嗎？")
            .setPositiveButton("確定") { _, _ -> viewModel.updateClassroomType(mapDisplayToClassroomType(roomTypeSpinner.selectedItem.toString())) }
            .setNegativeButton("取消", null)
            .show()

        // 觀察更新結果，成功後顯示 Toast 並關閉頁面
        viewModel.updateResult.observe(this, Observer { success ->
            if (success) {
                Toast.makeText(this, "教室資訊已更新", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun mapClassroomTypeToDisplay(type: String?): String {
        return when (type) {
            "Normal" -> "一般教室"; "Computer" -> "電腦教室"; "Drawing" -> "製圖教室"; "Lab" -> "實驗室"; "MeetingRoom" -> "研討室"; else -> "一般教室"
        }
    }

    private fun mapDisplayToClassroomType(display: String): String {
        return when (display) {
            "一般教室" -> "Normal"; "電腦教室" -> "Computer"; "製圖教室" -> "Drawing"; "實驗室" -> "Lab"; "研討室" -> "MeetingRoom"; else -> "Normal"
        }
    }
}

class ClassroomManageViewModel(private val repository: ClassroomScheduleRepository, private val savedStateHandle: SavedStateHandle) : ViewModel() {

    // 使用安全的方式取得名稱，避免閃退
    val classroomName: String = savedStateHandle.get<String>("CLASSROOM_NAME") ?: ""
    // 取得該教室的詳細資料 LiveData
    val classroomSchedule: LiveData<ClassroomSchedule?> = repository.getClassroomDetails(classroomName).asLiveData()

    // 自定義的 SingleLiveEvent，用於通知 UI 更新成功（僅觸發一次）
    private val _updateResult = SingleLiveEvent<Boolean>()
    val updateResult: LiveData<Boolean> = _updateResult

    /**
     * 更新教室類型：在協程中執行資料庫 update
     */
    fun updateClassroomType(newType: String) = viewModelScope.launch {
        val currentSchedule = classroomSchedule.value ?: return@launch
        val updatedSchedule = currentSchedule.copy(classroomType = newType)
        repository.update(updatedSchedule)
        _updateResult.postValue(true)
    }
}

class ClassroomManageViewModelFactory(
    private val repository: ClassroomScheduleRepository,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(ClassroomManageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClassroomManageViewModel(repository, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

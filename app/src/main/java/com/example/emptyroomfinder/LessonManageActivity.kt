package com.example.emptyroomfinder

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

/**
 * 課表詳細管理頁面：管理者可以針對特定時段修改「是否有課」狀態，或刪除教室
 */
class LessonManageActivity : AppCompatActivity() {

    private lateinit var buildingSpinner: Spinner
    private lateinit var floorSpinner: Spinner
    private lateinit var classroomSpinner: Spinner
    private lateinit var daySpinner: Spinner
    private lateinit var timeSpinner: Spinner
    private lateinit var hasClassSpinner: Spinner

    private val viewModel: LessonManageViewModel by viewModels {
        LessonManageViewModelFactory((application as EmptyRoomFinderApp).appContainer.classroomScheduleRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lesson_manage)

        initializeViews()// 1. 綁定 UI 元件
        setupStaticSpinners()// 2. 設定固定內容的選單 (星期、節次)
        observeViewModel() // 3. 觀察動態資料 (大樓、樓層、教室清單)
        setupListeners()
    }
    /**
     * 初始化下拉選單與按鈕點擊事件
     */
    private fun initializeViews() {
        buildingSpinner = findViewById(R.id.building_setting)
        floorSpinner = findViewById(R.id.floor_setting)
        classroomSpinner = findViewById(R.id.classroom_setting)
        daySpinner = findViewById(R.id.day_setting)
        timeSpinner = findViewById(R.id.time_setting)
        hasClassSpinner = findViewById(R.id.has_class_setting)

        findViewById<Button>(R.id.edit).setOnClickListener { confirmAction("修改", ::updateSchedule) }
        findViewById<Button>(R.id.delete).setOnClickListener { confirmAction("刪除", ::deleteClassroom) }
        findViewById<Button>(R.id.exit).setOnClickListener { finish() }
    }

    /**
     * 設定靜態的下拉選單內容 (不隨資料庫變動的部分)
     */
    private fun setupStaticSpinners() {
        val days = arrayOf("星期一", "星期二", "星期三", "星期四", "星期五")
        daySpinner.adapter = ArrayAdapter(this, R.layout.custom_spinner_item, days).also { it.setDropDownViewResource(R.layout.custom_spinner_item) }

        val times = arrayOf("第一節 08:10 - 09:00", "第二節 09:10 - 10:00", "第三節 10:10 - 11:00", "第四節 11:10 - 12:00", "中午 12:10 - 13:30", "第五節 13:40 - 14:30", "第六節 14:40 - 15:30", "第七節 15:40 - 16:30", "第八節 16:40 - 17:30")
        timeSpinner.adapter = ArrayAdapter(this, R.layout.custom_spinner_item, times).also { it.setDropDownViewResource(R.layout.custom_spinner_item) }

        val hasClassOptions = arrayOf("是", "否")
        hasClassSpinner.adapter = ArrayAdapter(this, R.layout.custom_spinner_item, hasClassOptions).also { it.setDropDownViewResource(R.layout.custom_spinner_item) }
    }

    /**
     * 核心邏輯：觀察資料庫，達成「大樓 -> 樓層 -> 教室」三級連動選單
     */
    private fun observeViewModel() {
        viewModel.buildings.observe(this) { buildings ->
            buildingSpinner.adapter = ArrayAdapter(this, R.layout.custom_spinner_item, buildings).also { it.setDropDownViewResource(R.layout.custom_spinner_item) }
        }

        viewModel.floors.observe(this) { floors ->
            val currentSelection = floorSpinner.selectedItem?.toString()
            floorSpinner.adapter = ArrayAdapter(this, R.layout.custom_spinner_item, floors).also { it.setDropDownViewResource(R.layout.custom_spinner_item) }
            if (currentSelection != null && floors.contains(currentSelection)) {
                floorSpinner.setSelection(floors.indexOf(currentSelection))
            }
        }

        viewModel.classrooms.observe(this) { classrooms ->
            val currentSelection = classroomSpinner.selectedItem?.toString()
            classroomSpinner.adapter = ArrayAdapter(this, R.layout.custom_spinner_item, classrooms).also { it.setDropDownViewResource(R.layout.custom_spinner_item) }
            if (currentSelection != null && classrooms.contains(currentSelection)) {
                classroomSpinner.setSelection(classrooms.indexOf(currentSelection))
            }
        }
    }

    /**
     * 執行「修改課表」邏輯
     */
    private fun setupListeners() {
        buildingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.selectedBuilding.value = parent?.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        floorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.selectedFloor.value = parent?.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun confirmAction(title: String, action: () -> Unit) {
        val classroom = classroomSpinner.selectedItem?.toString()
        if (classroom == null) {
            Toast.makeText(this, "請先選擇一間教室", Toast.LENGTH_SHORT).show()
            return
        }

        val message = if (title == "刪除") "您確定要刪除 '$classroom' 的所有資料嗎？此動作無法復原。" else "您確定要儲存這次的修改嗎？"

        AlertDialog.Builder(this)
            .setTitle("確認$title")
            .setMessage(message)
            .setPositiveButton("確定") { _, _ -> action() }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun updateSchedule() {
        val selectedClassroomName = classroomSpinner.selectedItem?.toString() ?: return
        val scheduleToUpdate = viewModel.getScheduleForClassroom(selectedClassroomName) ?: return

        val dayKey = mapDayToKey(daySpinner.selectedItem.toString())
        val timeKey = mapTimeToKey(timeSpinner.selectedItem.toString())
        val hasClass = hasClassSpinner.selectedItem.toString() == "是"
        val newValue = if (hasClass) "X" else null

        val updatedSchedule = getUpdatedSchedule(scheduleToUpdate, dayKey, timeKey, newValue)

        viewModel.update(updatedSchedule)
        Toast.makeText(this, "$selectedClassroomName 已更新", Toast.LENGTH_SHORT).show()
    }

    private fun deleteClassroom() {
        val selectedClassroomName = classroomSpinner.selectedItem?.toString() ?: return
        val scheduleToDelete = viewModel.getScheduleForClassroom(selectedClassroomName) ?: return
        viewModel.delete(scheduleToDelete)
        Toast.makeText(this, "教室 '$selectedClassroomName' 已被刪除", Toast.LENGTH_SHORT).show()
    }

    /**
     * 根據選中的星期與節次，利用 Kotlin 的 copy 函數更新 ClassroomSchedule 物件中對應的欄位
     */
    private fun getUpdatedSchedule(schedule: ClassroomSchedule, day: String, time: String, value: String?): ClassroomSchedule {
        return when (day to time) {
            "Mon" to "1" -> schedule.copy(mon1 = value)
            "Mon" to "2" -> schedule.copy(mon2 = value)
            "Mon" to "3" -> schedule.copy(mon3 = value)
            "Mon" to "4" -> schedule.copy(mon4 = value)
            "Mon" to "N" -> schedule.copy(monN = value)
            "Mon" to "5" -> schedule.copy(mon5 = value)
            "Mon" to "6" -> schedule.copy(mon6 = value)
            "Mon" to "7" -> schedule.copy(mon7 = value)
            "Mon" to "8" -> schedule.copy(mon8 = value)
            "Tue" to "1" -> schedule.copy(tue1 = value)
            "Tue" to "2" -> schedule.copy(tue2 = value)
            "Tue" to "3" -> schedule.copy(tue3 = value)
            "Tue" to "4" -> schedule.copy(tue4 = value)
            "Tue" to "N" -> schedule.copy(tueN = value)
            "Tue" to "5" -> schedule.copy(tue5 = value)
            "Tue" to "6" -> schedule.copy(tue6 = value)
            "Tue" to "7" -> schedule.copy(tue7 = value)
            "Tue" to "8" -> schedule.copy(tue8 = value)
            "Wed" to "1" -> schedule.copy(wed1 = value)
            "Wed" to "2" -> schedule.copy(wed2 = value)
            "Wed" to "3" -> schedule.copy(wed3 = value)
            "Wed" to "4" -> schedule.copy(wed4 = value)
            "Wed" to "N" -> schedule.copy(wedN = value)
            "Wed" to "5" -> schedule.copy(wed5 = value)
            "Wed" to "6" -> schedule.copy(wed6 = value)
            "Wed" to "7" -> schedule.copy(wed7 = value)
            "Wed" to "8" -> schedule.copy(wed8 = value)
            "Thu" to "1" -> schedule.copy(thu1 = value)
            "Thu" to "2" -> schedule.copy(thu2 = value)
            "Thu" to "3" -> schedule.copy(thu3 = value)
            "Thu" to "4" -> schedule.copy(thu4 = value)
            "Thu" to "N" -> schedule.copy(thuN = value)
            "Thu" to "5" -> schedule.copy(thu5 = value)
            "Thu" to "6" -> schedule.copy(thu6 = value)
            "Thu" to "7" -> schedule.copy(thu7 = value)
            "Thu" to "8" -> schedule.copy(thu8 = value)
            "Fri" to "1" -> schedule.copy(fri1 = value)
            "Fri" to "2" -> schedule.copy(fri2 = value)
            "Fri" to "3" -> schedule.copy(fri3 = value)
            "Fri" to "4" -> schedule.copy(fri4 = value)
            "Fri" to "N" -> schedule.copy(friN = value)
            "Fri" to "5" -> schedule.copy(fri5 = value)
            "Fri" to "6" -> schedule.copy(fri6 = value)
            "Fri" to "7" -> schedule.copy(fri7 = value)
            "Fri" to "8" -> schedule.copy(fri8 = value)
            else -> schedule
        }
    }

    private fun mapDayToKey(day: String): String = when (day) {
        "星期一" -> "Mon"; "星期二" -> "Tue"; "星期三" -> "Wed"; "星期四" -> "Thu"; "星期五" -> "Fri"; else -> ""
    }

    private fun mapTimeToKey(time: String): String = when (time.split(" ")[0]) {
        "第一節" -> "1"; "第二節" -> "2"; "第三節" -> "3"; "第四節" -> "4"; "中午" -> "N"; "第五節" -> "5"; "第六節" -> "6"; "第七節" -> "7"; "第八節" -> "8"; else -> ""
    }
}

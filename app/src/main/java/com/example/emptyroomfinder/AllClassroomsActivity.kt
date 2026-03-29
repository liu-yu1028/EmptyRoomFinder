package com.example.emptyroomfinder

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllClassroomsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AllClassroomsAdapter
    private val viewModel: AllClassroomsViewModel by viewModels {
        AllClassroomsViewModelFactory((application as EmptyRoomFinderApp).appContainer.classroomScheduleRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_classrooms)

        // 1. 初始化 RecyclerView
        recyclerView = findViewById(R.id.all_classrooms_recyclerview)
        adapter = AllClassroomsAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 2. 設定「新增教室」懸浮按鈕
        findViewById<FloatingActionButton>(R.id.fab_add_classroom).setOnClickListener {
            showAddClassroomDialog()
        }

        // Setup Exit Button
        findViewById<Button>(R.id.btn_exit).setOnClickListener {
            finish()
        }

        // 4. 開始觀察數據變化
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.allSchedules.observe(this, Observer { schedules ->
            // 將資料庫的實體類 (ClassroomSchedule) 轉換為 UI 顯示用的資料類 (ClassroomInfo)
            val classroomInfos = schedules.map { schedule ->
                ClassroomInfo(schedule.classroom, schedule.classroomType ?: "未知", schedule.classroomType == "Normal")
            }.sortedBy { it.name } // 依教室名稱排序
            adapter.updateData(classroomInfos)
        })
    }

    /**
     * 彈出新增教室的對話框 (AlertDialog)
     */
    private fun showAddClassroomDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_classroom, null)
        val classroomNameInput = dialogView.findViewById<EditText>(R.id.classroom_name_input)
        val classroomTypeSpinner = dialogView.findViewById<Spinner>(R.id.new_classroom_type_spinner)

        val roomTypes = arrayOf("一般教室", "電腦教室", "製圖教室", "實驗室", "研討室")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roomTypes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        classroomTypeSpinner.adapter = spinnerAdapter

        val dialog = AlertDialog.Builder(this)
            .setTitle("新增教室")
            .setView(dialogView)
            .setPositiveButton("確定", null) // Set to null initially to override later
            .setNegativeButton("取消", null)
            .create()

        dialog.show()

        // Override the "OK" button click to prevent auto-dismissal when validation fails or room exists
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = classroomNameInput.text.toString().trim().uppercase()
            val typeDisplay = classroomTypeSpinner.selectedItem.toString()

            // 檢查名稱格式：必須為 SF 或 ES 開頭後接數字
            if (name.isEmpty() || !name.matches(Regex("^(SF|ES)\\d+$"))) {
                Toast.makeText(this, "教室名稱格式不符 (需為 SF 或 ES 開頭，後面加數字)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            // 轉換顯示名稱為內部儲存標籤（如：一般教室 -> Normal）
            val type = mapDisplayToInternalType(typeDisplay)
            
            // Check if classroom exists before inserting
            lifecycleScope.launch {
                val exists = viewModel.checkExists(name)
                if (exists) {
                    // Show reminder and DO NOT dismiss the dialog
                    Toast.makeText(this@AllClassroomsActivity, "提醒：已經有這間教室了，不進行新增", Toast.LENGTH_LONG).show()
                } else {
                    // Perform insertion and dismiss the dialog
                    viewModel.insert(name, type)
                    Toast.makeText(this@AllClassroomsActivity, "教室 $name 新增成功", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
    }
    /**
     * 將選單文字映射至程式內部使用的類型字串
     */
    private fun mapDisplayToInternalType(display: String): String {
        return when (display) {
            "一般教室" -> "Normal"; "電腦教室" -> "Computer"; "製圖教室" -> "Drawing"; "實驗室" -> "Lab"; "研討室" -> "MeetingRoom"; else -> ""
        }
    }
}

/**
 * 處理教室資料邏輯的 ViewModel
 */
class AllClassroomsViewModel(private val repository: ClassroomScheduleRepository) : ViewModel() {
    val allSchedules: LiveData<List<ClassroomSchedule>> = repository.getAllSchedules().asLiveData()

    // 檢查教室是否已存在，避免重複新增
    suspend fun checkExists(classroomName: String): Boolean {
        return withContext(Dispatchers.IO) {
            repository.findByName(classroomName) != null
        }
    }

    // 建立一筆新的課表資料（初始狀態所有時段皆為 null/空閒）
    fun insert(classroomName: String, classroomType: String) = viewModelScope.launch {
        val newSchedule = ClassroomSchedule(
            classroom = classroomName, classroomType = classroomType,
            mon1 = null, mon2 = null, mon3 = null, mon4 = null, monN = null, mon5 = null, mon6 = null, mon7 = null, mon8 = null,
            tue1 = null, tue2 = null, tue3 = null, tue4 = null, tueN = null, tue5 = null, tue6 = null, tue7 = null, tue8 = null,
            wed1 = null, wed2 = null, wed3 = null, wed4 = null, wedN = null, wed5 = null, wed6 = null, wed7 = null, wed8 = null,
            thu1 = null, thu2 = null, thu3 = null, thu4 = null, thuN = null, thu5 = null, thu6 = null, thu7 = null, thu8 = null,
            fri1 = null, fri2 = null, fri3 = null, fri4 = null, friN = null, fri5 = null, fri6 = null, fri7 = null, fri8 = null
        )
        repository.insert(newSchedule)
    }
}

class AllClassroomsViewModelFactory(private val repository: ClassroomScheduleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AllClassroomsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AllClassroomsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

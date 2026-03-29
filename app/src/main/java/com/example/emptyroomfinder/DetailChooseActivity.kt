package com.example.emptyroomfinder

import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
/**
 * 教室管理選擇頁面：讓使用者從清單中選擇一間教室進行後續管理
 */
class ClassroomChooseManageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClassroomAdapter

    private val viewModel: ClassroomChooseManageViewModel by viewModels {
        ClassroomChooseManageViewModelFactory((application as EmptyRoomFinderApp).appContainer.classroomScheduleRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_choose)
        // 1. 初始化 RecyclerView
        recyclerView = findViewById(R.id.classrooms_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 初始化 Adapter，一開始先傳入空的清單
        adapter = ClassroomAdapter(emptyList())
        recyclerView.adapter = adapter

        // 2. 核心邏輯：觀察 ViewModel 中的 LiveData
        // 當資料庫中的教室資料有任何變動時，這段程式碼會被觸發
        viewModel.allClassrooms.observe(this, Observer { schedules ->
            // 從所有課表實體中，只取出「教室名稱」(String)，並進行排序
            val classroomNames = schedules.map { it.classroom }.sorted()
            adapter.updateData(classroomNames)
        })

        findViewById<Button>(R.id.exit2).setOnClickListener {
            finish()
        }
    }
}

// --- ViewModel and Factory for this Activity ---

class ClassroomChooseManageViewModel(repository: ClassroomScheduleRepository) : ViewModel() {
    // 從 Repository 取得資料流，並轉換為 LiveData 讓 Activity 可以觀察
    val allClassrooms = repository.getAllSchedules().asLiveData()
}

class ClassroomChooseManageViewModelFactory(private val repository: ClassroomScheduleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClassroomChooseManageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClassroomChooseManageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

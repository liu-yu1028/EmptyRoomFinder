package com.example.emptyroomfinder

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer

// 教室詳細資訊頁面：負責顯示單一較式的特定屬性，例如教室類型和使用規則
class RoomDetailActivity : AppCompatActivity() {

    // 定義UI元件變數
    private lateinit var classroomNameTextView: TextView
    private lateinit var classroomTypeValueTextView: TextView
    private lateinit var foodAllowedValueTextView: TextView
    private lateinit var homeButton: Button

    //初始化ViewModel，透過Factory注入Repository
    private val viewModel: RoomDetailViewModel by viewModels {
        val repository = (application as EmptyRoomFinderApp).appContainer.classroomScheduleRepository
        RoomDetailViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.room_detail)

        // 綁定XML佈局中的UI元件
        classroomNameTextView = findViewById(R.id.classroom_name)
        classroomTypeValueTextView = findViewById(R.id.classroom_type_value)
        foodAllowedValueTextView = findViewById(R.id.food_allowed_value)
        homeButton = findViewById(R.id.home_button)

        // 從Intent中取得上一個頁面傳遞過來的教室名稱
        val classroomName = intent.getStringExtra(EXTRA_CLASSROOM_NAME)

        // 如果沒有傳入教室名稱，則關閉此頁面(安全檢查)
        if (classroomName == null) {
            finish()
            return
        }

        // 開始觀察ViewModel中的教室詳細資料
        observeClassroomDetails(classroomName)

        //設置返回按鈕的點擊監聽器
        homeButton.setOnClickListener {
            finish()
        }
    }

    // 向ViewModel請求資料並訂閱變更
    private fun observeClassroomDetails(name: String) {
        // 呼叫ViewModel的"getDetails"方法，獲取LiveData，觸發Repository -> DAO -> Room資料庫的查詢流程
        viewModel.getDetails(name).observe(this, Observer { schedule ->
            // 當資料庫回傳資料時(不為null)，執行UI更新
            if (schedule != null) {
                updateUi(schedule)
            }
        })
    }

    // 將獲取到的ClassroomSchedule資料顯示到畫面上
    private fun updateUi(schedule: ClassroomSchedule) {
        // 顯示教室名稱
        classroomNameTextView.text = schedule.classroom
        // 教室類型資料轉換
        classroomTypeValueTextView.text = when (schedule.classroomType) {
            "Normal" -> "一般教室"
            "Computer" -> "電腦教室"
            "Drawing" -> "製圖教室"
            "Lab" -> "實驗室"
            "MeetingRoom" -> "研討室"
            else -> "未知"
        }
        //判斷教室是否能飲食，目前僅開放一般教室可飲食
        foodAllowedValueTextView.text = if (schedule.classroomType == "Normal") "是" else "否"
    }

    companion object {
        // 定義靜態常數，供其他頁面跳轉至此時使用相同的Key
        const val EXTRA_CLASSROOM_NAME = "com.example.emptyroomfinder.CLASSROOM_NAME"
    }
}
package com.example.emptyroomfinder

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class QueryResultActivity : AppCompatActivity() {

    // 選擇下拉選單(Spinner)
    private lateinit var daySpinner: Spinner
    private lateinit var timeSpinner: Spinner
    private lateinit var floorSpinner: Spinner
    // 顯示查詢結果的列表(RecyclerView)
    private lateinit var emptyRoomsRecyclerView: RecyclerView
    // 列表的資料銜接(Adapter)，負責將資料綁定到列表項目
    private lateinit var emptyRoomAdapter: EmptyRoomAdapter

    // 透過"by viewModels"取得ViewModel實例，用於處理資料邏輯和狀態管理
    private val viewModel: QueryResultViewModel by viewModels {
        // 使用Factory來創建ViewModel，並注入Repository(資料管理員)
        QueryResultViewModelFactory(
            (application as EmptyRoomFinderApp).appContainer.classroomScheduleRepository,
            this,
            intent.extras
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.query_result)

        // 綁定UI組件
        daySpinner = findViewById(R.id.day_spinner)
        timeSpinner = findViewById(R.id.time_spinner)
        floorSpinner = findViewById(R.id.floor_spinner)
        emptyRoomsRecyclerView = findViewById(R.id.empty_rooms_recyclerview)
        findViewById<Button>(R.id.button).setOnClickListener { finish() }

        // 設定下拉選單
        setupStaticSpinners()

        // 設定RecyclerView
        setupRecyclerView()

        // 觀察ViewModel的LiveData
        observeViewModel()
    }

    private fun setupRecyclerView() {
        //定義列表項目點擊事件，進入RoomDetailActivity頁面
        emptyRoomAdapter = EmptyRoomAdapter(emptyList()) { classroom ->
            val intent = Intent(this, RoomDetailActivity::class.java)
            //將教室名稱作為「額外資料』傳遞給下一頁
            intent.putExtra(RoomDetailActivity.EXTRA_CLASSROOM_NAME, classroom.classroom)
            startActivity(intent)
        }
        // 將Adapter設定給RecyclerView
        emptyRoomsRecyclerView.adapter = emptyRoomAdapter
        // 設定列表的佈局(垂直線性佈局)
        emptyRoomsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    // 定義下拉選單顯示選項和對應的後端key
    private fun setupStaticSpinners() {
        // Day Spinner
        val dayOptions = arrayOf("星期一", "星期二", "星期三", "星期四", "星期五")
        val dayAdapter = ArrayAdapter(this, R.layout.custom_spinner_item, dayOptions)
        dayAdapter.setDropDownViewResource(R.layout.custom_spinner_item)
        daySpinner.adapter = dayAdapter

        // Time Spinner
        val timeOptions = arrayOf("第一節 08:10 - 09:00", "第二節 09:10 - 10:00", "第三節 10:10 - 11:00", "第四節 11:10 - 12:00", "中午 12:10 - 13:30", "第五節 13:40 - 14:30", "第六節 14:40 - 15:30", "第七節 15:40 - 16:30", "第八節 16:40 - 17:30")
        val timeAdapter = ArrayAdapter(this, R.layout.custom_spinner_item, timeOptions)
        timeAdapter.setDropDownViewResource(R.layout.custom_spinner_item)
        timeSpinner.adapter = timeAdapter

        // 設定監聽器來更新ViewModel的選擇結果
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //將day Spinner選擇的向轉換為字串，並賦值給viewModel的selectedDay
                viewModel.selectedDay.value = daySpinner.selectedItem.toString()
                // 僅提取時間字串中的「主要部份」給ViewModel使用
                viewModel.selectedTime.value = timeSpinner.selectedItem.toString().substringBefore(" ")
                // 樓層的部分，監聽器已在observeViewModel中設置完成(下方)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { /* Do nothing */ }
        }
        // 將監聽器設定給下拉選單
        daySpinner.onItemSelectedListener = listener
        timeSpinner.onItemSelectedListener = listener
    }

    private fun observeViewModel() {
        // 觀察floors(樓層資料)並設定floor spinner
        viewModel.floors.observe(this, Observer { floors ->
            val floorAdapter = ArrayAdapter(this, R.layout.custom_spinner_item, floors)
            floorAdapter.setDropDownViewResource(R.layout.custom_spinner_item)
            floorSpinner.adapter = floorAdapter

            // 在floor spinner填入資料後，在設置其他監聽器
            floorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    viewModel.selectedFloor.value = floorSpinner.selectedItem.toString()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {  }
            }
        })

        // 觀察最終空emptyRooms(教室列表)並更新RecyclerView
        viewModel.emptyRooms.observe(this, Observer { rooms ->
            //當資料變動時，更新Adapter的內容
            emptyRoomAdapter.updateRoomsAndAddHeaders(rooms)
        })
    }
}

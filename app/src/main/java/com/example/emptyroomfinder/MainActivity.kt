package com.example.emptyroomfinder

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// 應用程式的主入口頁面（首頁）
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 處理視窗邊距，確保UI不會被系統狀態獵獲導覽列遮擋
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 設置建築物下拉選單
        val buildingSpinner = findViewById<Spinner>(R.id.building_spinner)
        val buildings = arrayOf("聖言樓", "進修部大樓")
        // 使用自定義的佈局(custom_spinner_item)建立適配器
        val buildingAdapter = ArrayAdapter(this, R.layout.custom_spinner_item, buildings)
        buildingAdapter.setDropDownViewResource(R.layout.custom_spinner_item)
        buildingSpinner.adapter = buildingAdapter

        // 設置開始查詢按鈕
        findViewById<Button>(R.id.button).setOnClickListener {
            val selectedBuilding = buildingSpinner.selectedItem.toString()

            // 建立Intent跳轉至查詢結果頁面(QueryResultActivity)
            val intent = Intent(this, QueryResultActivity::class.java).apply {
                putExtra("building", selectedBuilding)
            }
            startActivity(intent)
        }

        // 設置管理者按鈕
        findViewById<Button>(R.id.manage_button).setOnClickListener {
            // 跳轉至登入頁面(LoginActivity)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}

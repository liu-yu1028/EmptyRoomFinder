package com.example.emptyroomfinder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * 管理者模式主頁面
 * 負責提供各項管理功能的跳轉入口
 */
class ManagerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manager)

        // 1. 「課表管理」按鈕：跳轉至 LessonManageActivity
        val lessonSettingButton = findViewById<Button>(R.id.lesson_setting)
        lessonSettingButton.setOnClickListener {
            val intent = Intent(this, LessonManageActivity::class.java)
            startActivity(intent)
        }

        // 2. 「教室詳細設定」按鈕：跳轉至 ClassroomChooseManageActivity
        val classroomSettingButton = findViewById<Button>(R.id.classroom_setting)
        classroomSettingButton.setOnClickListener {
            val intent = Intent(this, ClassroomChooseManageActivity::class.java)
            startActivity(intent)
        }
// 3. 「瀏覽所有教室」按鈕：跳轉至 AllClassroomsActivity
        val browseClassroomsButton = findViewById<Button>(R.id.browse_classrooms_button)
        browseClassroomsButton.setOnClickListener {
            val intent = Intent(this, AllClassroomsActivity::class.java)
            startActivity(intent)
        }

        val exitButton = findViewById<Button>(R.id.exit_button)
        exitButton.setOnClickListener {
            // 清除登入狀態，確保下次進來需要重新登入
            val pref = getSharedPreferences("LoginData", Context.MODE_PRIVATE)
            pref.edit().putBoolean("isLoggedIn", false).apply()

            // 顯示提示訊息
            Toast.makeText(this, "已登出並返回主畫面", Toast.LENGTH_SHORT).show()

            // 跳轉回 MainActivity
            val intent = Intent(this, MainActivity::class.java)

            // 加上這兩行 Flag 可以清除掉之前的 Activity 堆疊，讓 App 回到乾淨的主頁
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }
}

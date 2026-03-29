package com.example.emptyroomfinder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// 管理員登入頁面
class LoginActivity : AppCompatActivity() {

    // 預設的管理員帳號與密碼（實際開發建議存放於伺服器或加密後的資料庫）
    private val ADMIN_USER = "1234"
    private val ADMIN_PASS = "1234"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 初始化UI元件
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnExit = findViewById<Button>(R.id.btnExit)

        // 登入按鈕點擊事件
        btnLogin.setOnClickListener {
            val user = etUsername.text.toString()
            val pass = etPassword.text.toString()

            // 驗證帳號密碼是否正確
            if (user == ADMIN_USER && pass == ADMIN_PASS) {
                val pref = getSharedPreferences("LoginData", Context.MODE_PRIVATE)
                pref.edit().putBoolean("isLoggedIn", true).apply()

                Toast.makeText(this, "登入成功", Toast.LENGTH_SHORT).show()
                // 跳轉至管理者專屬頁面
                goToManagerPage()
            } else {
                // 登入失敗提示
                Toast.makeText(this, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show()
            }
        }

        // 離開按鈕點擊事件：關閉目前頁面並返回上一頁
        btnExit.setOnClickListener {
            finish()
        }
    }

    // 跳轉至管理頁面(ManagerActivity)並結束目前頁面
    private fun goToManagerPage() {
        val intent = Intent(this, ManagerActivity::class.java)
        startActivity(intent)
        finish() 
    }
}

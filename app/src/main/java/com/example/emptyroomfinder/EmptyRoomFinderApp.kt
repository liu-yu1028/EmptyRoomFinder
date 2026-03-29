package com.example.emptyroomfinder

import android.app.Application

// 自定義的Application類別，用於持有AppContainer，這個實例的生命週期會和整個應用程式同步
class EmptyRoomFinderApp : Application() {

    // AppContainer實例：用於依賴注入(Dependency Injection)
    // 專案中的其他類別(如 Activity)會透過這個實例來獲取所需的依賴(如 Repository)
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        // 在App啟動時立即初始化AppContainer
        appContainer = AppContainer(this)
    }
}

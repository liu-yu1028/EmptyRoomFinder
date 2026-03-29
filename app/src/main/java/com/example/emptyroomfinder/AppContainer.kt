package com.example.emptyroomfinder

import android.content.Context

// 依賴注入容器：用於管理整個App需要共享的依賴物件，助於統一依賴關係，讓程式碼容易測試與維護
class AppContainer(context: Context) {

    // ClassroomScheduleRepository 實例：會被建立一次並在多個頁面間共享
    //  利用"by lazy" (延遲初始化)： 代表這個 Repository 只有在第一次被程式碼存取時才會真正建立
    val classroomScheduleRepository: ClassroomScheduleRepository by lazy {
        // 1. 取得資料庫實例(AppDatabase)
        val db = AppDatabase.getInstance(context)
        // 2. 將資料庫的DAO傳入Repository並回傳
        ClassroomScheduleRepository(db.classroomScheduleDao())
    }
}

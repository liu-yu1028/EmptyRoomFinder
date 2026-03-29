package com.example.emptyroomfinder

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

// 定義App的Room資料庫
// entities: 指定資料表類別為ClassroomSchedule
@Database(entities = [ClassroomSchedule::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // 定義取得DAO的抽象方法
    abstract fun classroomScheduleDao(): ClassroomScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 取得資料庫實例的單例(Singleton)方法
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "classroom_database"
                )
                    // 當資料庫版本升級且未提供遷移路徑時，允許刪除舊資料並重建
                .fallbackToDestructiveMigration()
                // 設定資料庫生命週期的Callback函式
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        // 處理資料庫建立與開啟時的動作
        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            // 當資料庫首次被建立時執行
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                triggerPopulate()
            }

            // 當資料庫被開啟時執行
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                triggerPopulate()
            }

            // 透過協程在背景執行緒啟動資料預填
            private fun triggerPopulate() {
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = database.classroomScheduleDao()
                        prePopulateDatabase(context, dao)
                    }
                }
            }

            // 讀取CSV資源並將其寫入資料庫
            private suspend fun prePopulateDatabase(context: Context, dao: ClassroomScheduleDao) {
                val sfSchedules = parseCsv(context, "SF.csv")
                val esSchedules = parseCsv(context, "ES.csv")
                // 確保清單中有資料才執行資料庫插入
                if (sfSchedules.isNotEmpty() || esSchedules.isNotEmpty()) {
                    dao.insertAll(sfSchedules + esSchedules)
                }
            }

            // 解析位於assets資料夾中的CSV檔案
            private fun parseCsv(context: Context, fileName: String): List<ClassroomSchedule> {
                return try {
                    val inputStream = context.assets.open(fileName)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val lines = reader.readLines()
                    
                    // 略過第一行標題，將其餘每一行解析為ClassroomSchedule物件
                    lines.drop(1).mapNotNull { line ->
                        val parts = line.split(",")
                        if (parts.size >= 47) {
                            // 直接在建構子中完成所有賦值
                            ClassroomSchedule(
                                classroom = parts[0],
                                mon1 = parts[1], mon2 = parts[2], mon3 = parts[3], mon4 = parts[4], monN = parts[5], mon5 = parts[6], mon6 = parts[7], mon7 = parts[8], mon8 = parts[9],
                                tue1 = parts[10], tue2 = parts[11], tue3 = parts[12], tue4 = parts[13], tueN = parts[14], tue5 = parts[15], tue6 = parts[16], tue7 = parts[17], tue8 = parts[18],
                                wed1 = parts[19], wed2 = parts[20], wed3 = parts[21], wed4 = parts[22], wedN = parts[23], wed5 = parts[24], wed6 = parts[25], wed7 = parts[26], wed8 = parts[27],
                                thu1 = parts[28], thu2 = parts[29], thu3 = parts[30], thu4 = parts[31], thuN = parts[32], thu5 = parts[33], thu6 = parts[34], thu7 = parts[35], 
                                thu8 = parts[36], // 這裡已經正確賦值了
                                fri1 = parts[37], fri2 = parts[38], fri3 = parts[39], fri4 = parts[40], friN = parts[41], fri5 = parts[42], fri6 = parts[43], fri7 = parts[44], fri8 = parts[45],
                                classroomType = parts[46]
                            )
                        } else null
                    }
                    // 若讀取或解析失敗，回傳空清單
                } catch (e: Exception) { emptyList() }
            }
        }
    }
}

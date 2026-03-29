package com.example.emptyroomfinder

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * 教室課表詳細資訊頁面
 */
class ClassroomScheduleDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CLASSROOM_NAME = "CLASSROOM_NAME"
    }

    private val viewModel: ClassroomScheduleDetailViewModel by viewModels {
        ClassroomScheduleDetailViewModelFactory(
            (application as EmptyRoomFinderApp).appContainer.classroomScheduleRepository,
            this,
            intent.extras // 確保傳入 Bundle 讓 ViewModel 讀取資料
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classroom_schedule_detail)

        val classroomNameTitle: TextView = findViewById(R.id.classroom_name_title)
        val scheduleDetailsText: TextView = findViewById(R.id.schedule_details_text)
        val backBtn: Button = findViewById(R.id.back_button)

        // 返回按鈕邏輯：關閉當前頁面回到 AllClassroomsActivity
        backBtn.setOnClickListener {
            finish()
        }

        // 觀察 LiveData 變化並更新 UI
        viewModel.classroomSchedule.observe(this) { schedule ->
            if (schedule != null) {
                classroomNameTitle.text = "${schedule.classroom} 課表"
                scheduleDetailsText.text = formatScheduleToDisplayString(schedule)
            } else {
                classroomNameTitle.text = "找不到課表"
                scheduleDetailsText.text = ""
            }
        }
    }

    /**
     * 將資料庫物件轉換為顯示在 ScrollView 中的文字列表
     */
    private fun formatScheduleToDisplayString(s: ClassroomSchedule): String {
        val sb = StringBuilder()

        // 定義節次對應的時間字串
        val timeMap = mapOf(
            "1" to "08:10 - 09:00",
            "2" to "09:10 - 10:00",
            "3" to "10:10 - 11:00",
            "4" to "11:10 - 12:00",
            "N" to "12:10 - 13:00",
            "5" to "13:40 - 14:30",
            "6" to "14:40 - 15:30",
            "7" to "15:40 - 16:30",
            "8" to "16:40 - 17:30"
        )

        val days = listOf("星期一" to "mon", "星期二" to "tue", "星期三" to "wed", "星期四" to "thu", "星期五" to "fri")
        val periods = listOf("1", "2", "3", "4", "N", "5", "6", "7", "8")

        for ((dayTitle, dayKey) in days) {
            sb.append("$dayTitle\n")
            var hasClass = false

            for (p in periods) {
                // 利用反射技術檢查 mon1, mon2... 等欄位是否為 "X"
                if (getStatusViaReflection(s, "$dayKey$p") == "X") {
                    val timeRange = timeMap[p] ?: ""
                    val periodName = if (p == "N") "第 N 節" else "第 $p 節"
                    sb.append("$periodName $timeRange\n")
                    hasClass = true
                }
            }

            if (!hasClass) {
                sb.append("(此日無課)\n")
            }
            sb.append("--------------------\n")
        }
        return sb.toString()
    }

    /**
     * 利用反射動態獲取 ClassroomSchedule 物件中的欄位值
     */
    private fun getStatusViaReflection(schedule: ClassroomSchedule, fieldName: String): String? {
        return try {
            val field = schedule::class.java.getDeclaredField(fieldName)
            field.isAccessible = true
            field.get(schedule) as? String
        } catch (e: Exception) {
            null
        }
    }
}

// --- ViewModel 實作 ---

class ClassroomScheduleDetailViewModel(
    private val repository: ClassroomScheduleRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 從 Intent 傳過來的名稱
    private val classroomName: LiveData<String?> =
        savedStateHandle.getLiveData(ClassroomScheduleDetailActivity.EXTRA_CLASSROOM_NAME)

    // 自動根據名稱從 Repository 抓取詳細課表資料
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val classroomSchedule: LiveData<ClassroomSchedule?> = classroomName.asFlow().flatMapLatest { name ->
        if (name != null) {
            repository.getClassroomDetails(name)
        } else {
            flowOf(null)
        }
    }.asLiveData()
}

// --- ViewModel Factory 實作 ---

class ClassroomScheduleDetailViewModelFactory(
    private val repository: ClassroomScheduleRepository,
    owner: SavedStateRegistryOwner,
    private val defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        if (modelClass.isAssignableFrom(ClassroomScheduleDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClassroomScheduleDetailViewModel(repository, handle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.example.emptyroomfinder

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class ClassroomInfo(val name: String, val type: String, val foodAllowed: Boolean)

//將 ClassroomInfo 資料集合與 RecyclerView 的列表項 (UI) 進行綁定
class AllClassroomsAdapter(private var classrooms: List<ClassroomInfo>) : RecyclerView.Adapter<AllClassroomsAdapter.ViewHolder>() {
    /**
     * 建立每一列項目的 View 容器
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_all_classrooms, parent, false)
        return ViewHolder(view)
    }

    /**
     * 將資料綁定到 View，並設定點擊事件
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val classroom = classrooms[position]
        holder.bind(classroom)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            // Corrected the Intent to point to the schedule detail activity
            val intent = Intent(context, ClassroomScheduleDetailActivity::class.java).apply {
                // 將點擊的教室名稱傳遞給下一個頁面
                putExtra("CLASSROOM_NAME", classroom.name)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = classrooms.size

    /**
     * 動態更新列表資料的方法
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newClassrooms: List<ClassroomInfo>) {
        this.classrooms = newClassrooms
        notifyDataSetChanged() // 通知 RecyclerView 資料已改變，需刷新 UI
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.classroom_name_text)
        private val typeTextView: TextView = itemView.findViewById(R.id.classroom_type_text)
        private val foodAllowedTextView: TextView = itemView.findViewById(R.id.food_allowed_text)

        fun bind(classroom: ClassroomInfo) {
            nameTextView.text = classroom.name
            typeTextView.text = when (classroom.type) {
                "Normal" -> "一般教室"
                "Computer" -> "電腦教室"
                "Drawing" -> "製圖教室"
                "Lab" -> "實驗室"
                "MeetingRoom" -> "研討室"
                else -> "未知"
            }
            foodAllowedTextView.text = if (classroom.foodAllowed) "是" else "否"
        }
    }
}
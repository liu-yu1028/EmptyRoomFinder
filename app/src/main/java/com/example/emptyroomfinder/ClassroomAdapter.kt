package com.example.emptyroomfinder

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//負責將教室名稱(頁面最上方)顯示在每一頁
class ClassroomAdapter(private var classrooms: List<String>) : RecyclerView.Adapter<ClassroomAdapter.ClassroomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassroomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_classroom, parent, false)
        return ClassroomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassroomViewHolder, position: Int) {
        val classroom = classrooms[position]
        holder.bind(classroom)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ClassroomManageActivity::class.java).apply {
                putExtra("CLASSROOM_NAME", classroom)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = classrooms.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newClassrooms: List<String>) {
        this.classrooms = newClassrooms
        notifyDataSetChanged()
    }

    class ClassroomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val classroomName: TextView = itemView.findViewById(R.id.classroom_name)

        fun bind(classroom: String) {
            classroomName.text = classroom
        }
    }
}

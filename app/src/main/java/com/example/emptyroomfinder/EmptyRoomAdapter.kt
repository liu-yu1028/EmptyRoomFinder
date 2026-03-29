package com.example.emptyroomfinder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 儲存列表中的項目，使用 "List<Any>"是因為需要存放兩種型別：
// 1.String(樓層標題) 2.ClassroomSchedule(教室資料)
class EmptyRoomAdapter(
    private var items: List<Any>,
    //點擊監聽器：當用戶點擊單一教室的項目時，執行Lamba函數
    private val onItemClick: (ClassroomSchedule) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 定義兩種不同的View Types
    private val viewTypeHeader = 0 // 樓層標題的類型
    private val viewTypeRoom = 1 // 教室資料的類型

    // 專門用於樓層標題的ViewHolder
    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerTitle: TextView = view.findViewById(R.id.header_title)
    }

    // 專門用於教室項目的ViewHolder
    class RoomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val roomNameTextView: TextView = view.findViewById(R.id.room_name_textview)
    }

    override fun getItemViewType(position: Int): Int {
        // 根據items列表中該位置資料的「實際類型」，回傳對應的佈局類型(ViewType)
        return when (items[position]) {
            // 如果資料類型是String，視為「樓層標題」
            is String -> viewTypeHeader
            // 如果資料類型是是ClassroomSchedule，視為「教室項目」
            is ClassroomSchedule -> viewTypeRoom
            // 如果出現以上兩者以外的類型，則拋出異常，標示該位置的資料無效
            else -> throw IllegalArgumentException("Invalid type of data at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // 根據"getItemViewType"回傳的ViewType，來定要載入哪個佈局
        return when (viewType) {
            viewTypeHeader -> {
                // 載入「樓層標題」佈局
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_floor_header, parent, false)
                HeaderViewHolder(view)
            }
            viewTypeRoom -> {
                // 載入「教室項目」佈局
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_empty_room, parent, false)
                RoomViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    // 綁定資料到視圖
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // 根據傳入的ViewHoler類型，進行類型轉換
        when (holder) {
            // 處理「樓層標題」的視圖
            is HeaderViewHolder -> {
                // 將items列表中的內容轉換為String，並設定給標題TextView
                holder.headerTitle.text = items[position] as String
            }
            // 處理「教室項目」的視圖
            is RoomViewHolder -> {
                // 將items列表中資料轉換為ClassroomSchedule類型
                val room = items[position] as ClassroomSchedule
                // 設定教室名稱(ex. "Room xxx")
                holder.roomNameTextView.text = room.classroom

                // 在項目視圖(itemView)設置點擊監聽器
                holder.itemView.setOnClickListener {
                    // 當使用者點擊教室時，觸發CallBack函式，並傳入該教室資料
                    onItemClick(room)
                }
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateRoomsAndAddHeaders(newRooms: List<ClassroomSchedule>) {
        val newItems = mutableListOf<Any>()
        if (newRooms.isNotEmpty()) {
            val groupedByFloor = newRooms.groupBy { extractFloor(it.classroom) }
            val sortedFloors = groupedByFloor.keys.sorted()

            for (floor in sortedFloors) {
                newItems.add("-- ${floor}樓 --")
                groupedByFloor[floor]?.sortedBy { it.classroom }?.let { newItems.addAll(it) }
            }
        }
        items = newItems
        notifyDataSetChanged()
    }

    /**
     * 輔助功能：從教室名稱中提取第一個數字作為樓層 (例如 SF204 -> 2)
     */
    private fun extractFloor(classroomName: String): Int {
        val firstDigitChar = classroomName.find { it.isDigit() }
        return firstDigitChar?.toString()?.toIntOrNull() ?: 0
    }
}

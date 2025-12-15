package com.example.brooksconnect

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class CalendarDaysAdapter(
    private val days: List<String>,
    private val todayDay: String // e.g. "16"
) : RecyclerView.Adapter<CalendarDaysAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayText: TextView = view.findViewById(R.id.day_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = days[position]
        holder.dayText.text = day

        if (day.isNotEmpty()) {
            if (day == todayDay) {
                // Highlight Today (Blue Circle)
                holder.dayText.setBackgroundResource(R.drawable.blue_circle_filled)
                holder.dayText.setTextColor(Color.WHITE)
                holder.dayText.setTypeface(null, Typeface.BOLD)
            } else {
                holder.dayText.background = null
                holder.dayText.setTextColor(Color.parseColor("#333333"))
                holder.dayText.setTypeface(null, Typeface.NORMAL)
            }
        } else {
            holder.dayText.background = null
            holder.dayText.text = ""
        }
    }

    override fun getItemCount() = days.size
}

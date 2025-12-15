package com.example.brooksconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RecentActivityItem(
    val title: String,
    val status: String,
    val date: Long,
    val type: ActivityType,
    val id: String
)

enum class ActivityType {
    REPORT, REQUEST
}

class RecentActivityAdapter(
    private val items: List<RecentActivityItem>,
    private val onItemClick: (RecentActivityItem) -> Unit
) : RecyclerView.Adapter<RecentActivityAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.activity_icon)
        val title: TextView = view.findViewById(R.id.activity_title)
        val status: TextView = view.findViewById(R.id.activity_status)
        val date: TextView = view.findViewById(R.id.activity_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.title.text = item.title
        holder.status.text = "Status: ${item.status.replaceFirstChar { it.uppercase() }}"

        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        holder.date.text = dateFormat.format(Date(item.date))

        if (item.type == ActivityType.REPORT) {
            holder.icon.setImageResource(R.drawable.ic_report_issue)
            holder.icon.setBackgroundResource(R.drawable.report_issue_icon_background) // Orangeish
            // Or use dedicated background as per screenshot (looks Orange)
        } else {
            holder.icon.setImageResource(R.drawable.ic_requests)
            holder.icon.setBackgroundResource(R.drawable.requests_icon_background) // Greenish
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}

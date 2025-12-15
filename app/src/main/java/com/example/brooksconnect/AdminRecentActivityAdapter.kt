package com.example.brooksconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AdminRecentItem(
    val title: String,
    val userName: String,
    val status: String,
    val date: Long,
    val type: ActivityType,
    val id: String,
    val report: Report? = null,
    val request: Request? = null
)

class AdminRecentActivityAdapter(
    private val items: List<AdminRecentItem>,
    private val onItemClick: (AdminRecentItem) -> Unit
) : RecyclerView.Adapter<AdminRecentActivityAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.icon)
        val title: TextView = view.findViewById(R.id.title)
        val userName: TextView = view.findViewById(R.id.user_name)
        val date: TextView = view.findViewById(R.id.date)
        val status: TextView = view.findViewById(R.id.status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_recent_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.title.text = item.title
        holder.userName.text = "by ${item.userName}"

        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        holder.date.text = dateFormat.format(Date(item.date))
        
        holder.status.text = item.status

        // Status Styling
        val status = item.status.lowercase()
        if (status == "pending" || status == "received") {
             holder.status.setBackgroundResource(R.drawable.in_progress_chip_background) // Orange/Yellowish
             holder.status.setTextColor(ContextCompat.getColor(context, R.color.orange_500))
             holder.status.text = if(status == "received") "Received" else "Pending"
        } else if (status == "in-progress" || status == "processing") {
             holder.status.setBackgroundResource(R.drawable.in_progress_chip_background)
             holder.status.setTextColor(ContextCompat.getColor(context, R.color.orange_500))
        } else if (status == "resolved" || status == "completed") {
             holder.status.setBackgroundResource(R.drawable.completed_chip_background)
             holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.white)) // Or grey text on green bg
             // Actually completed_chip_background usually implies grey/green. 
             // Let's assume white text for success
             holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        } else if (status == "ready") {
             holder.status.setBackgroundResource(R.drawable.ready_chip_background)
             holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        } else {
             holder.status.setBackgroundResource(R.drawable.chip_unselected_background)
             holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        }

        if (item.type == ActivityType.REPORT) {
            holder.icon.setImageResource(R.drawable.ic_report_issue)
            holder.icon.setBackgroundResource(R.drawable.report_issue_icon_background)
        } else {
            holder.icon.setImageResource(R.drawable.ic_requests)
            holder.icon.setBackgroundResource(R.drawable.requests_icon_background)
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size
}

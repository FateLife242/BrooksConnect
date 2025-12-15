package com.example.brooksconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class AnnouncementsRecordAdapter(private var announcements: List<Announcement>) : RecyclerView.Adapter<AnnouncementsRecordAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_announcement_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val announcement = announcements[position]
        holder.category.text = announcement.category
        holder.date.text = formatDate(announcement.date)
        holder.title.text = announcement.title
        holder.description.text = announcement.description

        // Set category background color
        when (announcement.category) {
            "Events" -> {
                holder.category.setBackgroundResource(R.drawable.events_chip_background)
                holder.category.setTextColor(holder.itemView.context.getColor(R.color.purple_500))
            }
            "Notice" -> {
                holder.category.setBackgroundResource(R.drawable.notice_chip_background)
                // Use blue text color for better visibility on light blue background
                holder.category.setTextColor(holder.itemView.context.getColor(R.color.blue_500))
            }
            "Advisory" -> {
                holder.category.setBackgroundResource(R.drawable.advisory_chip_background)
                // Use orange text color for better visibility on light orange background
                holder.category.setTextColor(holder.itemView.context.getColor(R.color.orange_500))
            }
            "Public Safety" -> {
                holder.category.setBackgroundResource(R.drawable.events_chip_background)
                holder.category.setTextColor(holder.itemView.context.getColor(R.color.purple_500))
            }
            else -> {
                holder.category.setBackgroundResource(R.drawable.events_chip_background)
                holder.category.setTextColor(holder.itemView.context.getColor(R.color.purple_500))
            }
        }
    }

    override fun getItemCount() = announcements.size

    fun updateAnnouncements(newAnnouncements: List<Announcement>) {
        announcements = newAnnouncements
        notifyDataSetChanged()
    }

    private fun formatDate(dateString: String): String {
        return try {
            // Try to parse "Nov 25, 2024" format
            val inputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val category: TextView = itemView.findViewById(R.id.announcement_category)
        val date: TextView = itemView.findViewById(R.id.announcement_date)
        val title: TextView = itemView.findViewById(R.id.announcement_title)
        val description: TextView = itemView.findViewById(R.id.announcement_description)
    }
}


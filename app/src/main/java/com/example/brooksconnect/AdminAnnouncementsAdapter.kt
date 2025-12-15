package com.example.brooksconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminAnnouncementsAdapter(
    private var announcements: MutableList<Announcement>,
    private val onEditClick: (Announcement, Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<AdminAnnouncementsAdapter.ViewHolder>() {

    private var filteredAnnouncements: List<Announcement> = announcements

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_announcement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val announcement = filteredAnnouncements[position]
        holder.category.text = announcement.category
        holder.date.text = announcement.date
        holder.title.text = announcement.title
        holder.description.text = announcement.description
        holder.editButton.setOnClickListener { onEditClick(announcement, position) }
        holder.deleteButton.setOnClickListener { onDeleteClick(position) }

        when (announcement.category) {
            "Events" -> {
                holder.category.setBackgroundResource(R.drawable.events_chip_background)
                holder.category.setTextColor(holder.itemView.context.getColor(R.color.purple_500))
            }
            "Notice" -> {
                holder.category.setBackgroundResource(R.drawable.notice_chip_background)
                holder.category.setTextColor(holder.itemView.context.getColor(R.color.blue_500))
            }
            "Advisory" -> {
                holder.category.setBackgroundResource(R.drawable.advisory_chip_background)
                holder.category.setTextColor(holder.itemView.context.getColor(R.color.orange_500))
            }
            else -> {
                holder.category.setBackgroundResource(R.drawable.chip_unselected_background)
                holder.category.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
            }
        }
    }

    override fun getItemCount() = filteredAnnouncements.size

    fun filter(category: String) {
        filteredAnnouncements = if (category == "All") {
            announcements
        } else {
            announcements.filter { it.category == category }
        }
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val category: TextView = itemView.findViewById(R.id.announcement_category)
        val date: TextView = itemView.findViewById(R.id.announcement_date)
        val title: TextView = itemView.findViewById(R.id.announcement_title)
        val description: TextView = itemView.findViewById(R.id.announcement_description)
        val editButton: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.edit_button)
        val deleteButton: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.delete_button)
    }

    fun removeAt(position: Int) {
        if (position in announcements.indices) {
            filteredAnnouncements = announcements
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, filteredAnnouncements.size - position)
        }
    }

    fun refreshData() {
        filteredAnnouncements = announcements
        notifyDataSetChanged()
    }
}

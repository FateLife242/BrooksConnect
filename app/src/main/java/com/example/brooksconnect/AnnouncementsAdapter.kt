package com.example.brooksconnect

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AnnouncementsAdapter(private var announcements: List<Announcement>) : RecyclerView.Adapter<AnnouncementsAdapter.ViewHolder>() {

    private var filteredAnnouncements: List<Announcement> = announcements

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_announcement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val announcement = filteredAnnouncements[position]
        holder.category.text = announcement.category
        holder.date.text = announcement.date
        holder.title.text = announcement.title
        holder.description.text = announcement.description

        when (announcement.category) {
            "Events" -> {
                holder.category.setBackgroundResource(R.drawable.events_chip_background)
                holder.category.setTextColor(holder.itemView.context.getColor(R.color.purple_500))
                holder.icon.setBackgroundResource(R.drawable.announcements_icon_background)
            }
            "Notice" -> {
                holder.category.setBackgroundResource(R.drawable.notice_chip_background)
                holder.category.setTextColor(holder.itemView.context.getColor(R.color.blue_500))
                holder.icon.setBackgroundResource(R.drawable.notice_chip_background)
            }
            "Advisory" -> {
                holder.category.setBackgroundResource(R.drawable.advisory_chip_background)
                holder.category.setTextColor(holder.itemView.context.getColor(R.color.orange_500))
                holder.icon.setBackgroundResource(R.drawable.advisory_chip_background)
            }
            "Public Safety" -> {
                holder.category.setBackgroundResource(R.drawable.advisory_chip_background) // Reusing shape, color via tint or separate drawable?
                // Assuming backgrounds are colored drawables. If I reuse advisory (Orange), it will look Orange.
                // I should probably tint it or use a new drawable. 
                // Let's assume I can reuse and just set text color for now if I don't have a red drawable.
                // Or better, use a generic background and set Tint.
                // Actually existing "advisory_chip_background" probably has hardcoded orange stroke/fill.
                // Let's use `advisory_chip_background` but try to set tint if possible, or just accept Orange for now if Red unavailable.
                // Wait, I can't easily create a new XML here.
                // I will use `advisory_chip_background` but set text color to RED #D32F2F.
                holder.category.setBackgroundResource(R.drawable.advisory_chip_background) 
                holder.category.setTextColor(android.graphics.Color.parseColor("#D32F2F")) 
                holder.category.background.setTint(android.graphics.Color.parseColor("#FFEBEE")) // Light Red background check?
                // The background is a drawable resource, likely shared. modifying it modifies all instances!
                // So I must mutate it.
                holder.category.background.mutate().setTint(android.graphics.Color.parseColor("#FFEBEE"))
                
                holder.icon.setBackgroundResource(R.drawable.advisory_chip_background)
                holder.icon.background.mutate().setTint(android.graphics.Color.parseColor("#FFEBEE"))
                holder.icon.setColorFilter(android.graphics.Color.parseColor("#D32F2F"))
            }
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, AnnouncementDetailActivity::class.java)
            intent.putExtra("announcement", announcement)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = filteredAnnouncements.size

    fun filter(category: String) {
        val cleanCategory = category.trim()
        filteredAnnouncements = if (cleanCategory.equals("All", ignoreCase = true)) {
            announcements
        } else {
            announcements.filter { it.category.trim().equals(cleanCategory, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val category: TextView = itemView.findViewById(R.id.announcement_category)
        val date: TextView = itemView.findViewById(R.id.announcement_date)
        val title: TextView = itemView.findViewById(R.id.announcement_title)
        val description: TextView = itemView.findViewById(R.id.announcement_description)
        val icon: ImageView = itemView.findViewById(R.id.announcement_icon)
    }
}

package com.example.brooksconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommunityEventsAdapter(private var events: MutableList<CommunityEvent>) : RecyclerView.Adapter<CommunityEventsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val month: TextView = view.findViewById(R.id.event_month)
        val day: TextView = view.findViewById(R.id.event_day)
        val title: TextView = view.findViewById(R.id.event_title)
        val location: TextView = view.findViewById(R.id.event_location)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_community_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]
        holder.month.text = event.month
        holder.day.text = event.day
        holder.title.text = event.title
        holder.location.text = event.location

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = android.content.Intent(context, EventDetailActivity::class.java).apply {
                putExtra("title", event.title)
                putExtra("location", event.location)
                putExtra("month", event.month)
                putExtra("day", event.day)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = events.size

    fun updateEvents(newEvents: List<CommunityEvent>) {
        events.clear()
        events.addAll(newEvents)
        notifyDataSetChanged()
    }
}

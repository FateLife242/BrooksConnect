package com.example.brooksconnect

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RequestsAdapter(private var requests: List<Request>) : RecyclerView.Adapter<RequestsAdapter.ViewHolder>() {

    private var filteredRequests: List<Request> = requests

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = filteredRequests[position]
        holder.title.text = request.title
        holder.id.text = request.id
        holder.applicant.text = request.applicant
        holder.filedDate.text = request.filedDate
        holder.status.text = request.status

        when (request.status.lowercase()) {
            "pending" -> {
                holder.status.text = "PENDING"
                holder.status.setBackgroundResource(R.drawable.pending_chip_background)
                holder.status.setTextColor(holder.itemView.context.getColor(R.color.orange_500))
            }
            "processing" -> {
                holder.status.text = "PROCESSING"
                holder.status.setBackgroundResource(R.drawable.in_progress_chip_background)
                holder.status.setTextColor(holder.itemView.context.getColor(R.color.orange_500))
            }
            "ready" -> {
                holder.status.text = "READY"
                holder.status.setBackgroundResource(R.drawable.ready_chip_background)
                holder.status.setTextColor(holder.itemView.context.getColor(android.R.color.white))
            }
            "completed" -> {
                holder.status.text = "COMPLETED"
                holder.status.setBackgroundResource(R.drawable.completed_chip_background)
                holder.status.setTextColor(holder.itemView.context.getColor(android.R.color.white))
            }
            else -> {
                holder.status.text = request.status.uppercase()
                holder.status.setBackgroundResource(R.drawable.chip_unselected_background)
                holder.status.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, RequestDetailActivity::class.java)
            intent.putExtra("request", request)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = filteredRequests.size

    fun filter(status: String) {
        filteredRequests = if (status == "All") {
            requests
        } else {
            requests.filter { it.status.equals(status, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.request_title)
        val id: TextView = itemView.findViewById(R.id.request_id)
        val applicant: TextView = itemView.findViewById(R.id.request_applicant)
        val filedDate: TextView = itemView.findViewById(R.id.request_filed_date)
        val status: TextView = itemView.findViewById(R.id.request_status)
    }
}

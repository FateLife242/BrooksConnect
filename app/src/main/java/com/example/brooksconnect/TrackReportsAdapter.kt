package com.example.brooksconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TrackReportsAdapter(private var reports: List<TrackReport>) : RecyclerView.Adapter<TrackReportsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.report_title)
        val reportId: TextView = view.findViewById(R.id.report_id)
        val statusChip: TextView = view.findViewById(R.id.status_chip)
        val description: TextView = view.findViewById(R.id.report_description)
        val location: TextView = view.findViewById(R.id.report_location)
        val aiClassificationContainer: View = view.findViewById(R.id.ai_classification_container)
        val aiClassificationText: TextView = view.findViewById(R.id.ai_classification_text)
        val filedDate: TextView = view.findViewById(R.id.filed_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track_report, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = reports[position]

        holder.title.text = report.title
        holder.reportId.text = "Report ID: ${report.reportId}"
        holder.statusChip.text = report.status
        holder.description.text = report.description
        holder.location.text = report.location
        holder.filedDate.text = "Filed: ${report.filedDate}"

        if (report.aiClassification.isNotEmpty()) {
            holder.aiClassificationContainer.visibility = View.VISIBLE
            holder.aiClassificationText.text = report.aiClassification
        } else {
            holder.aiClassificationContainer.visibility = View.GONE
        }
    }

    override fun getItemCount() = reports.size

    fun updateReports(newReports: List<TrackReport>) {
        reports = newReports
        notifyDataSetChanged()
    }
}

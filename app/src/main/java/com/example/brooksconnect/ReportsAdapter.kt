package com.example.brooksconnect

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReportsAdapter(private var reports: List<Report>) : RecyclerView.Adapter<ReportsAdapter.ViewHolder>() {

    private var filteredReports: List<Report> = reports

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = filteredReports[position]
        holder.title.text = report.title
        holder.id.text = report.id
        holder.description.text = report.description
        holder.location.text = report.location
        holder.reporter.text = report.reporter
        holder.filedDate.text = report.filedDate
        holder.status.text = report.status

        // AI Insights
        val aiLayout = holder.itemView.findViewById<android.widget.LinearLayout>(R.id.ai_insights_layout)
        val aiClassText = holder.itemView.findViewById<TextView>(R.id.ai_classification_label)
        val aiPriorityText = holder.itemView.findViewById<TextView>(R.id.ai_priority_label)
        val aiActionChip = holder.itemView.findViewById<TextView>(R.id.ai_action_chip)

        val hasAiData = report.aiPriority.isNotEmpty() || report.aiClassification.isNotEmpty() || report.aiAction.isNotEmpty()

        if (hasAiData) {
            aiLayout.visibility = View.VISIBLE
            
            // Classification & Priority
            if (report.aiClassification.isNotEmpty() || report.aiPriority.isNotEmpty()) {
                aiClassText.visibility = View.VISIBLE
                aiPriorityText.visibility = View.VISIBLE
                aiClassText.text = "AI Classification: ${report.aiClassification.ifEmpty { "N/A" }}"
                aiPriorityText.text = "Suggested Priority: ${report.aiPriority.ifEmpty { "Normal" }}"
            } else {
                aiClassText.visibility = View.GONE
                aiPriorityText.visibility = View.GONE
            }

            // Action Chip
            if (report.aiAction.isNotEmpty()) {
                aiActionChip.visibility = View.VISIBLE
                aiActionChip.text = report.aiAction
            } else {
                aiActionChip.visibility = View.GONE
            }
        } else {
            aiLayout.visibility = View.GONE
        }

        when (report.status.lowercase()) {
            "in-progress" -> {
                holder.status.setBackgroundResource(R.drawable.in_progress_chip_background)
                holder.status.setTextColor(holder.itemView.context.getColor(R.color.orange_500))
            }
            "resolved" -> {
                holder.status.setBackgroundResource(R.drawable.completed_chip_background)
                holder.status.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
            }
            else -> {
                holder.status.setBackgroundResource(R.drawable.chip_unselected_background)
                holder.status.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ReportDetailActivity::class.java)
            intent.putExtra("report", report)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = filteredReports.size

    fun filter(status: String) {
        val targetStatus = if (status.equals("In Progress", ignoreCase = true)) "in-progress" else status
        
        filteredReports = if (status == "All") {
            reports
        } else {
            reports.filter { it.status.equals(targetStatus, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    fun updateReports(newReports: List<Report>) {
        reports = newReports
        filteredReports = newReports
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.report_title)
        val id: TextView = itemView.findViewById(R.id.report_id)
        val description: TextView = itemView.findViewById(R.id.report_description)
        val location: TextView = itemView.findViewById(R.id.report_location)
        val reporter: TextView = itemView.findViewById(R.id.report_reporter)
        val filedDate: TextView = itemView.findViewById(R.id.report_filed_date)
        val status: TextView = itemView.findViewById(R.id.report_status)
    }
}

package com.example.brooksconnect

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TrackRequestsAdapter(private var requests: List<TrackRequest>) : RecyclerView.Adapter<TrackRequestsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.request_title)
        val statusChip: TextView = view.findViewById(R.id.status_chip)
        val requestId: TextView = view.findViewById(R.id.request_id)
        val filedDate: TextView = view.findViewById(R.id.filed_date)
        val purpose: TextView = view.findViewById(R.id.purpose)
        val readyFooter: View = view.findViewById(R.id.ready_footer)

        val step1Icon: ImageView = view.findViewById(R.id.step1_icon)
        val step1Text: TextView = view.findViewById(R.id.step1_text)
        val step1Status: TextView = view.findViewById(R.id.step1_status)

        val step2Icon: ImageView = view.findViewById(R.id.step2_icon)
        val step2Text: TextView = view.findViewById(R.id.step2_text)
        val step2Status: TextView = view.findViewById(R.id.step2_status)

        val step3Icon: ImageView = view.findViewById(R.id.step3_icon)
        val step3Text: TextView = view.findViewById(R.id.step3_text)
        val step3Status: TextView = view.findViewById(R.id.step3_status)
        
        val step4Icon: ImageView = view.findViewById(R.id.step4_icon)
        val step4Text: TextView = view.findViewById(R.id.step4_text)
        val step4Status: TextView = view.findViewById(R.id.step4_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]

        holder.title.text = request.title
        holder.requestId.text = "Request ID: ${request.requestId}"
        holder.filedDate.text = "Filed: ${request.filedDate}"
        holder.purpose.text = "Purpose: ${request.purpose}"
        
        // Status Chip Logic
        holder.statusChip.text = request.status
        if (request.status == "Ready for Pickup") {
             holder.statusChip.setBackgroundResource(R.drawable.ready_chip_background)
             holder.statusChip.setTextColor(Color.parseColor("#2E7D32")) // Green
        } else {
             holder.statusChip.setBackgroundResource(R.drawable.in_progress_chip_background)
             holder.statusChip.setTextColor(Color.parseColor("#155DFC")) // Blue
        }

        // Footer Logic
        if (request.currentStep == 3) { // Ready for pickup
            holder.readyFooter.visibility = View.VISIBLE
        } else {
            holder.readyFooter.visibility = View.GONE
        }

        // Timeline Logic
        updateStep(holder.step1Icon, holder.step1Text, holder.step1Status, 1, request.currentStep)
        updateStep(holder.step2Icon, holder.step2Text, holder.step2Status, 2, request.currentStep)
        updateStep(holder.step3Icon, holder.step3Text, holder.step3Status, 3, request.currentStep)
        updateStep(holder.step4Icon, holder.step4Text, holder.step4Status, 4, request.currentStep)
    }

    private fun updateStep(icon: ImageView, text: TextView, statusText: TextView, stepIndex: Int, currentStep: Int) {
        if (stepIndex <= currentStep) {
            // Completed or Current
            if (stepIndex == currentStep) {
                // Current
                icon.setImageResource(R.drawable.ic_check) 
                icon.setColorFilter(Color.parseColor("#155DFC")) // Blue
                text.setTextColor(Color.BLACK)
                statusText.visibility = View.VISIBLE
            } else {
                // Completed (Past)
                icon.setImageResource(R.drawable.ic_check)
                icon.setColorFilter(Color.parseColor("#155DFC")) // Blue
                text.setTextColor(Color.BLACK)
                statusText.visibility = View.GONE
            }
        } else {
            // Future
            icon.setImageResource(R.drawable.ic_check)
            icon.setColorFilter(Color.LTGRAY) 
            text.setTextColor(Color.LTGRAY)
            statusText.visibility = View.GONE
        }
    }

    override fun getItemCount() = requests.size

    fun updateRequests(newRequests: List<TrackRequest>) {
        requests = newRequests
        notifyDataSetChanged()
    }
}

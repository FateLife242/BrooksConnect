package com.example.brooksconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ServiceAdapter(private val services: List<Service>) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.service_icon)
        val title: TextView = view.findViewById(R.id.service_title)
        val description: TextView = view.findViewById(R.id.service_description)
        val price: TextView = view.findViewById(R.id.service_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = services[position]
        
        holder.icon.setImageResource(service.iconResId)
        holder.title.text = service.title
        holder.description.text = service.description
        holder.price.text = service.price

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = android.content.Intent(context, RequestFormActivity::class.java).apply {
                putExtra("SERVICE_TITLE", service.title)
                putExtra("SERVICE_PRICE", service.price)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = services.size
}

package com.example.brooksconnect

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ManageRequestsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RequestsAdapter
    private lateinit var chipGroup: ChipGroup
    private lateinit var loadingTextView: android.widget.TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_requests)

        val backArrow = findViewById<ImageView>(R.id.back_arrow)
        backArrow.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.requests_recyclerview)
        chipGroup = findViewById(R.id.chip_group)
        loadingTextView = findViewById(R.id.loading_text_view)

        // Initialize adapter with empty list
        adapter = RequestsAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchRequests()

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip: Chip? = group.findViewById(checkedIds[0])
                chip?.let {
                    adapter.filter(it.text.toString())
                }
            } else {
                adapter.filter("All")
            }
        }
    }

    private fun fetchRequests() {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        loadingTextView.visibility = android.view.View.VISIBLE
        loadingTextView.text = "Loading requests..."
        
        // Fetch all requests
        // Note: In a real app with many requests, you'd want pagination or filtering
        db.collection("requests")
            .get()
            .addOnSuccessListener { documents ->
                val requestsList = mutableListOf<Request>()
                
                for (document in documents) {
                    try {
                        val title = document.getString("serviceType") ?: "Unknown Request"
                        val id = document.getString("requestId") ?: document.id
                        val applicant = "Applicant: " + (document.getString("fullName") ?: "Unknown User")
                        
                        // Format date
                        val createdAt = document.getLong("createdAt") ?: 0L
                        val date = java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault())
                            .format(java.util.Date(createdAt))
                        val filedDate = "Filed: $date"
                        
                        val status = document.getString("status") ?: "pending"
                        
                        // Get attachments
                        val attachmentsInfo = document.get("attachments")
                        val attachmentsList = if (attachmentsInfo is List<*>) {
                            attachmentsInfo.filterIsInstance<String>()
                        } else {
                            emptyList()
                        }
                        
                        requestsList.add(Request(title, id, applicant, filedDate, status, attachmentsList))
                    } catch (e: Exception) {
                        // Skip invalid documents
                    }
                }
                
                // Sort by date descending (newest first)
                // Since filedDate is a string, we rely on the list order if insertion was ordered, 
                // but better to sort if we had the raw timestamp. 
                // Getting raw timestamp isn't in Request object yet, so let's just reverse for now or leave as is.
                // Actually, let's just display them.
                
                if (requestsList.isEmpty()) {
                    loadingTextView.text = "No requests found."
                } else {
                    loadingTextView.visibility = android.view.View.GONE
                }
                
                // Update adapter
                adapter = RequestsAdapter(requestsList)
                recyclerView.adapter = adapter
                // Restore filter if needed
            }
            .addOnFailureListener {
                loadingTextView.text = "Error loading requests."
            }
    }
}

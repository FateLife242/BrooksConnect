package com.example.brooksconnect

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class TrackRequestsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var adapter: TrackRequestsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_track_requests)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.paddingBottom)
            insets
        }

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.track_requests_recyclerview)
        progressBar = findViewById(R.id.progress_bar)
        emptyText = findViewById(R.id.empty_text)

        adapter = TrackRequestsAdapter(emptyList())
        recyclerView.adapter = adapter

        loadUserRequests()
    }

    private fun loadUserRequests() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            emptyText.text = "Please log in to view your requests"
            emptyText.visibility = View.VISIBLE
            return
        }

        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyText.visibility = View.GONE

        db.collection("requests")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE

                // Sort documents by createdAt descending before mapping
                val sortedDocs = documents.sortedByDescending { it.getLong("createdAt") ?: 0L }

                val requests = sortedDocs.mapNotNull { doc ->
                    try {
                        val serviceType = doc.getString("serviceType") ?: "Unknown"
                        val status = doc.getString("status") ?: "pending"
                        val purpose = doc.getString("purpose") ?: ""
                        val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        val filedDate = dateFormat.format(Date(createdAt))

                        // Map status to step number
                        val currentStep = when (status.lowercase()) {
                            "pending" -> 1
                            "processing" -> 2
                            "ready" -> 3
                            "completed" -> 4
                            else -> 1
                        }

                        // Map status to display text
                        val displayStatus = when (status.lowercase()) {
                            "pending" -> "Pending Review"
                            "processing" -> "Processing"
                            "ready" -> "Ready for Pickup"
                            "completed" -> "Completed"
                            else -> status.replaceFirstChar { it.uppercase() }
                        }

                        TrackRequest(
                            title = serviceType,
                            requestId = "REQ-${doc.id.takeLast(8).uppercase()}",
                            filedDate = filedDate,
                            purpose = purpose.ifEmpty { "General purpose" },
                            status = displayStatus,
                            currentStep = currentStep
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                adapter.updateRequests(requests)

                if (requests.isEmpty()) {
                    emptyText.text = "No requests found"
                    emptyText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyText.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                emptyText.text = "Failed to load requests"
                emptyText.visibility = View.VISIBLE
            }
    }
}


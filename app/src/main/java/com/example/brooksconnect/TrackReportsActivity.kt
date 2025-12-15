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

class TrackReportsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var adapter: TrackReportsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_track_reports)
        
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

        recyclerView = findViewById(R.id.track_reports_recyclerview)
        progressBar = findViewById(R.id.progress_bar)
        emptyText = findViewById(R.id.empty_text)

        adapter = TrackReportsAdapter(emptyList())
        recyclerView.adapter = adapter

        loadUserReports()
    }

    private fun loadUserReports() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            emptyText.text = "Please log in to view your reports"
            emptyText.visibility = View.VISIBLE
            return
        }

        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyText.visibility = View.GONE

        db.collection("reports")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE

                // Sort documents by createdAt descending client-side to avoid index error
                val sortedDocs = documents.sortedByDescending { it.getLong("createdAt") ?: 0L }

                val reports = sortedDocs.mapNotNull { doc ->
                    try {
                        val category = doc.getString("category") ?: "Unknown"
                        val status = doc.getString("status") ?: "received"
                        val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        val description = doc.getString("description") ?: ""
                        val location = doc.getString("location") ?: ""

                        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                        val filedDate = dateFormat.format(Date(createdAt))

                        TrackReport(
                            title = category,
                            reportId = "REP-${doc.id.takeLast(8).uppercase()}",
                            description = description.ifEmpty { "Issue reported" },
                            location = location.ifEmpty { "Barangay Brookspoint" },
                            aiClassification = "", // Can be populated later with AI
                            filedDate = filedDate,
                            status = status.replaceFirstChar { it.uppercase() }
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                adapter.updateReports(reports)

                if (reports.isEmpty()) {
                    emptyText.text = "No reports found"
                    emptyText.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyText.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                emptyText.text = "Failed to load reports"
                emptyText.visibility = View.VISIBLE
            }
    }
}


package com.example.brooksconnect

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.back_button)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = v.layoutParams as android.view.ViewGroup.MarginLayoutParams
            params.topMargin = systemBars.top + 16.dpToPx(this)
            v.layoutParams = params
            insets
        }

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        // Logout button
        findViewById<android.view.View>(R.id.logout_button).setOnClickListener {
            auth.signOut()
            val intent = android.content.Intent(this, LoginActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

        // View All click handlers
        findViewById<TextView>(R.id.view_all_requests).setOnClickListener {
            startActivity(android.content.Intent(this, TrackRequestsActivity::class.java))
        }

        findViewById<TextView>(R.id.view_all_reports).setOnClickListener {
            startActivity(android.content.Intent(this, TrackReportsActivity::class.java))
        }



        // Load user profile data from Firestore
        loadUserProfile()
        loadActivitySummary()
        loadRecentRequests()
        loadRecentReports()
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to view profile", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "User"
                    val email = document.getString("email") ?: currentUser.email ?: ""
                    val phone = document.getString("phone") ?: ""
                    val address = document.getString("address") ?: ""

                    findViewById<TextView>(R.id.user_name).text = name
                    findViewById<TextView>(R.id.email_value).text = email
                    findViewById<TextView>(R.id.phone_value).text = phone
                    findViewById<TextView>(R.id.address_value).text = address
                } else {
                    val email = currentUser.email ?: ""
                    findViewById<TextView>(R.id.user_name).text = email.substringBefore("@")
                    findViewById<TextView>(R.id.email_value).text = email
                }
            }
            .addOnFailureListener { e ->
                val email = auth.currentUser?.email ?: ""
                findViewById<TextView>(R.id.user_name).text = email.substringBefore("@")
                findViewById<TextView>(R.id.email_value).text = email
            }
    }

    private fun loadActivitySummary() {
        val currentUser = auth.currentUser ?: return

        // Count requests
        db.collection("requests")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                findViewById<TextView>(R.id.requests_count).text = documents.size().toString()
            }
            .addOnFailureListener {
                findViewById<TextView>(R.id.requests_count).text = "0"
            }

        // Count reports
        db.collection("reports")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { documents ->
                findViewById<TextView>(R.id.reports_count).text = documents.size().toString()
            }
            .addOnFailureListener {
                findViewById<TextView>(R.id.reports_count).text = "0"
            }
    }

    private fun loadRecentRequests() {
        val currentUser = auth.currentUser ?: return
        val container = findViewById<LinearLayout>(R.id.recent_requests_container)
        container.removeAllViews()

        db.collection("requests")
            .whereEqualTo("userId", currentUser.uid)
            .limit(10) // Fetch a few more to sort, then take 3
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val emptyView = TextView(this).apply {
                        text = "No recent requests"
                        setTextColor(resources.getColor(android.R.color.darker_gray, null))
                        textSize = 14f
                    }
                    container.addView(emptyView)
                    return@addOnSuccessListener
                }

                // Client-side sort
                val sortedDocs = documents.sortedByDescending { it.getLong("createdAt") ?: 0L }
                    .take(3)

                for (document in sortedDocs) {
                    val title = document.getString("serviceType") ?: document.getString("title") ?: "Request"
                    val status = document.getString("status") ?: "pending"
                    val createdAt = document.getLong("createdAt")
                    val dateStr = if (createdAt != null) {
                        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date(createdAt))
                    } else "N/A"

                    addRequestItem(container, title, dateStr, status)
                }
            }
            .addOnFailureListener {
                val emptyView = TextView(this).apply {
                    text = "No recent requests"
                    setTextColor(resources.getColor(android.R.color.darker_gray, null))
                    textSize = 14f
                }
                container.addView(emptyView)
            }
    }

    private fun loadRecentReports() {
        val currentUser = auth.currentUser ?: return
        val container = findViewById<LinearLayout>(R.id.recent_reports_container)
        container.removeAllViews()

        db.collection("reports")
            .whereEqualTo("userId", currentUser.uid)
            .limit(10) // Fetch a few more to sort, then take 3
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val emptyView = TextView(this).apply {
                        text = "No recent reports"
                        setTextColor(resources.getColor(android.R.color.darker_gray, null))
                        textSize = 14f
                    }
                    container.addView(emptyView)
                    return@addOnSuccessListener
                }

                // Client-side sort
                val sortedDocs = documents.sortedByDescending { it.getLong("createdAt") ?: 0L }
                    .take(3)

                for (document in sortedDocs) {
                    val title = document.getString("category") ?: document.getString("title") ?: "Report"
                    val status = document.getString("status") ?: "received"
                    val createdAt = document.getLong("createdAt")
                    val dateStr = if (createdAt != null) {
                        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date(createdAt))
                    } else "N/A"

                    addReportItem(container, title, dateStr, status)
                }
            }
            .addOnFailureListener {
                val emptyView = TextView(this).apply {
                    text = "No recent reports"
                    setTextColor(resources.getColor(android.R.color.darker_gray, null))
                    textSize = 14f
                }
                container.addView(emptyView)
            }
    }

    private fun addRequestItem(container: LinearLayout, title: String, date: String, status: String) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_profile_activity, container, false)
        itemView.findViewById<TextView>(R.id.item_title).text = title
        itemView.findViewById<TextView>(R.id.item_date).text = date
        val statusView = itemView.findViewById<TextView>(R.id.item_status)
        statusView.text = status

        when (status.lowercase()) {
            "ready", "completed", "approved" -> {
                statusView.setBackgroundResource(R.drawable.status_ready_background)
                statusView.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
            }
            "processing", "pending" -> {
                statusView.setBackgroundResource(R.drawable.status_processing_background)
                statusView.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
            }
            else -> {
                statusView.setBackgroundResource(R.drawable.status_received_background)
                statusView.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
            }
        }

        container.addView(itemView)
    }

    private fun addReportItem(container: LinearLayout, title: String, date: String, status: String) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_profile_activity, container, false)
        itemView.findViewById<TextView>(R.id.item_title).text = title
        itemView.findViewById<TextView>(R.id.item_date).text = date
        val statusView = itemView.findViewById<TextView>(R.id.item_status)
        statusView.text = status

        when (status.lowercase()) {
            "resolved", "completed" -> {
                statusView.setBackgroundResource(R.drawable.status_ready_background)
                statusView.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
            }
            else -> {
                statusView.setBackgroundResource(R.drawable.status_received_background)
                statusView.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
            }
        }

        container.addView(itemView)
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}

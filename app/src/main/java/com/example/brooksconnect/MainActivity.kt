package com.example.brooksconnect

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.QuerySnapshot

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Hide system navigation bar for immersive experience
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        val header = findViewById<View>(R.id.header)
        val initialPaddingTop = header.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(header) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top + initialPaddingTop, v.paddingRight, v.paddingBottom)
            insets
        }

        // Set up RecyclerView
        val recycler = findViewById<RecyclerView>(R.id.recent_activity_recycler)
        recycler.layoutManager = LinearLayoutManager(this)

        // Load user name from Firebase
        loadUserName()

        val announcementsCard = findViewById<CardView>(R.id.announcements_card)
        announcementsCard.setOnClickListener {
            val intent = Intent(this, AnnouncementsActivity::class.java)
            startActivity(intent)
        }

        val requestsCard = findViewById<CardView>(R.id.requests_card)
        requestsCard.setOnClickListener {
            val intent = Intent(this, ServiceRequestsActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.report_issue_card).setOnClickListener {
            startActivity(Intent(this, ReportIssueActivity::class.java))
        }

        findViewById<View>(R.id.calendar_card).setOnClickListener {
            startActivity(Intent(this, EventCalendarActivity::class.java))
        }

        findViewById<View>(R.id.emergency_card).setOnClickListener {
            startActivity(Intent(this, EmergencyAssistanceActivity::class.java))
        }

        val bottomNavigation = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        try {
            bottomNavigation.itemActiveIndicatorColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
        } catch (e: Exception) {
            // Ignore if property is not supported or fails
        }
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_news -> {
                    startActivity(Intent(this, AnnouncementsActivity::class.java))
                    true
                }
                R.id.navigation_services -> {
                    startActivity(Intent(this, ServiceRequestsActivity::class.java))
                    true
                }
                R.id.navigation_home -> {
                     // Already on Home
                     true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserName() {
        val currentUser = auth.currentUser
        val userNameView = findViewById<TextView>(R.id.user_name)
        val profileInitialView = findViewById<TextView>(R.id.profile_initial)

        if (currentUser == null) {
            userNameView.text = "Guest"
            profileInitialView.text = "G"
            return
        }

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "User"
                    userNameView.text = name
                    profileInitialView.text = if (name.isNotEmpty()) name[0].uppercase() else "U"
                } else {
                    // Fallback to email
                    val email = currentUser.email ?: "User"
                    val displayName = email.substringBefore("@")
                    userNameView.text = displayName
                    profileInitialView.text = if (displayName.isNotEmpty()) displayName[0].uppercase() else "U"
                }
            }
            .addOnFailureListener {
                // Fallback to email on error
                val email = currentUser.email ?: "User"
                val displayName = email.substringBefore("@")
                userNameView.text = displayName
                profileInitialView.text = if (displayName.isNotEmpty()) displayName[0].uppercase() else "U"
            }
    }

    override fun onResume() {
        super.onResume()
        // Reset bottom navigation to Home when returning to this activity
        findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.navigation_home
        // Refresh user name in case it was updated
        loadUserName()
        // Refresh recent activity
        loadRecentActivity()
    }


    private fun loadRecentActivity() {
        val currentUser = auth.currentUser ?: return
        val recycler = findViewById<RecyclerView>(R.id.recent_activity_recycler)

        val reportsTask = db.collection("reports")
            .whereEqualTo("userId", currentUser.uid)
            .get()

        val requestsTask = db.collection("requests")
            .whereEqualTo("userId", currentUser.uid)
            .get()

        val tasks = Tasks.whenAllSuccess<QuerySnapshot>(reportsTask, requestsTask)
        
        tasks.addOnSuccessListener { results ->
            // Results is a List<QuerySnapshot>
            val reportDocs = results[0]
            val requestDocs = results[1]
            val recentItems = mutableListOf<RecentActivityItem>()

            for (doc in reportDocs) {
                val createdAt = doc.getLong("createdAt") ?: 0L
                recentItems.add(
                    RecentActivityItem(
                        title = "${doc.getString("category") ?: "Report"} Report",
                        status = doc.getString("status") ?: "Received",
                        date = createdAt,
                        type = ActivityType.REPORT,
                        id = doc.id
                    )
                )
            }

            for (doc in requestDocs) {
                val timestamp = doc.getLong("createdAt") ?: 0L
                recentItems.add(
                    RecentActivityItem(
                        title = "${doc.getString("serviceType") ?: "Service"} Request",
                        status = doc.getString("status") ?: "Pending",
                        date = timestamp,
                        type = ActivityType.REQUEST,
                        id = doc.id
                    )
                )
            }

            // Sort and Display (Top 3)
            recentItems.sortByDescending { it.date }
            val displayItems = recentItems.take(3)

            recycler.adapter = RecentActivityAdapter(displayItems) { item ->
                if (item.type == ActivityType.REPORT) {
                    startActivity(Intent(this, TrackReportsActivity::class.java))
                } else {
                    startActivity(Intent(this, TrackRequestsActivity::class.java))
                }
            }
        }.addOnFailureListener {
             // Handle errors or show empty state if needed.
             // For now, we just don't update the list (or could clear it).
        }
    }
}
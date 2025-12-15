package com.example.brooksconnect

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Update Posts count
        val postsCount = findViewById<TextView>(R.id.posts_count)
        val announcements = AnnouncementsRepository.getAnnouncements(this)
        postsCount.text = announcements.size.toString()

        val manageRequestsCard = findViewById<CardView>(R.id.manage_requests_card)
        manageRequestsCard.setOnClickListener {
            val intent = Intent(this, ManageRequestsActivity::class.java)
            startActivity(intent)
        }

        val manageReportsCard = findViewById<CardView>(R.id.manage_reports_card)
        manageReportsCard.setOnClickListener {
            val intent = Intent(this, ManageReportsActivity::class.java)
            startActivity(intent)
        }

        val announcementsCard = findViewById<CardView>(R.id.announcements_card)
        announcementsCard.setOnClickListener {
            val intent = Intent(this, AdminAnnouncementsActivity::class.java)
            startActivity(intent)
        }

        val analyticsCard = findViewById<CardView>(R.id.analytics_card)
        analyticsCard.setOnClickListener {
            val intent = Intent(this, AnalyticsActivity::class.java)
            startActivity(intent)
        }

        val recordsCard = findViewById<CardView>(R.id.records_card)
        recordsCard.setOnClickListener {
            val intent = Intent(this, RecordsActivity::class.java)
            startActivity(intent)
        }

        val postsCard = findViewById<CardView>(R.id.posts_card)
        postsCard.setOnClickListener {
            val intent = Intent(this, AdminAnnouncementsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Update Posts count when returning to this activity
        val postsCount = findViewById<TextView>(R.id.posts_count)
        val announcements = AnnouncementsRepository.getAnnouncements(this)
        postsCount.text = announcements.size.toString()
        
        updateActiveReportsCount()
        updatePendingRequestsCount()
        loadRecentActivity()
    }

    private fun loadRecentActivity() {
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.admin_recent_activity_recycler)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val dateFormat = java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault())
        
        // Parallel Fetching: Define tasks
        val reportsTask = db.collection("reports")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(5)
            .get()
            
        val requestsTask = db.collection("requests")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(5)
            .get()
            
        com.google.android.gms.tasks.Tasks.whenAllSuccess<com.google.firebase.firestore.QuerySnapshot>(reportsTask, requestsTask)
            .addOnSuccessListener { results ->
                val reportDocs = results[0].documents
                val requestDocs = results[1].documents
                
                // Collect unique User IDs
                val userIds = mutableSetOf<String>()
                reportDocs.forEach { it.getString("userId")?.let { id -> if(id.isNotEmpty()) userIds.add(id) } }
                requestDocs.forEach { it.getString("userId")?.let { id -> if(id.isNotEmpty()) userIds.add(id) } }
                
                // Fetch Users efficiently (Deduplicated)
                // Since Firestore doesn't have "whereIn" for document IDs easily for >10 items without batching logic,
                // and here we have max 10 items anyway, individual fetches in parallel is optimal enough if simple.
                
                val userNamesMap = mutableMapOf<String, String>()
                val userFetchTasks = userIds.map { userId ->
                    db.collection("users").document(userId).get()
                        .continueWith { task ->
                            if (task.isSuccessful) {
                                val name = task.result?.getString("name")
                                if (name != null) {
                                    synchronized(userNamesMap) {
                                        userNamesMap[userId] = name
                                    }
                                }
                            }
                        }
                }
                
                com.google.android.gms.tasks.Tasks.whenAllComplete(userFetchTasks)
                    .addOnSuccessListener {
                        val recentItems = mutableListOf<AdminRecentItem>()
                        
                        // Process Reports
                        for (doc in reportDocs) {
                            val userId = doc.getString("userId") ?: ""
                            val realName = userNamesMap[userId] ?: doc.getString("reporterName") ?: doc.getString("reporter") ?: "User"
                             // Fallback cleanup
                            val cleanName = if(realName.startsWith("Reporter: ")) realName.replace("Reporter: ", "") else realName
                            
                            val title = doc.getString("category") ?: "Report"
                            val filedDate = doc.getLong("createdAt") ?: 0L
                            val status = doc.getString("status") ?: "Received"
                            val attachments = (doc.get("attachments") as? List<*>)?.map { it.toString() } ?: emptyList()
                            
                            val report = Report(
                                title = title,
                                id = doc.id,
                                description = doc.getString("description") ?: "",
                                location = doc.getString("location") ?: "",
                                reporter = "Reporter: $cleanName",
                                filedDate = dateFormat.format(java.util.Date(filedDate)),
                                status = status,
                                attachments = attachments
                            )
                            
                            recentItems.add(AdminRecentItem(
                                title = title,
                                userName = cleanName,
                                status = status,
                                date = filedDate,
                                type = ActivityType.REPORT,
                                id = doc.id,
                                report = report
                            ))
                        }
                        
                        // Process Requests
                        for (doc in requestDocs) {
                            val userId = doc.getString("userId") ?: ""
                            // Prioritize: 1. Live Profile (if perm) 2. Saved Registered Name 3. Saved Input Name 4. Legacy
                            val realName = userNamesMap[userId] ?: doc.getString("registeredName") ?: doc.getString("fullName") ?: doc.getString("applicant") ?: "User"
                            val cleanName = if(realName.startsWith("Applicant: ")) realName.replace("Applicant: ", "") else realName
                            
                            val title = doc.getString("serviceType") ?: "Request"
                            val filedDate = doc.getLong("createdAt") ?: 0L
                            val status = doc.getString("status") ?: "Pending"
                            val attachments = (doc.get("attachments") as? List<*>)?.map { it.toString() } ?: emptyList()
                            
                            val request = Request(
                                title = title,
                                id = doc.id,
                                applicant = "Applicant: $cleanName",
                                filedDate = dateFormat.format(java.util.Date(filedDate)),
                                status = status,
                                attachments = attachments
                            )
                            
                            recentItems.add(AdminRecentItem(
                                title = title,
                                userName = cleanName,
                                status = status,
                                date = filedDate,
                                type = ActivityType.REQUEST,
                                id = doc.id,
                                request = request
                            ))
                        }
                        
                        recentItems.sortByDescending { it.date }
                        recyclerView.adapter = AdminRecentActivityAdapter(recentItems.take(5)) { item ->
                            if (item.type == ActivityType.REPORT && item.report != null) {
                                val intent = Intent(this, ReportDetailActivity::class.java)
                                intent.putExtra("report", item.report)
                                startActivity(intent)
                            } else if (item.type == ActivityType.REQUEST && item.request != null) {
                                val intent = Intent(this, RequestDetailActivity::class.java)
                                intent.putExtra("request", item.request)
                                startActivity(intent)
                            }
                        }
                    }
            }
            .addOnFailureListener {
                 // Handle failure gracefully
            }
    }

    private fun updatePendingRequestsCount() {
        val pendingCountView = findViewById<TextView>(R.id.pending_requests_count)
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        db.collection("requests")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                pendingCountView.text = documents.size().toString()
            }
            .addOnFailureListener {
                pendingCountView.text = "-"
            }
    }

    private fun updateActiveReportsCount() {
        val activeReportsCountView = findViewById<TextView>(R.id.active_reports_count)
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        db.collection("reports")
            .get()
            .addOnSuccessListener { documents ->
                var activeCount = 0
                for (document in documents) {
                    val status = document.getString("status")?.lowercase() ?: "received"
                    if (status != "resolved") {
                        activeCount++
                    }
                }
                activeReportsCountView.text = activeCount.toString()
            }
            .addOnFailureListener {
                activeReportsCountView.text = "-"
            }
    }
}

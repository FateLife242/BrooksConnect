package com.example.brooksconnect

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import android.content.Intent

class RecordsActivity : AppCompatActivity() {

    private lateinit var announcementsRecyclerView: RecyclerView
    private lateinit var announcementsAdapter: AnnouncementsRecordAdapter
    private lateinit var sectionTitle: TextView
    private lateinit var sectionCount: TextView
    private lateinit var emptyState: View
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_records)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top + 16.dpToPx(this), v.paddingRight, v.paddingBottom)
            insets
        }

        val backArrow = findViewById<ImageView>(R.id.back_arrow)
        backArrow.setOnClickListener {
            val intent = Intent(this, AdminActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        announcementsRecyclerView = findViewById(R.id.announcements_recyclerview)
        sectionTitle = findViewById(R.id.section_title)
        sectionCount = findViewById(R.id.section_count)
        emptyState = findViewById(R.id.empty_state)
        emptyText = findViewById(R.id.empty_text)

        val tabGroup = findViewById<MaterialButtonToggleGroup>(R.id.tab_group)
        val requestsTab = findViewById<MaterialButton>(R.id.tab_requests)
        val reportsTab = findViewById<MaterialButton>(R.id.tab_reports)
        val postsTab = findViewById<MaterialButton>(R.id.tab_posts)

        // Update button styles when selected/unselected
        tabGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.tab_requests -> {
                        updateTabStyle(requestsTab, true)
                        updateTabStyle(reportsTab, false)
                        updateTabStyle(postsTab, false)
                        showRequestsView()
                    }
                    R.id.tab_reports -> {
                        updateTabStyle(requestsTab, false)
                        updateTabStyle(reportsTab, true)
                        updateTabStyle(postsTab, false)
                        showReportsView()
                    }
                    R.id.tab_posts -> {
                        updateTabStyle(requestsTab, false)
                        updateTabStyle(reportsTab, false)
                        updateTabStyle(postsTab, true)
                        showPostsView()
                    }
                }
            }
        }

        // Set initial state
        updateTabStyle(requestsTab, true)
        updateTabStyle(reportsTab, false)
        updateTabStyle(postsTab, false)
        showRequestsView()
    }

    private fun showRequestsView() {
        sectionTitle.text = "Completed Requests"
        announcementsRecyclerView.visibility = View.GONE
        emptyState.visibility = View.GONE
        
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("requests")
            .whereEqualTo("status", "completed")
            .get()
            .addOnSuccessListener { documents ->
                val requests = documents.map { doc ->
                    val filedDate = doc.getLong("createdAt") ?: 0L
                    val dateString = java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault()).format(java.util.Date(filedDate))
                    
                    TrackRequest(
                        title = doc.getString("serviceType") ?: "Request",
                        requestId = doc.id,
                        filedDate = dateString,
                        purpose = doc.getString("purpose") ?: "N/A",
                        status = "Completed",
                        currentStep = 4 // Completed
                    )
                }

                sectionCount.text = "${requests.size} total"
                
                if (requests.isEmpty()) {
                    announcementsRecyclerView.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                    emptyText.text = "No completed requests found"
                } else {
                    emptyState.visibility = View.GONE
                    announcementsRecyclerView.visibility = View.VISIBLE
                    announcementsRecyclerView.layoutManager = LinearLayoutManager(this)
                    announcementsRecyclerView.adapter = TrackRequestsAdapter(requests)
                }
            }
            .addOnFailureListener {
                sectionCount.text = "0 total"
                emptyState.visibility = View.VISIBLE
                emptyText.text = "Failed to load requests"
            }
    }

    private fun showReportsView() {
        sectionTitle.text = "Completed Reports"
        announcementsRecyclerView.visibility = View.GONE
        emptyState.visibility = View.GONE
        
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("reports")
            .whereEqualTo("status", "resolved")
            .get()
            .addOnSuccessListener { documents ->
                val reports = documents.map { doc ->
                    val filedDate = doc.getLong("createdAt") ?: 0L
                    val dateString = java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault()).format(java.util.Date(filedDate))

                    TrackReport(
                        title = doc.getString("category") ?: "Report",
                        reportId = doc.id,
                        description = doc.getString("description") ?: "",
                        location = doc.getString("location") ?: "",
                        aiClassification = "", // Not stored or computed? Leave empty or fetch
                        filedDate = dateString,
                        status = "Resolved"
                    )
                }
                
                sectionCount.text = "${reports.size} total"

                if (reports.isEmpty()) {
                    announcementsRecyclerView.visibility = View.GONE
                    emptyState.visibility = View.VISIBLE
                    emptyText.text = "No completed reports found"
                } else {
                    emptyState.visibility = View.GONE
                    announcementsRecyclerView.visibility = View.VISIBLE
                    announcementsRecyclerView.layoutManager = LinearLayoutManager(this)
                    announcementsRecyclerView.adapter = TrackReportsAdapter(reports)
                }
            }
            .addOnFailureListener {
                 sectionCount.text = "0 total"
                 emptyState.visibility = View.VISIBLE
                 emptyText.text = "Failed to load reports"
            }
    }

    private fun showPostsView() {
        sectionTitle.text = "All Announcements"
        val announcements = AnnouncementsRepository.getAnnouncements(this)
        sectionCount.text = "${announcements.size} total"
        
        if (announcements.isEmpty()) {
            announcementsRecyclerView.visibility = View.GONE
            emptyState.visibility = View.VISIBLE
            emptyText.text = "No announcements found"
        } else {
            emptyState.visibility = View.GONE
            announcementsRecyclerView.visibility = View.VISIBLE
            
            // Reuse logic or create new adapter
            // Note: Reuse existing adapter variable but check type if we want (but Kotlin is type safe, reusing lateinit might be tricky if we change types?)
            // Actually `announcementsAdapter` is explicit property `AnnouncementsRecordAdapter`.
            // RecycleView adapter is `RecyclerView.Adapter<*>?`.
            // So we can assign any adapter to the RV.
            
            announcementsRecyclerView.layoutManager = LinearLayoutManager(this)
            announcementsRecyclerView.adapter = AnnouncementsRecordAdapter(announcements)
        }
    }

    private fun updateTabStyle(button: MaterialButton, isSelected: Boolean) {
        if (isSelected) {
            button.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue_500))
            button.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            button.iconTint = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
        } else {
            button.backgroundTintList = ColorStateList.valueOf(0xFFEEEEEE.toInt())
            button.setTextColor(0xFF757575.toInt())
            button.iconTint = ColorStateList.valueOf(0xFF757575.toInt())
        }
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}


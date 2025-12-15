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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class ManageReportsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReportsAdapter
    private lateinit var chipGroup: ChipGroup
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var db: FirebaseFirestore
    private var allReports: List<Report> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_reports)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top + 16.dpToPx(this), v.paddingRight, v.paddingBottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        val backArrow = findViewById<ImageView>(R.id.back_arrow)
        backArrow.setOnClickListener {
            val intent = android.content.Intent(this, AdminActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        recyclerView = findViewById(R.id.reports_recyclerview)
        chipGroup = findViewById(R.id.chip_group)
        progressBar = findViewById(R.id.progress_bar)
        emptyText = findViewById(R.id.empty_text)

        adapter = ReportsAdapter(emptyList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

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
    
    override fun onResume() {
        super.onResume()
        loadReportsFromFirestore()
    }

    private fun loadReportsFromFirestore() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyText.visibility = View.GONE

        db.collection("reports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE

                val reports = documents.mapNotNull { doc ->
                    try {
                        val category = doc.getString("category") ?: "Unknown"
                        val status = doc.getString("status") ?: "received"
                        val userEmail = doc.getString("userEmail") ?: "Unknown"
                        val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        val description = doc.getString("description") ?: ""
                        val location = doc.getString("location") ?: ""

                        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                        val filedDate = "Filed: ${dateFormat.format(Date(createdAt))}"
                        val aiPriority = doc.getString("aiPriority") ?: ""
                        val aiClassification = doc.getString("aiClassification") ?: ""
                        val aiAction = doc.getString("aiAction") ?: ""

                        Report(
                            title = category,
                            id = doc.id,
                            description = description.ifEmpty { "Issue reported by resident" },
                            location = location.ifEmpty { "Barangay Brookspoint" },
                            reporter = "Reporter: $userEmail",
                            filedDate = filedDate,
                            status = status,
                            attachments = doc.get("attachments") as? List<String> ?: emptyList(),
                            aiPriority = aiPriority,
                            aiClassification = aiClassification,
                            aiAction = aiAction
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                allReports = reports
                adapter.updateReports(reports)

                if (reports.isEmpty()) {
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

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}


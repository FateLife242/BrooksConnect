package com.example.brooksconnect

import android.os.Bundle
import android.content.Intent
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.brooksconnect.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Calendar
import java.util.Locale

class AnalyticsActivity : AppCompatActivity() {
    
    // TODO: Replace with your actual API Key from https://aistudio.google.com/
    // It is recommended to put this in local.properties or BuildConfig for security
    private val GEMINI_API_KEY = "AIzaSyDv6PK9tqsEkhJNu4EYKOMe1cJN96xJDww"
    
    private val db = FirebaseFirestore.getInstance()
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_analytics)

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

        loadAnalyticsData()
    }

    override fun onResume() {
        super.onResume()
        loadAnalyticsData()
    }

    private fun loadAnalyticsData() {
        loadRequestsData()
        loadReportsData()
    }

    private fun loadRequestsData() {
        val requestsCountView = findViewById<TextView>(R.id.requests_count)
        val requestsStatusView = findViewById<TextView>(R.id.requests_status)
        val serviceNameView = findViewById<TextView>(R.id.service_name)
        val servicePercentageView = findViewById<TextView>(R.id.service_percentage)
        val serviceProgressView = findViewById<ProgressBar>(R.id.service_progress)
        val completionRateView = findViewById<TextView>(R.id.completion_rate_value)
        val processTimeView = findViewById<TextView>(R.id.process_time_value)
        
        // Insight Views
        val peakDayTitle = findViewById<TextView>(R.id.peak_day_title)
        val peakDayDesc = findViewById<TextView>(R.id.peak_day_desc)
        val processTrendTitle = findViewById<TextView>(R.id.process_trend_title)
        val processTrendDesc = findViewById<TextView>(R.id.process_trend_desc)

        db.collection("requests")
            .get()
            .addOnSuccessListener { documents ->
                val totalRequests = documents.size()
                var completedCount = 0
                val serviceTypeCounts = mutableMapOf<String, Int>()
                var totalProcessingTimeMs = 0L
                var processedCount = 0
                val dayCounts = mutableMapOf<Int, Int>()

                for (document in documents) {
                    val status = document.getString("status")?.lowercase() ?: ""
                    if (status == "completed" || status == "approved" || status == "released") {
                        completedCount++
                        
                        // Calculate processing time for completed requests
                        val createdAt = document.getLong("createdAt") ?: 0L
                        val completedAt = document.getLong("completedAt") 
                            ?: document.getLong("updatedAt") 
                            ?: 0L
                        if (createdAt > 0 && completedAt > createdAt) {
                            totalProcessingTimeMs += (completedAt - createdAt)
                            processedCount++
                        }
                    }

                    // Count service types
                    val serviceType = document.getString("serviceType") ?: "Other"
                    serviceTypeCounts[serviceType] = (serviceTypeCounts[serviceType] ?: 0) + 1
                    
                    // Count days for Peak Period Insight
                    val createdAt = document.getLong("createdAt") ?: 0L
                    if (createdAt > 0) {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = createdAt
                        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        dayCounts[dayOfWeek] = (dayCounts[dayOfWeek] ?: 0) + 1
                    }
                }

                // Update Total Requests
                requestsCountView.text = totalRequests.toString()
                requestsStatusView.text = "$completedCount completed"

                // Update Completion Rate
                val completionRate = if (totalRequests > 0) {
                    (completedCount * 100 / totalRequests)
                } else 0
                completionRateView.text = "$completionRate%"

                // Update Avg. Process Time
                val avgDays = if (processedCount > 0) {
                    val avgTimeMs = totalProcessingTimeMs / processedCount
                    avgTimeMs / (1000 * 60 * 60 * 24).toDouble()
                } else 0.0

                if (processedCount > 0) {
                    processTimeView.text = String.format("%.1fd", avgDays)
                } else {
                    processTimeView.text = "—"
                }

                // Update Most Requested Service
                if (serviceTypeCounts.isNotEmpty()) {
                    val topService = serviceTypeCounts.maxByOrNull { it.value }
                    if (topService != null) {
                        serviceNameView.text = topService.key
                        val percentage = if (totalRequests > 0) {
                            (topService.value * 100 / totalRequests)
                        } else 0
                        servicePercentageView.text = "${topService.value} ($percentage%)"
                        serviceProgressView.progress = percentage
                    }
                } else {
                    serviceNameView.text = "No data"
                    servicePercentageView.text = "—"
                    serviceProgressView.progress = 0
                }
                
                // --- UI UPDATE: AI Insights (Request Based) ---
                
                // Insight 1: Peak Period
                if (dayCounts.isNotEmpty()) {
                    val peakDayEntry = dayCounts.maxByOrNull { it.value }
                    val peakDayName = getDayName(peakDayEntry?.key ?: 1)
                    peakDayTitle.text = "Peak Request Period: $peakDayName"
                    
                    // Use Gemini for the description
                    val prompt = "The peak day for service requests is $peakDayName. Write a 1-sentence strategic advice for a barangay admin."
                    generateInsight(prompt, peakDayDesc, "Most requests are filed on ${peakDayName}s. Consider increasing staff availability.")
                } else {
                    peakDayTitle.text = "Peak Request Period"
                    peakDayDesc.text = "Not enough data to analyze peak periods yet."
                }

                // Insight 3: Processing Time Analysis
                if (avgDays > 0) {
                    processTrendTitle.text = "Processing Efficiency"
                    
                    val status = if(avgDays < 3.0) "good" else "slow"
                    val prompt = "The average processing time is ${String.format("%.1f", avgDays)} days, which is considered $status. Write a 1-sentence encouraging feedback for the staff."
                    
                    val fallback = if (avgDays < 3.0) {
                        "Great job! Average processing time is under 3 days (${String.format("%.1f", avgDays)}d). Keep it up!"
                    } else {
                         "Average time is ${String.format("%.1f", avgDays)}d. Aim to reduce this to under 3 days."
                    }
                    generateInsight(prompt, processTrendDesc, fallback)
                } else {
                    processTrendTitle.text = "Processing Efficiency"
                    processTrendDesc.text = "Complete more requests to unlock efficiency insights."
                }
            }
            .addOnFailureListener {
                requestsCountView.text = "—"
                requestsStatusView.text = "Error loading"
            }
    }

    private fun loadReportsData() {
        val reportsCountView = findViewById<TextView>(R.id.reports_count)
        val reportsStatusView = findViewById<TextView>(R.id.reports_status)
        val issueNameView = findViewById<TextView>(R.id.issue_name)
        val issuePercentageView = findViewById<TextView>(R.id.issue_percentage)
        val issueProgressView = findViewById<ProgressBar>(R.id.issue_progress)
        val resolutionRateView = findViewById<TextView>(R.id.resolution_rate_value)
        
        // Insight Views
        val commonIssueTitle = findViewById<TextView>(R.id.common_issue_title)
        val commonIssueDesc = findViewById<TextView>(R.id.common_issue_desc)

        db.collection("reports")
            .get()
            .addOnSuccessListener { documents ->
                val totalReports = documents.size()
                var resolvedCount = 0
                val categoryCounts = mutableMapOf<String, Int>()

                for (document in documents) {
                    val status = document.getString("status")?.lowercase() ?: ""
                    if (status == "resolved") {
                        resolvedCount++
                    }

                    // Count categories
                    val category = document.getString("category") ?: "Other"
                    categoryCounts[category] = (categoryCounts[category] ?: 0) + 1
                }

                // Update Total Reports
                reportsCountView.text = totalReports.toString()
                reportsStatusView.text = "$resolvedCount resolved"

                // Update Resolution Rate
                val resolutionRate = if (totalReports > 0) {
                    (resolvedCount * 100 / totalReports)
                } else 0
                resolutionRateView.text = "$resolutionRate%"

                // Update Most Reported Issue
                if (categoryCounts.isNotEmpty()) {
                    val topCategoryEntry = categoryCounts.maxByOrNull { it.value }
                    if (topCategoryEntry != null) {
                        val topCategory = topCategoryEntry.key
                        val percentage = if (totalReports > 0) {
                            (topCategoryEntry.value * 100 / totalReports)
                        } else 0

                        issueNameView.text = topCategory
                        issuePercentageView.text = "${topCategoryEntry.value} ($percentage%)"
                        issueProgressView.progress = percentage
                        
                        // Update AI Insight Card
                        commonIssueTitle.text = "Pattern: $topCategory"
                       
                        val prompt = "The most common issue reported in the barangay is '$topCategory' (${percentage}% of total). Write a 1-sentence proactive recommendation for the admin."
                        val fallback = "$topCategory reports are spiking (${percentage}% of total). ${getRecommendationForCategory(topCategory)}"
                        
                        generateInsight(prompt, commonIssueDesc, fallback)
                        
                    }
                } else {
                    issueNameView.text = "No data"
                    issuePercentageView.text = "—"
                    issueProgressView.progress = 0
                    
                    commonIssueTitle.text = "Common Issue Pattern"
                    commonIssueDesc.text = "Not enough data to analyze issue patterns yet."
                }
            }
            .addOnFailureListener {
                reportsCountView.text = "—"
                reportsStatusView.text = "Error loading"
            }
    }
    
    private fun generateInsight(prompt: String, textView: TextView, fallbackText: String) {
        // Set fallback first so user sees something immediately
        textView.text = fallbackText
        
        if (GEMINI_API_KEY == "YOUR_API_KEY_HERE") {
            // If no key, just keep the fallback
            return
        }

        // Check Persistent Cache
        val cacheKey = "insight_" + prompt.hashCode()
        val sharedPrefs = getSharedPreferences("ai_insights_cache", MODE_PRIVATE)
        val cachedInsight = sharedPrefs.getString(cacheKey, null)
        
        if (cachedInsight != null) {
            textView.text = cachedInsight
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // BUILD JSON manually for REST API
                val jsonBody = org.json.JSONObject().apply {
                    put("contents", org.json.JSONArray().apply {
                        put(org.json.JSONObject().apply {
                            put("parts", org.json.JSONArray().apply {
                                put(org.json.JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                }.toString()

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=$GEMINI_API_KEY")
                    .addHeader("Content-Type", "application/json")
                    .post(jsonBody.toRequestBody("application/json".toMediaTypeOrNull()))
                    .build()

                val response = client.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    // Parse Gemini Response
                    try {
                        val jsonResponse = org.json.JSONObject(responseBody)
                        val text = jsonResponse.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")
                            .trim()

                        withContext(Dispatchers.Main) {
                            textView.text = text
                            // Save to Cache
                            sharedPrefs.edit().putString(cacheKey, text).apply()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                     // Log error
                     val errorBody = response.body?.string() ?: ""
                     // println("Analytics Gemini Error: $errorBody")
                }
            } catch (e: Exception) {
                // If AI fails (no internet, quota exceeded), we already have the fallback
                e.printStackTrace()
            }
        }
    }
    
    private fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Unknown"
        }
    }

    private fun getRecommendationForCategory(category: String): String {
        return when (category.lowercase(Locale.ROOT)) {
            "noise complaint" -> "Consider implementing stricter noise ordinances or community mediation programs."
            "waste management" -> "Organize community clean-up drives and promote proper waste segregation."
            "road damage" -> "Coordinate with local engineering offices for road repairs and maintenance."
            "security concern" -> "Increase police visibility or establish neighborhood watch programs."
            else -> "Investigate the root causes and develop targeted solutions."
        }
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}

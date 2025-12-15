package com.example.brooksconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import coil.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Locale
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.preference.PreferenceManager
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportDetailActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Important for OSMDroid
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        
        setContentView(R.layout.activity_report_detail)

        val report = intent.getParcelableExtra<Report>("report")
        if (report == null) {
            finish()
            return
        }


        val aiClassificationChip = findViewById<TextView>(R.id.ai_classification_chip)
        val issueDescription = findViewById<TextView>(R.id.issue_description)
        val locationText = findViewById<TextView>(R.id.location_text)
        val viewOnMap = findViewById<TextView>(R.id.view_on_map)
        val reporterName = findViewById<TextView>(R.id.reporter_name)
        val dateReported = findViewById<TextView>(R.id.date_reported)
        val noteInput = findViewById<TextInputEditText>(R.id.note_input)
        val addNoteButton = findViewById<MaterialButton>(R.id.add_note_button)
        val notesContainer = findViewById<LinearLayout>(R.id.notes_container)
        
        // Status Action Buttons
        val actionButton = findViewById<MaterialButton>(R.id.action_button)
        val secondaryButton = findViewById<MaterialButton>(R.id.secondary_button)
        
        // Header Info
        val reportTitleView = findViewById<TextView>(R.id.report_title)
        val reportIdView = findViewById<TextView>(R.id.report_id)
        val statusChip = findViewById<TextView>(R.id.status_chip)

        val closeButton = findViewById<ImageView>(R.id.close_button)
        closeButton.setOnClickListener { finish() }

        // Set Header Data
        reportTitleView.text = report.title
        reportIdView.text = "Report ID: ${report.id.takeLast(8).uppercase()}"
        statusChip.text = report.status
        
        // Style Status Chip
        when (report.status.lowercase()) {
            "in-progress" -> {
                statusChip.setBackgroundResource(R.drawable.in_progress_chip_background)
                statusChip.setTextColor(ContextCompat.getColor(this, R.color.orange_500))
            }
            "resolved" -> {
                statusChip.setBackgroundResource(R.drawable.completed_chip_background)
                statusChip.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray)) // Or Green if available
            }
            else -> {
                statusChip.setBackgroundResource(R.drawable.chip_unselected_background)
                statusChip.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
            }
        }

        // Set AI Classification (default based on report title)
        aiClassificationChip.text = when (report.title.lowercase()) {
            "road issue" -> "Road Maintenance Required"
            "water leak" -> "Water System Repair Required"
            "fallen tree" -> "Emergency Cleanup Required"
            else -> "Maintenance Required"
        }
        
        // --- GEMINI AI INTEGRATION ---
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiKey = "AIzaSyDv7pG4Cw9Senb47djsqRAvx368bfAzqFM" // Using same key
                val generativeModel = GenerativeModel(modelName = "gemini-pro", apiKey = apiKey)
                
                val prompt = """
                    Analyze this issue report description: "${report.description}"
                    The text may be in English, Filipino (Tagalog), or Taglish.
                    Classify it into a short 2-3 word English category for staff (e.g., "Road Repair", "Waste Collection", "Noise Control").
                    Return ONLY the category name.
                """.trimIndent()
                
                val response = generativeModel.generateContent(prompt)
                val aiCategory = response.text?.trim() ?: ""
                
                if (aiCategory.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        aiClassificationChip.text = "AI Classification: $aiCategory"
                        // Optional: Change color to show it's AI
                        aiClassificationChip.setTextColor(ContextCompat.getColor(this@ReportDetailActivity, R.color.purple_500))
                    }
                }
            } catch (e: Exception) {
                // Keep default if AI fails
                e.printStackTrace()
            }
        }

        // Set report data
        issueDescription.text = report.description
        locationText.text = report.location
        
        // Extract reporter name from "Reporter: Maria Santos"
        val reporterNameText = report.reporter.replace("Reporter: ", "")
        reporterName.text = reporterNameText
        
        // Format date from "Filed: 11/27/2024" to "November 27, 2024"
        val dateText = report.filedDate.replace("Filed: ", "")
        dateReported.text = formatDate(dateText)
        
        // Attachments
        val attachmentImage = findViewById<ImageView>(R.id.attachment_image)
        val noAttachmentsText = findViewById<TextView>(R.id.no_attachments_text)
        
        if (report.attachments.isNotEmpty()) {
            val imageUrl = report.attachments[0] // Load first attachment
            noAttachmentsText.visibility = View.GONE
            attachmentImage.visibility = View.VISIBLE
            
            val isPdf = imageUrl.lowercase(Locale.ROOT).endsWith(".pdf")

            if (isPdf) {
                attachmentImage.setImageResource(android.R.drawable.ic_menu_sort_by_size)
                attachmentImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
                attachmentImage.setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
            } else {
                attachmentImage.load(imageUrl) {
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.stat_notify_error)
                }
                attachmentImage.scaleType = ImageView.ScaleType.CENTER_CROP
            }

            attachmentImage.setOnClickListener {
                 val intent = Intent(Intent.ACTION_VIEW)
                 intent.data = Uri.parse(imageUrl)
                 intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                 try {
                     startActivity(intent)
                 } catch (e: Exception) {
                     Toast.makeText(this, "No app found to open this file", Toast.LENGTH_SHORT).show()
                 }
            }
        } else {
            attachmentImage.visibility = View.GONE
            noAttachmentsText.visibility = View.VISIBLE
        }

        // Handle View on Map click
        viewOnMap.setOnClickListener {
             if (report.location.contains("(") && report.location.contains(")")) {
                 try {
                     val parts = report.location.substringAfterLast("(").replace(")", "").split(",")
                     val lat = parts[0].trim().toDouble()
                     val lon = parts[1].trim().toDouble()
                     val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon")
                     startActivity(Intent(Intent.ACTION_VIEW, uri))
                 } catch (e: Exception) {
                     Toast.makeText(this, "Location format invalid", Toast.LENGTH_SHORT).show()
                 }
             } else {
                 Toast.makeText(this, "No coordinates found", Toast.LENGTH_SHORT).show()
             }
        }
        
        // Initialize Map in Detail View
        val mapView = findViewById<MapView>(R.id.detail_map)
        if (report.location.contains("(") && report.location.contains(")")) {
            try {
                // Parse "Address (Lat, Lon)"
                val parts = report.location.substringAfterLast("(").replace(")", "").split(",")
                val lat = parts[0].trim().toDouble()
                val lon = parts[1].trim().toDouble()
                
                mapView.visibility = View.VISIBLE
                mapView.setTileSource(TileSourceFactory.MAPNIK)
                mapView.setMultiTouchControls(true)
                mapView.setBuiltInZoomControls(true)
                mapView.controller.setZoom(17.0)
                
                val point = GeoPoint(lat, lon)
                mapView.controller.setCenter(point)
                
                val marker = Marker(mapView)
                marker.position = point
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = report.location.substringBeforeLast("(")
                mapView.overlays.add(marker)
                
            } catch (e: Exception) {
                mapView.visibility = View.GONE
                findViewById<TextView>(R.id.map_placeholder).visibility = View.VISIBLE
            }
        } else {
             mapView.visibility = View.GONE // or show placeholder
        }

        // Handle Add Note button
        addNoteButton.setOnClickListener {
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (user == null) {
                android.widget.Toast.makeText(this, "You must be logged in to add a note", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            user.getIdToken(false).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val claims = task.result?.claims
                    val isAdmin = claims?.get("admin") as? Boolean ?: false

                    if (isAdmin) {
                        noteInput.text?.toString()?.trim()?.let { noteText ->
                            if (noteText.isNotEmpty()) {
                                NotesRepository.addNote(this, report.id, noteText)
                                val noteCard = createNoteCard(noteText)
                                notesContainer.addView(noteCard)
                                noteInput.text?.clear()
                            }
                        }
                    } else {
                        android.widget.Toast.makeText(this, "Only admins can add notes", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    android.widget.Toast.makeText(this, "Failed to verify permissions", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Handle Status Workflow
        setupActionButtons(actionButton, secondaryButton, report)
        
        // Load existing notes from repository
        val savedNotes = NotesRepository.getNotes(this, report.id)
        savedNotes.forEach { note ->
            val noteCard = createNoteCard(note)
            notesContainer.addView(noteCard)
        }
    }

    private fun createNoteCard(note: String): CardView {
        val cardView = CardView(this)
        val margin = (16 * resources.displayMetrics.density).toInt()
        val padding = (12 * resources.displayMetrics.density).toInt()
        
        cardView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = margin
        }
        cardView.radius = 12f
        cardView.cardElevation = 1f
        cardView.setCardBackgroundColor(0xFFE7F0FF.toInt())

        val textView = TextView(this)
        textView.text = note
        textView.setTextColor(0xFF155dfc.toInt())
        textView.textSize = 14f
        textView.setPadding(padding, padding, padding, padding)

        cardView.addView(textView)
        return cardView
    }
    
    private fun setupActionButtons(actionButton: MaterialButton, secondaryButton: MaterialButton, report: Report) {
        val status = report.status.lowercase()
        
        // Reset visibility
        findViewById<CardView>(R.id.update_status_card).visibility = View.VISIBLE

        when (status) {
            "received" -> {
                actionButton.text = "Start Work"
                actionButton.icon = ContextCompat.getDrawable(this, R.drawable.ic_wrench)
                actionButton.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF9800")) // Orange
                
                actionButton.setOnClickListener {
                    updateReportStatus(report.id, "in-progress")
                }
                
                secondaryButton.text = "Cancel"
                secondaryButton.setOnClickListener { finish() }
            }
            "in-progress" -> {
                actionButton.text = "Mark Resolved"
                actionButton.icon = ContextCompat.getDrawable(this, R.drawable.ic_check)
                actionButton.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#00C853")) // Green
                
                actionButton.setOnClickListener {
                    updateReportStatus(report.id, "resolved")
                }
                
                secondaryButton.text = "Cancel"
                secondaryButton.setOnClickListener { finish() }
            }
            "resolved" -> {
                 // Show closed state
                actionButton.visibility = View.GONE
                secondaryButton.text = "Close"
                secondaryButton.setOnClickListener { finish() }
            }
            else -> {
                // Default fallback
                actionButton.isEnabled = false
                secondaryButton.text = "Close"
                secondaryButton.setOnClickListener { finish() }
            }
        }
    }

    private fun updateReportStatus(reportId: String, newStatus: String) {
        val actionButton = findViewById<MaterialButton>(R.id.action_button)
        // Disable button strictly via view reference or passed argument if inside class
        actionButton.isEnabled = false
        actionButton.text = "Updating..."

        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("reports").document(reportId)
            .update("status", newStatus)
            .addOnSuccessListener {
                 actionButton.isEnabled = true
                 
                 // Update Local UI
                 val statusChip = findViewById<TextView>(R.id.status_chip)
                 statusChip.text = newStatus
                 
                 // Update Chip Style
                when (newStatus.lowercase()) {
                    "in-progress" -> {
                        statusChip.setBackgroundResource(R.drawable.in_progress_chip_background)
                        statusChip.setTextColor(ContextCompat.getColor(this, R.color.orange_500))
                    }
                    "resolved" -> {
                        statusChip.setBackgroundResource(R.drawable.completed_chip_background)
                        statusChip.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
                    }
                    else -> {
                        statusChip.setBackgroundResource(R.drawable.chip_unselected_background)
                        statusChip.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
                    }
                }
                
                // Update Buttons
                // Create a temporary object to reuse setupActionButtons logic or just call it
                // We need to pass the updated report object but we don't have a mutable report object easily.
                // Simpler to copy-paste logic or make report var mutable? 
                // Let's just create a shallow copy? Report is data class.
                val report = intent.getParcelableExtra<Report>("report") ?: return@addOnSuccessListener
                val updatedReport = report.copy(status = newStatus)
                
                intent.putExtra("report", updatedReport)
                setupActionButtons(actionButton, findViewById(R.id.secondary_button), updatedReport)

                Toast.makeText(this, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                actionButton.isEnabled = true
                actionButton.text = "Action Failed" // Temporarily show error
                actionButton.postDelayed({ 
                    // Reset text based on old status
                    // Just triggering logic again with old report
                    val report = intent.getParcelableExtra<Report>("report")
                     if(report!=null) setupActionButtons(actionButton, findViewById(R.id.secondary_button), report)
                }, 1500)
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatDate(dateString: String): String {
        return try {
            // Try to parse "11/27/2024" format
            val parts = dateString.split("/")
            if (parts.size == 3) {
                val month = parts[0].toInt()
                val day = parts[1].toInt()
                val year = parts[2].toInt()
                
                val monthNames = arrayOf(
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
                )
                
                "${monthNames[month - 1]} $day, $year"
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }
}


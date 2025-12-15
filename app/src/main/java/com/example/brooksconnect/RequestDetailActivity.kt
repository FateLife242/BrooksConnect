package com.example.brooksconnect

import android.os.Bundle
import coil.load
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class RequestDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_detail)

        val request = intent.getParcelableExtra<Request>("request")
        if (request == null) {
            finish()
            return
        }

        val closeButton = findViewById<ImageView>(R.id.close_button)
        // closeButtonBottom is now handled in setupActionButtons with secondary_button
        val requestId = findViewById<TextView>(R.id.request_id)
        val documentType = findViewById<TextView>(R.id.document_type)
        val requestStatus = findViewById<TextView>(R.id.request_status)
        val applicantName = findViewById<TextView>(R.id.applicant_name)
        val purpose = findViewById<TextView>(R.id.purpose)
        val filedDate = findViewById<TextView>(R.id.filed_date)


        closeButton.setOnClickListener { finish() }


        // Set request data
        requestId.text = request.id.replace("ID: ", "")
        documentType.text = request.title
        
        // Extract applicant name from "Applicant: Juan dela Cruz"
        val applicantNameText = request.applicant.replace("Applicant: ", "")
        applicantName.text = applicantNameText
        
        // Format date from "Filed: 12/8/2025" to "November 25, 2024"
        val dateText = request.filedDate.replace("Filed: ", "")
        filedDate.text = formatDate(dateText)
        
        // Set default purpose (can be added to Request data class later)
        purpose.text = "Employment requirement"

        // Attachments
        val attachmentImage = findViewById<ImageView>(R.id.attachment_image)
        val noAttachmentsText = findViewById<TextView>(R.id.no_attachments_text)
        
        if (request.attachments.isNotEmpty()) {
            val imageUrl = request.attachments[0] // Load first attachment for now
            noAttachmentsText.visibility = android.view.View.GONE
            attachmentImage.visibility = android.view.View.VISIBLE
            
            // Simple check for PDF based on extension or if we had metadata
            val isPdf = imageUrl.lowercase(java.util.Locale.ROOT).endsWith(".pdf")

            if (isPdf) {
                // Show PDF icon (using standard android drawable or text for now if no specific icon)
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

            attachmentImage.visibility = android.view.View.VISIBLE
            noAttachmentsText.visibility = android.view.View.GONE
            
            attachmentImage.setOnClickListener {
                 val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                 intent.data = android.net.Uri.parse(imageUrl)
                 // Important for PDF viewing
                 intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                 try {
                     startActivity(intent)
                 } catch (e: Exception) {
                     android.widget.Toast.makeText(this, "No app found to open this file", android.widget.Toast.LENGTH_SHORT).show()
                 }
            }
        } else {
            attachmentImage.visibility = android.view.View.GONE
            noAttachmentsText.visibility = android.view.View.VISIBLE
        }
        
        // Set status
        when (request.status.lowercase()) {
            "pending" -> {
                requestStatus.text = "PENDING"
                requestStatus.setBackgroundResource(R.drawable.pending_chip_background)
                requestStatus.setTextColor(getColor(R.color.orange_500))
            }
            "completed" -> {
                requestStatus.text = "COMPLETED"
                requestStatus.setBackgroundResource(R.drawable.completed_chip_background)
                requestStatus.setTextColor(getColor(android.R.color.white))
            }
            "processing" -> {
                requestStatus.text = "PROCESSING"
                requestStatus.setBackgroundResource(R.drawable.in_progress_chip_background)
                requestStatus.setTextColor(getColor(R.color.orange_500))
            }
            "ready" -> {
                requestStatus.text = "READY"
                requestStatus.setBackgroundResource(R.drawable.ready_chip_background)
                requestStatus.setTextColor(getColor(android.R.color.white))
            }
            else -> {
                requestStatus.text = request.status.uppercase()
                requestStatus.setBackgroundResource(R.drawable.chip_unselected_background)
                requestStatus.setTextColor(getColor(android.R.color.darker_gray))
            }
        }

        // Status Action Buttons
        val actionButton = findViewById<MaterialButton>(R.id.action_button)
        val secondaryButton = findViewById<MaterialButton>(R.id.secondary_button)

        setupActionButtons(actionButton, secondaryButton, request)
    }

    private fun setupActionButtons(actionButton: MaterialButton, secondaryButton: MaterialButton, request: Request) {
        val status = request.status.lowercase()
        
        // Define colors
        val blueColor = androidx.core.content.ContextCompat.getColor(this, R.color.blue_600) // Assuming distinct blue exists or use standard
        val greenColor = androidx.core.content.ContextCompat.getColor(this, android.R.color.holo_green_dark)
        val greyColor = androidx.core.content.ContextCompat.getColor(this, android.R.color.darker_gray)
        val defaultColor = androidx.core.content.ContextCompat.getColor(this, android.R.color.black)

        // Reset visibility
        findViewById<androidx.cardview.widget.CardView>(R.id.update_status_card).visibility = android.view.View.VISIBLE

        when (status) {
            "pending" -> {
                actionButton.text = "Start Processing"
                actionButton.icon = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.ic_time) // or appropriate icon
                actionButton.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#155dfc")) // Blue
                
                actionButton.setOnClickListener {
                    updateRequestStatus(request.id, "processing")
                }
                
                secondaryButton.text = "Cancel"
                secondaryButton.setOnClickListener { finish() }
            }
            "processing" -> {
                actionButton.text = "Mark Ready"
                actionButton.icon = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.ic_check)
                actionButton.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#00C853")) // Green
                
                actionButton.setOnClickListener {
                    updateRequestStatus(request.id, "ready")
                }
                
                secondaryButton.text = "Cancel"
                secondaryButton.setOnClickListener { finish() }
            }
            "ready" -> {
                actionButton.text = "Mark Completed"
                actionButton.icon = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.ic_check)
                actionButton.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#424242")) // Dark Grey
                
                actionButton.setOnClickListener {
                    updateRequestStatus(request.id, "completed")
                }
                
                secondaryButton.text = "Close"
                secondaryButton.setOnClickListener { finish() }
            }
            "completed" -> {
                // Show closed state
                findViewById<androidx.cardview.widget.CardView>(R.id.update_status_card).visibility = android.view.View.VISIBLE
                actionButton.visibility = android.view.View.GONE
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

    private fun updateRequestStatus(requestIdRaw: String, newStatus: String) {
        val cleanId = requestIdRaw.trim()
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val requestStatus = findViewById<TextView>(R.id.request_status)
        val actionButton = findViewById<MaterialButton>(R.id.action_button)
        val secondaryButton = findViewById<MaterialButton>(R.id.secondary_button)

        // Disable buttons and show loading state on button
        actionButton.isEnabled = false
        secondaryButton.isEnabled = false
        val originalText = actionButton.text
        actionButton.text = "Updating..."
        
        db.collection("requests").document(cleanId)
            .update("status", newStatus)
            .addOnSuccessListener {
                android.widget.Toast.makeText(this, "Status updated to $newStatus", android.widget.Toast.LENGTH_SHORT).show()
                
                // Update local UI immediately
                val updatedRequest = intent.getParcelableExtra<Request>("request")?.copy(status = newStatus) 
                    ?: return@addOnSuccessListener
                
                // Update Intent payload so if queried again it has new status (optional but good practice)
                intent.putExtra("request", updatedRequest)
                
                // Update Status Chip/Text
                when (newStatus.lowercase()) {
                    "pending" -> {
                        requestStatus.text = "PENDING"
                        requestStatus.setBackgroundResource(R.drawable.pending_chip_background)
                        requestStatus.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.orange_500))
                    }
                    "processing" -> {
                        requestStatus.text = "PROCESSING"
                        requestStatus.setBackgroundResource(R.drawable.in_progress_chip_background)
                        requestStatus.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.orange_500))
                    }
                    "ready" -> {
                        requestStatus.text = "READY"
                        requestStatus.setBackgroundResource(R.drawable.ready_chip_background)
                        requestStatus.setTextColor(androidx.core.content.ContextCompat.getColor(this, android.R.color.white))
                    }
                    "completed" -> {
                        requestStatus.text = "COMPLETED"
                        requestStatus.setBackgroundResource(R.drawable.completed_chip_background)
                        requestStatus.setTextColor(androidx.core.content.ContextCompat.getColor(this, android.R.color.white))
                    }
                }
                
                // Re-setup buttons for next state
                setupActionButtons(actionButton, secondaryButton, updatedRequest)
                
                // Re-enable buttons
                actionButton.isEnabled = true
                secondaryButton.isEnabled = true
            }
            .addOnFailureListener { e ->
                android.widget.Toast.makeText(this, "Failed to update: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                actionButton.text = originalText
                actionButton.isEnabled = true
                secondaryButton.isEnabled = true
            }
    }

    private fun formatDate(dateString: String): String {
        return try {
            // Try to parse "12/8/2025" format
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


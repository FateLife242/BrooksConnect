package com.example.brooksconnect

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText

class AdminAnnouncementsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminAnnouncementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_announcements)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top + 16.dpToPx(this), v.paddingRight, v.paddingBottom)
            insets
        }

        recyclerView = findViewById(R.id.admin_announcements_recyclerview)
        val fab = findViewById<FloatingActionButton>(R.id.fab_create_announcement)
        
        findViewById<ImageView>(R.id.back_arrow).setOnClickListener {
            val intent = android.content.Intent(this, AdminActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        val announcements = AnnouncementsRepository.getAnnouncements(this)
        adapter = AdminAnnouncementsAdapter(
            announcements,
            onEditClick = { announcement, position ->
                showEditBottomSheet(announcement, position)
            },
            onDeleteClick = { position ->
                deleteAnnouncement(position)
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fab.setOnClickListener {
            showCreateBottomSheet()
        }
    }

    private fun showEditBottomSheet(announcement: Announcement, position: Int) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_edit_announcement, null)
        dialog.setContentView(view)

        val categoryGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.category_toggle_group)
        val eventsButton = view.findViewById<MaterialButton>(R.id.btn_events)
        val noticeButton = view.findViewById<MaterialButton>(R.id.btn_notice)
        val advisoryButton = view.findViewById<MaterialButton>(R.id.btn_advisory)
        val publicSafetyButton = view.findViewById<MaterialButton>(R.id.btn_public_safety)
        val titleInput = view.findViewById<TextInputEditText>(R.id.input_title)
        val contentInput = view.findViewById<TextInputEditText>(R.id.input_content)
        val closeButton = view.findViewById<ImageView>(R.id.btn_close)
        val updateButton = view.findViewById<MaterialButton>(R.id.btn_update)

        titleInput.setText(announcement.title)
        contentInput.setText(announcement.description)

        when (announcement.category) {
            "Events" -> eventsButton.isChecked = true
            "Notice" -> noticeButton.isChecked = true
            "Advisory" -> advisoryButton.isChecked = true
            else -> publicSafetyButton.isChecked = true
        }

        closeButton.setOnClickListener { dialog.dismiss() }
        updateButton.setOnClickListener {
            val selectedCategory = when (categoryGroup.checkedButtonId) {
                R.id.btn_events -> "Events"
                R.id.btn_notice -> "Notice"
                R.id.btn_advisory -> "Advisory"
                R.id.btn_public_safety -> "Public Safety"
                else -> announcement.category
            }

            val updated = Announcement(
                category = selectedCategory,
                date = announcement.date,
                title = titleInput.text?.toString().orEmpty(),
                description = contentInput.text?.toString().orEmpty()
            )

            AnnouncementsRepository.updateAnnouncement(this, position, updated)
            adapter.notifyItemChanged(position)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCreateBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_create_announcement, null)
        dialog.setContentView(view)

        val categoryGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.category_toggle_group)
        val eventsButton = view.findViewById<MaterialButton>(R.id.btn_events)
        val noticeButton = view.findViewById<MaterialButton>(R.id.btn_notice)
        val advisoryButton = view.findViewById<MaterialButton>(R.id.btn_advisory)
        val publicSafetyButton = view.findViewById<MaterialButton>(R.id.btn_public_safety)
        val titleInput = view.findViewById<TextInputEditText>(R.id.input_title)
        val contentInput = view.findViewById<TextInputEditText>(R.id.input_content)
        val dateInput = view.findViewById<TextInputEditText>(R.id.input_date)
        val closeButton = view.findViewById<ImageView>(R.id.btn_close)
        val publishButton = view.findViewById<MaterialButton>(R.id.btn_publish)

        // Default category Notice selected
        noticeButton.isChecked = true

        // Date Picker Logic
        val calendar = java.util.Calendar.getInstance()
        dateInput.setOnClickListener {
            val datePickerDialog = android.app.DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(java.util.Calendar.YEAR, year)
                    calendar.set(java.util.Calendar.MONTH, month)
                    calendar.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    dateInput.setText(format.format(calendar.time))
                },
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        closeButton.setOnClickListener { dialog.dismiss() }
        publishButton.setOnClickListener {
            val selectedCategory = when (categoryGroup.checkedButtonId) {
                R.id.btn_events -> "Events"
                R.id.btn_notice -> "Notice"
                R.id.btn_advisory -> "Advisory"
                R.id.btn_public_safety -> "Public Safety"
                else -> "Notice"
            }

            val title = titleInput.text?.toString().orEmpty().trim()
            val description = contentInput.text?.toString().orEmpty().trim()
            val selectedDate = dateInput.text?.toString().orEmpty().trim()

            if (title.isEmpty() && description.isEmpty()) {
                dialog.dismiss()
                return@setOnClickListener
            }

            // Use the selected date, fallback to Today if empty
            val date = if (selectedDate.isNotEmpty()) selectedDate else SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
            
            val newAnnouncement = Announcement(
                category = selectedCategory,
                date = date,
                title = title.ifEmpty { "Untitled" },
                description = description.ifEmpty { "No content provided." }
            )

            AnnouncementsRepository.addAnnouncement(this, newAnnouncement)

            // If it's an Event, also save to Firestore so it appears in the Calendar
            if (selectedCategory == "Events") {
                try {
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val eventDate = sdf.parse(date)
                    
                    if (eventDate != null) {
                        val calendar = java.util.Calendar.getInstance()
                        calendar.time = eventDate
                        
                        val month = SimpleDateFormat("MMM", Locale.getDefault()).format(eventDate).uppercase()
                        val day = SimpleDateFormat("dd", Locale.getDefault()).format(eventDate)
                        
                        val communityEvent = hashMapOf(
                            "title" to newAnnouncement.title,
                            "location" to "Barangay Hall", // Default location, or add a field later
                            "month" to month,
                            "day" to day,
                            "timestamp" to eventDate.time
                        )

                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("events")
                            .add(communityEvent)
                            .addOnSuccessListener {
                                android.widget.Toast.makeText(this, "Event added to Calendar!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                android.widget.Toast.makeText(this, "Failed to sync to Calendar", android.widget.Toast.LENGTH_SHORT).show()
                            }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            adapter.notifyItemInserted(0)
            recyclerView.scrollToPosition(0)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteAnnouncement(position: Int) {
        AnnouncementsRepository.removeAnnouncement(this, position)
        adapter.notifyItemRemoved(position)
        adapter.notifyItemRangeChanged(position, adapter.itemCount)
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}

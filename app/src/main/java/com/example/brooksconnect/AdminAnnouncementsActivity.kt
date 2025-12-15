package com.example.brooksconnect

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
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
        setContentView(R.layout.activity_admin_announcements)

        recyclerView = findViewById(R.id.admin_announcements_recyclerview)
        val fab = findViewById<FloatingActionButton>(R.id.fab_create_announcement)

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
        val closeButton = view.findViewById<ImageView>(R.id.btn_close)
        val publishButton = view.findViewById<MaterialButton>(R.id.btn_publish)

        // Default category Notice selected
        noticeButton.isChecked = true

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
            if (title.isEmpty() && description.isEmpty()) {
                dialog.dismiss()
                return@setOnClickListener
            }

            val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
            val newAnnouncement = Announcement(
                category = selectedCategory,
                date = date,
                title = title.ifEmpty { "Untitled" },
                description = description.ifEmpty { "No content provided." }
            )

            AnnouncementsRepository.addAnnouncement(this, newAnnouncement)
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
}

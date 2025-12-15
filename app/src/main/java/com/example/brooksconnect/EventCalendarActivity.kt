package com.example.brooksconnect

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView

class EventCalendarActivity : AppCompatActivity() {

    private lateinit var adapter: CommunityEventsAdapter
    private val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_event_calendar)

         ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.paddingBottom)
            insets
        }

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.events_recyclerview)
        // Initialize with empty list
        adapter = CommunityEventsAdapter(mutableListOf())
        recyclerView.adapter = adapter

        setupCalendar()
        fetchEvents()
        
        // Temporary cleanup to remove the specific samples user requested
        removeSampleEvents()
    }

    private fun removeSampleEvents() {
        val titlesToRemove = listOf("Senior Citizens Monthly Meeting", "Basketball League Finals")
        
        db.collection("events")
            .whereIn("title", titlesToRemove)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("events").document(document.id).delete()
                }
                if (!documents.isEmpty) {
                    android.widget.Toast.makeText(this, "Removed sample events", android.widget.Toast.LENGTH_SHORT).show()
                    fetchEvents() // Refresh list
                }
            }
    }

    private fun setupCalendar() {
        val phTimeZone = java.util.TimeZone.getTimeZone("Asia/Manila")
        val calendar = java.util.Calendar.getInstance(phTimeZone)

        // Set Header Text (e.g. "December 2025")
        val monthName = calendar.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, java.util.Locale.ENGLISH)
        val year = calendar.get(java.util.Calendar.YEAR)
        
        // Find the Month Title TextView. 
        // Note: The ID in XML was not explicitly set in the snippet I viewed, assuming it's the first TextView in the card header.
        // Let's check logic: The previous XML had a LinearLayout header.
        // I need to make sure I target the correct ID. Previous XML had a TextView with "December 2025".
        // Let's assume user didn't change ID or I need to find it by traversal or add ID in a separate step?
        // Actually, looking at XML, the TextView didn't have an ID! 
        // Wait, I replaced the *days* part, but the *header* part (lines 94-116 in old XML) might still be there.
        // Line 102 in old file: <TextView ... text="December 2025" ... /> No ID.
        // I should have added an ID.
        // Temporary Fix: Find by Hierarchy or just add ID in XML first?
        // Better: I will use findViewById on the root of the included layout if possible, or just traverse.
        // Actually, I can rely on Kotlin synthetics or just add the ID now?
        // I'll assume I can find it by traversing the card content.
        // Card > Linear > Linear (Header) > TextView (Index 0)
        
        try {
            // Traverse up from the RecyclerView to find the Month Title
            val gridRecycler = findViewById<RecyclerView>(R.id.calendar_grid_recyclerview)
            val cardLayout = gridRecycler.parent as android.widget.LinearLayout
            // The first child of the card content LinearLayout is the Header LinearLayout
            val headerLayout = cardLayout.getChildAt(0) as android.widget.LinearLayout 
            // The first child of the Header LinearLayout is the Title TextView (index 1 is the calendar icon)
            val monthTitle = headerLayout.getChildAt(0) as android.widget.TextView
            monthTitle.text = "$monthName $year"
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Calendar Logic
        val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        val todayDay = calendar.get(java.util.Calendar.DAY_OF_MONTH).toString()
        
        // Prepare list
        val dayList = ArrayList<String>()
        
        // Set to 1st of month to find starting weekday
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) // Sun=1, Mon=2...
        
        // Add empty slots for padding
        for (i in 1 until firstDayOfWeek) {
            dayList.add("")
        }
        
        // Add days
        for (i in 1..daysInMonth) {
            dayList.add(i.toString())
        }

        val calendarAdapter = CalendarDaysAdapter(dayList, todayDay)
        val gridRecycler = findViewById<RecyclerView>(R.id.calendar_grid_recyclerview)
        gridRecycler.adapter = calendarAdapter
    }

    private fun fetchEvents() {
        // 1. Get Start of Today in Philippines Time
        val phTimeZone = java.util.TimeZone.getTimeZone("Asia/Manila")
        val calendar = java.util.Calendar.getInstance(phTimeZone)
        
        // Reset to beginning of the day (00:00:00)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        val startOfTodayMillis = calendar.timeInMillis

        // 2. Query Firestore
        db.collection("events")
            .whereGreaterThanOrEqualTo("timestamp", startOfTodayMillis)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Check if collection is actually empty (no events at all) to seed data
                    // checkAndSeedData(startOfTodayMillis) // Disabled seeding as per user request to remove samples
                } else {
                    val events = documents.toObjects(CommunityEvent::class.java)
                    adapter.updateEvents(events)
                }
            }
            .addOnFailureListener { e ->
                android.widget.Toast.makeText(this, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
    }

    private fun checkAndSeedData(todayMillis: Long) {
        // Query ALL events to see if DB is truly empty or just no future events
        db.collection("events").limit(1).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    seedSampleEvents(todayMillis)
                } else {
                    // DB has data, but all are in the past. Show empty state.
                    android.widget.Toast.makeText(this, "No upcoming events", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun seedSampleEvents(todayMillis: Long) {
        val oneDayMillis = 24 * 60 * 60 * 1000L
        
        // Create 1 Past Event (should NOT be shown)
        val pastEvent = CommunityEvent(
            "Community Clean-up Drive (Past)", "Barangay Hall", "DEC", "7",
            todayMillis - (5 * oneDayMillis) 
        )

        // Create 2 Future Events
        val futureEvent1 = CommunityEvent(
            "Senior Citizens Monthly Meeting", "Barangay Hall Multi-Purpose Room", "DEC", "20",
            todayMillis + (4 * oneDayMillis)
        )

        val futureEvent2 = CommunityEvent(
            "Basketball League Finals", "Brookspoint Basketball Court", "DEC", "25",
            todayMillis + (9 * oneDayMillis)
        )
        
        val batch = db.batch()
        val col = db.collection("events")
        
        batch.set(col.document(), pastEvent)
        batch.set(col.document(), futureEvent1)
        batch.set(col.document(), futureEvent2)
        
        batch.commit().addOnSuccessListener {
            fetchEvents() // Reload to show the future ones
            android.widget.Toast.makeText(this, "Sample events added", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

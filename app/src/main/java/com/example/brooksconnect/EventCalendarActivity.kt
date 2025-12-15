package com.example.brooksconnect

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView

class EventCalendarActivity : AppCompatActivity() {
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
        val events = listOf(
            CommunityEvent("Community Clean-up Drive", "Barangay Hall", "DEC", "7"),
            CommunityEvent("Senior Citizens Monthly Meeting", "Barangay Hall Multi-Purpose Room", "DEC", "10"),
            CommunityEvent("Basketball League Finals", "Brookspoint Basketball Court", "DEC", "15")
        )
        recyclerView.adapter = CommunityEventsAdapter(events)
    }
}

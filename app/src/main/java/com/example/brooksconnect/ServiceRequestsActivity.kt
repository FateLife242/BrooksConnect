package com.example.brooksconnect

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ServiceRequestsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_service_requests)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.paddingBottom)
            insets
        }

        // Setup Back Button
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        // Setup Track Requests Card
        findViewById<CardView>(R.id.track_requests_card).setOnClickListener {
             // Navigate to Requests Record Activity (Assuming it exists or will be created, 
             // but for now let's just use a placeholder or check if existing RecordsActivity works)
             // Based on previous file list, there is a RecordsActivity.
             val intent = Intent(this, TrackRequestsActivity::class.java)
             // Putting an extra to maybe select the correct tab if implemented
             startActivity(intent)
        }

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.services_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val services = listOf(
            Service(
                "Barangay Clearance",
                "Certificate of residency for various purposes",
                "Processing fee: ₱50.00",
                R.drawable.ic_requests // Placeholder icon
            ),
            Service(
                "Certificate of Indigency",
                "For financially disadvantaged residents",
                "Processing fee: Free",
                R.drawable.ic_requests // Placeholder icon
            ),
            Service(
                "Certificate of Residency",
                "Proof of residence in the barangay",
                "Processing fee: ₱30.00",
                R.drawable.ic_home // Using home icon as it fits better
            ),
            Service(
                "Barangay Business Permit",
                "Permit for small businesses",
                "Processing fee: ₱100.00",
                R.drawable.ic_services // Placeholder icon
            ),
            Service(
                "Certificate of Good Moral",
                "Character reference certificate",
                "Processing fee: ₱30.00",
                R.drawable.ic_shield_outline
            ),
            Service(
                "First Time Job Seeker",
                "Tax exemption for first-time job seekers",
                "Processing fee: Free",
                R.drawable.ic_services
            )
        )

        recyclerView.adapter = ServiceAdapter(services)

        val bottomNavigation = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_services
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = android.content.Intent(this, MainActivity::class.java)
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    true
                }
                R.id.navigation_news -> {
                    val intent = android.content.Intent(this, AnnouncementsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_services -> true
                R.id.navigation_profile -> {
                    val intent = android.content.Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}

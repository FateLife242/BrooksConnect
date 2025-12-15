package com.example.brooksconnect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class AnnouncementsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AnnouncementsAdapter
    private lateinit var chipGroup: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcements)

        recyclerView = findViewById(R.id.announcements_recyclerview)
        chipGroup = findViewById(R.id.chip_group)
        
        findViewById<android.widget.ImageView>(R.id.back_arrow).setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        val announcements = AnnouncementsRepository.getAnnouncements(this)
        adapter = AnnouncementsAdapter(announcements)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            // Reset all chips to default style
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as? Chip
                chip?.let {
                    it.setChipBackgroundColorResource(android.R.color.white) // Or custom gray #EEEEEE
                    it.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EEEEEE")))
                    it.setTextColor(android.graphics.Color.parseColor("#757575")) // Darker Gray
                }
            }

            if (checkedIds.isNotEmpty()) {
                val chip: Chip? = group.findViewById(checkedIds[0])
                chip?.let {
                    // Set selected chip style
                    it.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#155dfc")))
                    it.setTextColor(android.graphics.Color.WHITE)
                    
                    adapter.filter(it.text.toString())
                    checkEmptyState()
                }
            } else {
                // If nothing selected (shouldn't happen with singleSelection=true and checked=true default, but safe fallback)
                adapter.filter("All")
                checkEmptyState()
            }
        }
        
        // Initial state check (since "All" is default but listener triggers only on change, we assume initial list is full)
        // If initial list is empty, we should check too.
        checkEmptyState()

        val bottomNavigation = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_news
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = android.content.Intent(this, MainActivity::class.java)
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    true
                }
                R.id.navigation_news -> true
                R.id.navigation_services -> {
                    val intent = android.content.Intent(this, ServiceRequestsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = android.content.Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun checkEmptyState() {
        val emptyStateView = findViewById<android.widget.LinearLayout>(R.id.empty_state_view)
        if (adapter.itemCount == 0) {
            recyclerView.visibility = android.view.View.GONE
            emptyStateView.visibility = android.view.View.VISIBLE
        } else {
            recyclerView.visibility = android.view.View.VISIBLE
            emptyStateView.visibility = android.view.View.GONE
        }
    }


}

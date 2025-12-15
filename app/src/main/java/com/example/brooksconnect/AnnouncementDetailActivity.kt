package com.example.brooksconnect

import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AnnouncementDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcement_detail)

        val announcement = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("announcement", Announcement::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Announcement>("announcement")
        }

        announcement?.let { 
            findViewById<TextView>(R.id.detail_category).text = it.category
            findViewById<TextView>(R.id.detail_date).text = it.date
            findViewById<TextView>(R.id.detail_title).text = it.title
            findViewById<TextView>(R.id.detail_description).text = it.description

            val categoryTextView = findViewById<TextView>(R.id.detail_category)
            val detailIcon = findViewById<ImageView>(R.id.detail_icon)

            when (it.category) {
                "Events" -> {
                    categoryTextView.setBackgroundResource(R.drawable.events_chip_background)
                    categoryTextView.setTextColor(getColor(R.color.purple_500))
                    detailIcon.setBackgroundResource(R.drawable.light_blue_circle_background)
                }
                "Notice" -> {
                    categoryTextView.setBackgroundResource(R.drawable.notice_chip_background)
                    categoryTextView.setTextColor(getColor(R.color.blue_500))
                    detailIcon.setBackgroundResource(R.drawable.notice_chip_background)
                }
                "Advisory" -> {
                    categoryTextView.setBackgroundResource(R.drawable.advisory_chip_background)
                    categoryTextView.setTextColor(getColor(R.color.orange_500))
                    detailIcon.setBackgroundResource(R.drawable.advisory_chip_background)
                }
                else -> {
                    categoryTextView.setBackgroundResource(R.drawable.chip_unselected_background)
                    categoryTextView.setTextColor(getColor(android.R.color.darker_gray))
                    detailIcon.setBackgroundResource(R.drawable.light_blue_circle_background)
                }
            }
        }

        findViewById<ImageView>(R.id.back_arrow).setOnClickListener {
            finish()
        }
    }
}
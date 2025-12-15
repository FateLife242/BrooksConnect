package com.example.brooksconnect

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EventDetailActivity : AppCompatActivity() {

    private lateinit var commentsAdapter: CommentsAdapter
    private val comments = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_event_detail)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.paddingBottom)
            insets
        }

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        // Get data from intent
        val title = intent.getStringExtra("title") ?: "Event"
        val location = intent.getStringExtra("location") ?: "Location"
        val month = intent.getStringExtra("month") ?: "DEC"
        val day = intent.getStringExtra("day") ?: "1"

        findViewById<TextView>(R.id.detail_title).text = title
        findViewById<TextView>(R.id.detail_location).text = location
        findViewById<TextView>(R.id.detail_month).text = month
        findViewById<TextView>(R.id.detail_day).text = day

        // Setup Comments
        val recyclerView = findViewById<RecyclerView>(R.id.comments_recyclerview)
        comments.add(Comment("Juan dela Cruz", "a")) // Initial dummy comment
        commentsAdapter = CommentsAdapter(comments)
        recyclerView.adapter = commentsAdapter

        // Send Comment
        val input = findViewById<EditText>(R.id.comment_input)
        findViewById<FloatingActionButton>(R.id.send_button).setOnClickListener {
            val text = input.text.toString()
            if (text.isNotBlank()) {
                comments.add(Comment("User", text))
                commentsAdapter.notifyItemInserted(comments.size - 1)
                input.text.clear()
            }
        }
    }
}

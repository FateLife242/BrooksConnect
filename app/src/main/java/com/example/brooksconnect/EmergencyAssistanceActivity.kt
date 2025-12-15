package com.example.brooksconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView

class EmergencyAssistanceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_emergency_assistance)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.back_button)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = v.layoutParams as android.view.ViewGroup.MarginLayoutParams
            params.topMargin = systemBars.top + 16.dpToPx(this)
            v.layoutParams = params
            insets
        }

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        findViewById<FrameLayout>(R.id.sos_button).setOnClickListener {
            Toast.makeText(this, "Sending Emergency Alert...", Toast.LENGTH_LONG).show()
            // In a real app, this would trigger the backend alert
        }

        val contacts = listOf(
            EmergencyContact("Barangay Emergency Hotline", "0917-123-4567", R.drawable.ic_siren),
            EmergencyContact("Police (PNP)", "117", R.drawable.ic_police),
            EmergencyContact("Fire Department (BFP)", "(074) 442-2222", R.drawable.ic_fire_truck),
            EmergencyContact("Medical Emergency", "(074) 442-1111", R.drawable.ic_medical)
        )

        val recyclerView = findViewById<RecyclerView>(R.id.contacts_recyclerview)
        recyclerView.adapter = EmergencyContactsAdapter(contacts) { contact ->
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${contact.number}")
            startActivity(intent)
        }
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}

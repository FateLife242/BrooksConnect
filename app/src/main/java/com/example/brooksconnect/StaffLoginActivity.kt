package com.example.brooksconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StaffLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_staff_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Handle Window Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top + 16.dpToPx(this), v.paddingRight, v.paddingBottom)
            insets
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val backArrow = findViewById<ImageView>(R.id.back_arrow)

        // Handle back button
        backArrow.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        // Handle login button click
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                btnLogin.isEnabled = false
                btnLogin.text = "Signing in..."
                signInAndCheckRole(email, password, btnLogin)
            } else {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    private fun signInAndCheckRole(email: String, password: String, btnLogin: Button) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in successful. Check the role in Firestore.
                    checkAdminRole(btnLogin)
                } else {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Sign In"
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkAdminRole(btnLogin: Button) {
        val user = auth.currentUser
        
        if (user == null) {
            btnLogin.isEnabled = true
            btnLogin.text = "Sign In"
            Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check user role in Firestore
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")
                    
                    if (role == "admin" || role == "staff") {
                        Toast.makeText(this, "Admin Login Successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, AdminActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        // Regular user trying to access admin panel - deny access
                        auth.signOut() // Sign them out
                        btnLogin.isEnabled = true
                        btnLogin.text = "Sign In"
                        Toast.makeText(
                            this, 
                            "Access denied. This area is for staff only.", 
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    // No user document found, they might be a legacy admin - check custom claims
                    checkAdminClaim(btnLogin)
                }
            }
            .addOnFailureListener { e ->
                // Firestore check failed, try custom claims as fallback
                checkAdminClaim(btnLogin)
            }
    }

    private fun checkAdminClaim(btnLogin: Button) {
        val user = auth.currentUser

        // Force token refresh to pull the latest custom claims
        user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
            if (tokenTask.isSuccessful) {
                val claims = tokenTask.result?.claims
                val isAdmin = claims?.get("admin") as? Boolean ?: false

                if (isAdmin) {
                    Toast.makeText(this, "Admin Login Successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, AdminActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    // No admin claim - deny access
                    auth.signOut()
                    btnLogin.isEnabled = true
                    btnLogin.text = "Sign In"
                    Toast.makeText(
                        this, 
                        "Access denied. This area is for staff only.", 
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                btnLogin.isEnabled = true
                btnLogin.text = "Sign In"
                Toast.makeText(this, "Failed to verify user role.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
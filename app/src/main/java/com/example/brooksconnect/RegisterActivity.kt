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

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top + v.paddingTop, v.paddingRight, v.paddingBottom)
            insets
        }

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        val fullName = findViewById<EditText>(R.id.full_name)
        val email = findViewById<EditText>(R.id.email)
        val phone = findViewById<EditText>(R.id.phone)
        val address = findViewById<EditText>(R.id.address)
        val password = findViewById<EditText>(R.id.password)
        val createAccountButton = findViewById<Button>(R.id.create_account_button)

        createAccountButton.setOnClickListener {
            val nameText = fullName.text.toString().trim()
            val emailText = email.text.toString().trim()
            val phoneText = phone.text.toString().trim()
            val addressText = address.text.toString().trim()
            val passwordText = password.text.toString().trim()

            if (nameText.isEmpty() || emailText.isEmpty() || phoneText.isEmpty() || 
                addressText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordText.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createAccountButton.isEnabled = false
            createAccountButton.text = "Creating account..."

            // Create user with Firebase Auth
            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.let {
                            // Store user data in Firestore with role = "user" (not admin)
                            val userData = hashMapOf(
                                "uid" to it.uid,
                                "name" to nameText,
                                "email" to emailText,
                                "phone" to phoneText,
                                "address" to addressText,
                                "role" to "user", // Regular user, not admin
                                "createdAt" to System.currentTimeMillis()
                            )

                            db.collection("users").document(it.uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    auth.signOut() // Sign out to force manual login
                                    Toast.makeText(this, "Account created! Please sign in.", Toast.LENGTH_SHORT).show()
                                    finish() // Close RegisterActivity, goes back to LoginActivity
                                }
                                .addOnFailureListener { e ->
                                    createAccountButton.isEnabled = true
                                    createAccountButton.text = "Create Account"
                                    Toast.makeText(this, "Failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    } else {
                        createAccountButton.isEnabled = true
                        createAccountButton.text = "Create Account"
                        Toast.makeText(
                            this,
                            "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}

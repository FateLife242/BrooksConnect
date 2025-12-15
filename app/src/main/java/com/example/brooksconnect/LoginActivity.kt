package com.example.brooksconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login)
        val staffLoginButton = findViewById<TextView>(R.id.staff_login_button)

        // Check if user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Verify role before auto-login
            // If admin, this will sign them out and show the login screen.
            // If user, this will navigate to main.
            loginButton.isEnabled = false
            loginButton.text = "Checking session..."
            checkUserRole(loginButton)
            // Do not return here, let the listeners attach, but arguably we should wait.
            // Actually checkUserRole is async. The UI is already visible.
            // We should preventing user from typing while checking?
            // Existing listeners will overwrite local listeners if we don't return?
            // No, listeners are set below.
        }

        loginButton.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginButton.isEnabled = false
            loginButton.text = "Signing in..."

            auth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        checkUserRole(loginButton)
                    } else {
                        loginButton.isEnabled = true
                        loginButton.text = "Sign In"
                        Toast.makeText(
                            this,
                            "Login failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        staffLoginButton.setOnClickListener {
            val intent = Intent(this, StaffLoginActivity::class.java)
            startActivity(intent)
        }

        val registerPrompt = findViewById<TextView>(R.id.register_prompt)
        registerPrompt.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkUserRole(loginButton: Button) {
        val user = auth.currentUser
        if (user == null) {
            loginButton.isEnabled = true
            loginButton.text = "Sign In"
            return
        }

        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                // Default to allowing access unless explicitly an admin
                var isStaff = false
                
                if (document != null && document.exists()) {
                    val role = document.getString("role")
                    if (role == "admin" || role == "staff") {
                        isStaff = true
                    }
                }
                
                if (isStaff) {
                   denyAccess(loginButton)
                } else {
                   // Double check claims (if legacy admin)
                   user.getIdToken(false).addOnSuccessListener { result ->
                       val claims = result.claims
                       val isAdminClaim = claims["admin"] as? Boolean ?: false
                       
                       if (isAdminClaim) {
                           denyAccess(loginButton)
                       } else {
                           // Regular user confirm
                           loginButton.isEnabled = true
                           loginButton.text = "Sign In"
                           Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                           navigateToMain()
                       }
                   }.addOnFailureListener {
                       // Failed to get claims, assume user (safer for blocking? No, safer to allow if Auth worked and no DB role says admin)
                       // If we are paranoid, we block on error. But typically offline login works.
                       // Let's proceed.
                       navigateToMain()
                   }
                }
            }
            .addOnFailureListener {
                // If DB check fails (offline?), we generally allow login if Auth was successful, 
                // but enforcing strict admin content separation might require online check.
                // For now, let's proceed to Main.
                navigateToMain()
            }
    }

    private fun denyAccess(loginButton: Button) {
        auth.signOut()
        loginButton.isEnabled = true
        loginButton.text = "Sign In"
        Toast.makeText(
            this, 
            "Access Denied: Please use the Staff Login portal.", 
            Toast.LENGTH_LONG
        ).show()
    }



    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}

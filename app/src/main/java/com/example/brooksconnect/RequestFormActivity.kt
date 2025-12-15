package com.example.brooksconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class RequestFormActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    // UI Elements
    private lateinit var etFullName: EditText
    private lateinit var etAddress: EditText
    private lateinit var etContactNumber: EditText
    private lateinit var etPurpose: EditText
    private lateinit var btnSubmit: MaterialButton
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var processingFeeText: TextView
    private lateinit var headerTitle: TextView
    private lateinit var uploadContainer: LinearLayout

    private var serviceTitle: String = "Service Request"
    private var servicePrice: String = "Processing Fee: Varies"
    
    private var selectedImageUri: Uri? = null

    // Image Picker
    // File Picker
    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            
            // Take persistable permission to access content later if needed (though we upload immediately usually)
            val takeFlags: Int = intent.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            try {
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                // Ignore if not supported
            }

            // Update UI to show preview
            uploadContainer.findViewById<TextView>(R.id.upload_text)?.visibility = View.GONE
            uploadContainer.findViewById<TextView>(R.id.upload_subtext)?.visibility = View.GONE
            
            val iconView = uploadContainer.findViewById<ImageView>(R.id.upload_icon)
            iconView?.let {
                it.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                it.layoutParams.height = 400 // Fixed height for preview
                it.setPadding(0, 0, 0, 0)
                
                val type = contentResolver.getType(uri)
                if (type?.startsWith("image/") == true) {
                    it.setImageURI(uri)
                    it.scaleType = ImageView.ScaleType.CENTER_CROP
                } else {
                    // Show generic document icon for PDF etc
                    it.setImageResource(android.R.drawable.ic_menu_sort_by_size) // Placeholder icon
                    it.scaleType = ImageView.ScaleType.CENTER_INSIDE
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_request_form)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize Cloudinary
        try {
            val config = HashMap<String, String>()
            config["cloud_name"] = "dpj3oe5kg" 
            config["api_key"] = "434136765863741"
            config["api_secret"] = "Z93-mkZLP5jlRAKEfmnipxA-vfc"
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // Already initialized
        }

        // Get data from intent
        serviceTitle = intent.getStringExtra("SERVICE_TITLE") ?: "Service Request"
        servicePrice = intent.getStringExtra("SERVICE_PRICE") ?: "Processing Fee: Varies"

        setupUI()
        setupWindowInsets()
        loadUserData()
    }

    private fun setupUI() {
        etFullName = findViewById(R.id.et_full_name)
        etAddress = findViewById(R.id.et_address)
        etContactNumber = findViewById(R.id.et_contact_number)
        etPurpose = findViewById(R.id.et_purpose)
        btnSubmit = findViewById(R.id.btn_submit_request)
        loadingOverlay = findViewById(R.id.loading_overlay)
        processingFeeText = findViewById(R.id.processing_fee_text)
        headerTitle = findViewById(R.id.header_title)
        uploadContainer = findViewById(R.id.upload_container)

        headerTitle.text = serviceTitle
        processingFeeText.text = servicePrice

        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }

        uploadContainer.setOnClickListener {
            pickFileLauncher.launch(arrayOf("image/*", "application/pdf"))
        }

        btnSubmit.setOnClickListener {
            submitRequest()
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.paddingBottom)
            insets
        }
    }

    private var registeredName: String = ""

    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return
        
        // Pre-fill email/name if available from Auth
        etFullName.setText(currentUser.displayName)
        
        // Fetch more details from Firestore users collection
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name")
                    val address = document.getString("address")
                    val phone = document.getString("phone")

                    if (!name.isNullOrEmpty()) {
                         etFullName.setText(name)
                         registeredName = name // Capture for saving
                    }
                    if (!address.isNullOrEmpty()) etAddress.setText(address)
                    if (!phone.isNullOrEmpty()) etContactNumber.setText(phone)
                }
            }
    }

    private fun submitRequest() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to submit a request", Toast.LENGTH_SHORT).show()
            return
        }

        val fullName = etFullName.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val contact = etContactNumber.text.toString().trim()
        val purpose = etPurpose.text.toString().trim()
        
        // Use registered name if available, otherwise fall back to typed name (or Auth display name)
        val finalName = if (registeredName.isNotEmpty()) registeredName else (currentUser.displayName ?: fullName)

        if (fullName.isEmpty() || address.isEmpty() || contact.isEmpty() || purpose.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        loadingOverlay.visibility = View.VISIBLE
        btnSubmit.isEnabled = false
        
        if (selectedImageUri != null) {
             uploadImageAndSaveRequest(currentUser.uid, currentUser.email ?: "", fullName, finalName, address, contact, purpose)
        } else {
             saveRequestToFirestore(currentUser.uid, currentUser.email ?: "", fullName, finalName, address, contact, purpose, emptyList())
        }
    }
    
    private fun uploadImageAndSaveRequest(userId: String, email: String, inputName: String, regName: String, address: String, contact: String, purpose: String) {
        val requestId = UUID.randomUUID().toString()
        
        MediaManager.get().upload(selectedImageUri)
            .unsigned("ml_default")
            .option("resource_type", "auto")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    runOnUiThread {
                        Toast.makeText(this@RequestFormActivity, "Uploading image...", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) { }
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String ?: ""
                    saveRequestToFirestore(userId, email, inputName, regName, address, contact, purpose, listOf(url))
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    runOnUiThread {
                        loadingOverlay.visibility = View.GONE
                        btnSubmit.isEnabled = true
                        Toast.makeText(this@RequestFormActivity, "Upload failed: ${error.description}", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    private fun saveRequestToFirestore(userId: String, email: String, inputName: String, regName: String, address: String, contact: String, purpose: String, attachments: List<String>) {
        val requestId = "REQ-${System.currentTimeMillis()}"
        
        val requestData = hashMapOf(
            "requestId" to requestId,
            "userId" to userId,
            "userEmail" to email,
            "serviceType" to serviceTitle,
            "fullName" to inputName, // Keep what they typed as "fullName"
            "registeredName" to regName, // Store official name here
            "address" to address,
            "contactNumber" to contact,
            "purpose" to purpose,
            "status" to "pending",
            "createdAt" to System.currentTimeMillis(),
            "updatedAt" to System.currentTimeMillis(),
            "attachments" to attachments
        )

        db.collection("requests")
            .document(requestId)
            .set(requestData)
            .addOnSuccessListener {
                loadingOverlay.visibility = View.GONE
                Toast.makeText(this, "Request submitted successfully!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                loadingOverlay.visibility = View.GONE
                btnSubmit.isEnabled = true
                Toast.makeText(this, "Failed to submit request: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

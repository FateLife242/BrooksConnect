package com.example.brooksconnect

data class TrackRequest(
    val title: String,
    val requestId: String,
    val filedDate: String,
    val purpose: String,
    val status: String, // "Processing", "Ready for Pickup", "Completed"
    val currentStep: Int // 1: Pending Review, 2: Processing, 3: Ready for Pickup, 4: Completed
)

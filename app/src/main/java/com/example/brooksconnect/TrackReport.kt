package com.example.brooksconnect

data class TrackReport(
    val title: String,
    val reportId: String,
    val description: String,
    val location: String,
    val aiClassification: String, // e.g., "Public Lighting Repair"
    val filedDate: String,
    val status: String // e.g. "Received"
)

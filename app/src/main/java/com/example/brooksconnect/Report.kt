package com.example.brooksconnect

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Report(
    val title: String,
    val id: String,
    val description: String,
    val location: String,
    val reporter: String,
    val filedDate: String,
    val status: String,
    val attachments: List<String> = emptyList(),
    val aiPriority: String = "",
    val aiClassification: String = "",
    val aiAction: String = ""
) : Parcelable

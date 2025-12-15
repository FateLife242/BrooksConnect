package com.example.brooksconnect

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Request(
    val title: String,
    val id: String,
    val applicant: String,
    val filedDate: String,
    val status: String,
    val attachments: List<String> = emptyList() // Default empty for backward compatibility
) : Parcelable

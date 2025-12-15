package com.example.brooksconnect

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Announcement(
    val category: String,
    val date: String,
    val title: String,
    val description: String
) : Parcelable
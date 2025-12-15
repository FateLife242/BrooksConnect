package com.example.brooksconnect

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AnnouncementsRepository {

    private const val PREFS_NAME = "AnnouncementsPrefs"
    private const val ANNOUNCEMENTS_KEY = "announcements"

    private val initialAnnouncements = listOf<Announcement>()

    private var announcements: MutableList<Announcement>? = null

    fun getAnnouncements(context: Context): MutableList<Announcement> {
        if (announcements == null) {
            announcements = loadAnnouncements(context)
        }
        return announcements!!
    }

    private fun loadAnnouncements(context: Context): MutableList<Announcement> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(ANNOUNCEMENTS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Announcement>>() {}.type
            val loadedList: MutableList<Announcement> = gson.fromJson(json, type)
            
            // Filter out legacy sample announcements
            val legacyTitles = setOf(
                "Community Clean-up Drive",
                "Holiday Schedule Notice",
                "Water Interruption Advisory",
                "Public Market Day",
                "Christmas Tree Lighting",
                "Voter's Registration",
                "Road Closure Advisory"
            )
            
            loadedList.removeAll { it.title in legacyTitles }
            loadedList
        } else {
            initialAnnouncements.toMutableList()
        }
    }

    private fun saveAnnouncements(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        val gson = Gson()
        val json = gson.toJson(announcements)
        prefs.putString(ANNOUNCEMENTS_KEY, json)
        prefs.apply()
    }

    fun addAnnouncement(context: Context, announcement: Announcement) {
        getAnnouncements(context).add(0, announcement)
        saveAnnouncements(context)
    }

    fun removeAnnouncement(context: Context, position: Int) {
        getAnnouncements(context).let {
            if (position >= 0 && position < it.size) {
                it.removeAt(position)
                saveAnnouncements(context)
            }
        }
    }

    fun updateAnnouncement(context: Context, position: Int, updatedAnnouncement: Announcement) {
        getAnnouncements(context).let {
            if (position >= 0 && position < it.size) {
                it[position] = updatedAnnouncement
                saveAnnouncements(context)
            }
        }
    }
}

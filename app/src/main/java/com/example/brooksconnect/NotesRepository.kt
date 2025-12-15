package com.example.brooksconnect

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object NotesRepository {

    private const val PREFS_NAME = "NotesPrefs"
    private const val NOTES_KEY = "report_notes"

    // Map of ReportID -> List of Notes
    private var notesMap: MutableMap<String, MutableList<String>>? = null

    fun getNotes(context: Context, reportId: String): MutableList<String> {
        if (notesMap == null) {
            notesMap = loadNotes(context)
        }
        // Return the list for this report, or create a new empty list if none exists
        return notesMap?.getOrPut(reportId) { mutableListOf() } ?: mutableListOf()
    }

    private fun loadNotes(context: Context): MutableMap<String, MutableList<String>> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString(NOTES_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<MutableMap<String, MutableList<String>>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableMapOf()
        }
    }

    private fun saveNotes(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        val gson = Gson()
        val json = gson.toJson(notesMap)
        prefs.putString(NOTES_KEY, json)
        prefs.apply()
    }

    fun addNote(context: Context, reportId: String, note: String) {
        val notes = getNotes(context, reportId)
        notes.add(note)
        saveNotes(context)
    }
}

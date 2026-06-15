package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_entries")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pestIndex: Int,
    val pestName: String,
    val confidence: Float,
    val localImageUri: String, // Path to local internal cache of image file
    val timestamp: Long = System.currentTimeMillis()
)

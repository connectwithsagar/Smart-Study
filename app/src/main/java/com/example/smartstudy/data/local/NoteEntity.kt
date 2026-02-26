package com.example.smartstudy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rawText: String,
    val summary: String,
    val quizScore: Int,
    val date: Long = System.currentTimeMillis()
)
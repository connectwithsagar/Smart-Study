package com.example.smartstudy.data.repository

import com.example.smartstudy.data.local.NoteDao
import com.example.smartstudy.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun insert(note: NoteEntity) {
        noteDao.insertNote(note)
    }

    suspend fun getSessionCount(): Int = noteDao.getSessionCount()
    
    suspend fun getAverageScore(): Float = noteDao.getAverageScore()
}
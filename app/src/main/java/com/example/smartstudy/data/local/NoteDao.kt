package com.example.smartstudy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getSessionCount(): Int

    @Query("SELECT AVG(quizScore) FROM notes WHERE quizScore > 0")
    suspend fun getAverageScore(): Float
}
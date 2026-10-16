package com.example.notes.data.repository

import com.example.notes.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeNotes(): Flow<List<Note>>
    suspend fun getNote(id: Long): Note?
    suspend fun saveNote(note: Note)
    suspend fun toggleNoteCompletion(note: Note)
    suspend fun deleteNote(note: Note)
}

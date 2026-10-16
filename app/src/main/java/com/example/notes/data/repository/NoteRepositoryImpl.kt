package com.example.notes.data.repository

import com.example.notes.data.local.NoteDao
import com.example.notes.data.local.toDomain
import com.example.notes.data.local.toEntity
import com.example.notes.domain.model.Note
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao
) : NoteRepository {

    override fun observeNotes(): Flow<List<Note>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getNote(id: Long): Note? = dao.getById(id)?.toDomain()

    override suspend fun saveNote(note: Note) = dao.upsert(note.toEntity())

    override suspend fun toggleNoteCompletion(note: Note) {
        val newStatus = !note.isCompleted
        dao.updateCompletionStatus(note.id, newStatus, System.currentTimeMillis())
    }

    override suspend fun deleteNote(note: Note) = dao.deleteById(note.id)
}

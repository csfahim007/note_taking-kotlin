package com.example.notes.data.repository

import com.example.notes.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNoteRepository : NoteRepository {
    private val notesFlow = MutableStateFlow<List<Note>>(emptyList())

    override fun observeNotes(): Flow<List<Note>> {
        return notesFlow.map { list ->
            list.sortedWith(compareBy<Note> { it.isCompleted }.thenByDescending { it.updatedAt })
        }
    }

    override suspend fun getNote(id: Long): Note? {
        return notesFlow.value.find { it.id == id }
    }

    override suspend fun saveNote(note: Note) {
        val current = notesFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == note.id && note.id != 0L }
        if (index >= 0) {
            current[index] = note
        } else {
            val nextId = if (note.id == 0L) (current.maxOfOrNull { it.id } ?: 0L) + 1L else note.id
            current.add(note.copy(id = nextId))
        }
        notesFlow.value = current
    }

    override suspend fun toggleNoteCompletion(note: Note) {
        val current = notesFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == note.id }
        if (index >= 0) {
            current[index] = current[index].copy(
                isCompleted = !current[index].isCompleted,
                updatedAt = System.currentTimeMillis()
            )
            notesFlow.value = current
        }
    }

    override suspend fun deleteNote(note: Note) {
        notesFlow.value = notesFlow.value.filterNot { it.id == note.id }
    }
}

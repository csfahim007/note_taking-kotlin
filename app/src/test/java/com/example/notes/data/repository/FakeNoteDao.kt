package com.example.notes.data.repository

import com.example.notes.data.local.NoteDao
import com.example.notes.data.local.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNoteDao : NoteDao {
    private val notesFlow = MutableStateFlow<List<NoteEntity>>(emptyList())

    override fun observeAll(): Flow<List<NoteEntity>> {
        return notesFlow.map { list ->
            list.sortedWith(compareBy<NoteEntity> { it.isCompleted }.thenByDescending { it.updatedAt })
        }
    }

    override suspend fun getById(id: Long): NoteEntity? {
        return notesFlow.value.find { it.id == id }
    }

    override suspend fun upsert(note: NoteEntity) {
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

    override suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean, updatedAt: Long) {
        val current = notesFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            current[index] = current[index].copy(isCompleted = isCompleted, updatedAt = updatedAt)
            notesFlow.value = current
        }
    }

    override suspend fun deleteById(id: Long) {
        notesFlow.value = notesFlow.value.filterNot { it.id == id }
    }
}

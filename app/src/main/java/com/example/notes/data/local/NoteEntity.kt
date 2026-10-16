package com.example.notes.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.notes.domain.model.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val content: String,
    val isCompleted: Boolean = false,
    val isTodo: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

fun NoteEntity.toDomain() = Note(
    id = id,
    title = title,
    content = content,
    isCompleted = isCompleted,
    isTodo = isTodo,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Note.toEntity() = NoteEntity(
    id = id,
    title = title,
    content = content,
    isCompleted = isCompleted,
    isTodo = isTodo,
    createdAt = createdAt,
    updatedAt = updatedAt
)

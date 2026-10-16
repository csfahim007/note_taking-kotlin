package com.example.notes.domain.model

/** A note or to-do as seen by the UI layer. */
data class Note(
    val id: Long = 0L,
    val title: String,
    val content: String,
    val isCompleted: Boolean = false,
    val isTodo: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

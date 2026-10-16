package com.example.notes.domain.model

/** A note as seen by the UI layer. Independent of how it is stored. */
data class Note(
    val id: Long = 0L,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)

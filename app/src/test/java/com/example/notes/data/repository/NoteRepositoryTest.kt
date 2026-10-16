package com.example.notes.data.repository

import com.example.notes.domain.model.Note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class NoteRepositoryTest {

    private lateinit var fakeDao: FakeNoteDao
    private lateinit var repository: NoteRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeNoteDao()
        repository = NoteRepositoryImpl(fakeDao)
    }

    @Test
    fun saveAndGetNote() = runTest {
        val note = Note(id = 1L, title = "Test Title", content = "Test Content", createdAt = 100L, updatedAt = 100L)
        repository.saveNote(note)

        val retrieved = repository.getNote(1L)
        assertEquals("Test Title", retrieved?.title)
        assertEquals("Test Content", retrieved?.content)
    }

    @Test
    fun observeNotesEmitsInDescendingOrderOfUpdatedAt() = runTest {
        val note1 = Note(id = 1L, title = "First", content = "Content 1", createdAt = 100L, updatedAt = 100L)
        val note2 = Note(id = 2L, title = "Second", content = "Content 2", createdAt = 200L, updatedAt = 300L)
        repository.saveNote(note1)
        repository.saveNote(note2)

        val observed = repository.observeNotes().first()
        assertEquals(2, observed.size)
        assertEquals(2L, observed[0].id)
        assertEquals(1L, observed[1].id)
    }

    @Test
    fun deleteNoteRemovesIt() = runTest {
        val note = Note(id = 1L, title = "To Delete", content = "Content", createdAt = 100L, updatedAt = 100L)
        repository.saveNote(note)

        repository.deleteNote(note)
        assertNull(repository.getNote(1L))
    }
}

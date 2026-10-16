package com.example.notes.ui.edit

import androidx.lifecycle.SavedStateHandle
import com.example.notes.data.repository.FakeNoteRepository
import com.example.notes.domain.model.Note
import com.example.notes.ui.navigation.NEW_NOTE_ID
import com.example.notes.ui.navigation.NOTE_ID_ARG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeNoteRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeNoteRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun newNoteInitialization() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(NOTE_ID_ARG to NEW_NOTE_ID))
        val viewModel = NoteEditViewModel(savedStateHandle, repository)

        assertTrue(viewModel.isNewNote)
        assertEquals("", viewModel.title)
        assertEquals("", viewModel.content)
        assertFalse(viewModel.isTodo)
        assertFalse(viewModel.isCompleted)
        assertNull(viewModel.lastEdited)
    }

    @Test
    fun saveNewNoteSavesToRepositoryAndEmitsCloseEvent() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(NOTE_ID_ARG to NEW_NOTE_ID))
        val viewModel = NoteEditViewModel(savedStateHandle, repository)

        viewModel.onTitleChange("New Title")
        viewModel.onContentChange("New Content")
        viewModel.saveAndClose()

        val closeEvent = viewModel.closeEvents.first()
        assertEquals(Unit, closeEvent)

        val savedNotes = repository.getNote(1L)
        assertEquals("New Title", savedNotes?.title)
        assertEquals("New Content", savedNotes?.content)
    }

    @Test
    fun saveNewTodoItemSavesChecklist() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(NOTE_ID_ARG to NEW_NOTE_ID))
        val viewModel = NoteEditViewModel(savedStateHandle, repository)

        viewModel.onTitleChange("Groceries")
        viewModel.toggleIsTodo()
        viewModel.addTaskItem("Buy Milk")
        viewModel.addTaskItem("Buy Eggs")

        viewModel.saveAndClose()
        viewModel.closeEvents.first()

        val saved = repository.getNote(1L)
        assertEquals("Groceries", saved?.title)
        assertTrue(saved?.isTodo == true)
        assertTrue(saved?.content?.contains("Buy Milk") == true)
        assertTrue(saved?.content?.contains("Buy Eggs") == true)
    }

    @Test
    fun saveBlankNoteDoesNotSaveToRepository() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf(NOTE_ID_ARG to NEW_NOTE_ID))
        val viewModel = NoteEditViewModel(savedStateHandle, repository)

        viewModel.onTitleChange("   ")
        viewModel.onContentChange("   ")
        viewModel.saveAndClose()

        viewModel.closeEvents.first()
        assertNull(repository.getNote(1L))
    }

    @Test
    fun loadAndEditExistingNote() = runTest {
        val note = Note(id = 1L, title = "Old Title", content = "Old Content", createdAt = 100L, updatedAt = 100L)
        repository.saveNote(note)

        val savedStateHandle = SavedStateHandle(mapOf(NOTE_ID_ARG to 1L))
        val viewModel = NoteEditViewModel(savedStateHandle, repository)

        assertEquals("Old Title", viewModel.title)
        assertEquals("Old Content", viewModel.content)

        viewModel.onTitleChange("Updated Title")
        viewModel.saveAndClose()

        val updated = repository.getNote(1L)
        assertEquals("Updated Title", updated?.title)
    }

    @Test
    fun deleteExistingNote() = runTest {
        val note = Note(id = 1L, title = "Title", content = "Content", createdAt = 100L, updatedAt = 100L)
        repository.saveNote(note)

        val savedStateHandle = SavedStateHandle(mapOf(NOTE_ID_ARG to 1L))
        val viewModel = NoteEditViewModel(savedStateHandle, repository)

        viewModel.deleteNote()

        viewModel.closeEvents.first()
        assertNull(repository.getNote(1L))
    }
}

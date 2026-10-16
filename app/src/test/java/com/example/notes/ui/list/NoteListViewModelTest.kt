package com.example.notes.ui.list

import androidx.compose.runtime.snapshots.Snapshot
import com.example.notes.data.repository.FakeNoteRepository
import com.example.notes.domain.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeNoteRepository
    private lateinit var viewModel: NoteListViewModel

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
    fun initialStateWithNoNotes() = runTest {
        viewModel = NoteListViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.notes.isEmpty())
        assertFalse(state.hasAnyNotes)
    }

    @Test
    fun filteringNotesByQuery() = runTest {
        repository.saveNote(Note(id = 1L, title = "Shopping List", content = "Milk, Eggs", createdAt = 100L, updatedAt = 100L))
        repository.saveNote(Note(id = 2L, title = "Work Ideas", content = "Kotlin Compose", createdAt = 200L, updatedAt = 200L))

        viewModel = NoteListViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.onQueryChange("Milk")
        Snapshot.sendApplyNotifications()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.notes.size)
        assertEquals("Shopping List", state.notes[0].title)
        assertTrue(state.hasAnyNotes)
    }

    @Test
    fun filteringByNoteFilterType() = runTest {
        repository.saveNote(Note(id = 1L, title = "Note 1", content = "Content", isTodo = false, createdAt = 100L, updatedAt = 100L))
        repository.saveNote(Note(id = 2L, title = "To-Do 1", content = "[ ] Task", isTodo = true, createdAt = 200L, updatedAt = 200L))

        viewModel = NoteListViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.onFilterChange(NoteFilter.TODOS)
        Snapshot.sendApplyNotifications()
        testScheduler.advanceUntilIdle()

        val todosState = viewModel.uiState.value
        assertEquals(1, todosState.notes.size)
        assertEquals("To-Do 1", todosState.notes[0].title)

        viewModel.onFilterChange(NoteFilter.NOTES)
        Snapshot.sendApplyNotifications()
        testScheduler.advanceUntilIdle()

        val notesState = viewModel.uiState.value
        assertEquals(1, notesState.notes.size)
        assertEquals("Note 1", notesState.notes[0].title)
    }

    @Test
    fun deleteAndUndoDeleteNote() = runTest {
        val note = Note(id = 1L, title = "Delete Me", content = "Content", createdAt = 100L, updatedAt = 100L)
        repository.saveNote(note)

        viewModel = NoteListViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        var deletedNoteFromChannel: Note? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.deletedNotes.collect { deletedNoteFromChannel = it }
        }

        viewModel.deleteNote(note)
        testScheduler.advanceUntilIdle()

        assertEquals(note.id, deletedNoteFromChannel?.id)
        assertTrue(viewModel.uiState.value.notes.isEmpty())

        viewModel.undoDelete(note)
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.notes.size)
    }
}

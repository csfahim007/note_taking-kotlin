package com.example.notes.ui.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.data.repository.NoteRepository
import com.example.notes.domain.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NoteFilter {
    ALL, NOTES, TODOS
}

data class NoteListUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = true,
    /** True when the database has notes, even if the current search hides them all. */
    val hasAnyNotes: Boolean = false,
    val selectedFilter: NoteFilter = NoteFilter.ALL
)

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    /** Backed by Compose state so the search field never lags behind the keyboard. */
    var query by mutableStateOf("")
        private set

    var filter by mutableStateOf(NoteFilter.ALL)
        private set

    val uiState: StateFlow<NoteListUiState> = combine(
        repository.observeNotes(),
        snapshotFlow { query },
        snapshotFlow { filter }
    ) { notes, searchText, currentFilter ->
        val trimmed = searchText.trim()
        val filteredByType = when (currentFilter) {
            NoteFilter.ALL -> notes
            NoteFilter.NOTES -> notes.filter { !it.isTodo }
            NoteFilter.TODOS -> notes.filter { it.isTodo }
        }

        val visible = if (trimmed.isEmpty()) {
            filteredByType
        } else {
            filteredByType.filter {
                it.title.contains(trimmed, ignoreCase = true) ||
                    it.content.contains(trimmed, ignoreCase = true)
            }
        }
        NoteListUiState(
            notes = visible,
            isLoading = false,
            hasAnyNotes = notes.isNotEmpty(),
            selectedFilter = currentFilter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NoteListUiState()
    )

    private val deletedChannel = Channel<Note>(Channel.BUFFERED)

    /** Emits each note right after it is deleted, so the UI can offer "Undo". */
    val deletedNotes: Flow<Note> = deletedChannel.receiveAsFlow()

    fun onQueryChange(newQuery: String) {
        query = newQuery
        Snapshot.sendApplyNotifications()
    }

    fun onFilterChange(newFilter: NoteFilter) {
        filter = newFilter
        Snapshot.sendApplyNotifications()
    }

    fun toggleCompletion(note: Note) {
        viewModelScope.launch {
            repository.toggleNoteCompletion(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
            deletedChannel.send(note)
        }
    }

    fun undoDelete(note: Note) {
        viewModelScope.launch { repository.saveNote(note) }
    }
}

package com.example.notes.ui.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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

data class NoteListUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = true,
    /** True when the database has notes, even if the current search hides them all. */
    val hasAnyNotes: Boolean = false
)

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    /** Backed by Compose state so the search field never lags behind the keyboard. */
    var query by mutableStateOf("")
        private set

    val uiState: StateFlow<NoteListUiState> = combine(
        repository.observeNotes(),
        snapshotFlow { query }
    ) { notes, searchText ->
        val trimmed = searchText.trim()
        val visible = if (trimmed.isEmpty()) {
            notes
        } else {
            notes.filter {
                it.title.contains(trimmed, ignoreCase = true) ||
                    it.content.contains(trimmed, ignoreCase = true)
            }
        }
        NoteListUiState(notes = visible, isLoading = false, hasAnyNotes = notes.isNotEmpty())
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
        androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
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

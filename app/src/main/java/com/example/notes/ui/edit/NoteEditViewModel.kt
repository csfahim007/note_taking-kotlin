package com.example.notes.ui.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.data.repository.NoteRepository
import com.example.notes.domain.model.Note
import com.example.notes.ui.navigation.NEW_NOTE_ID
import com.example.notes.ui.navigation.NOTE_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: NoteRepository
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<Long>(NOTE_ID_ARG) ?: NEW_NOTE_ID

    val isNewNote: Boolean = noteId == NEW_NOTE_ID

    var title by mutableStateOf("")
        private set

    var content by mutableStateOf("")
        private set

    /** Epoch millis of the last save; null for a note that has not been saved yet. */
    var lastEdited by mutableStateOf<Long?>(null)
        private set

    /** The note as stored in the database; used to skip saves when nothing changed. */
    private var original: Note? = null
    private var isFinishing = false

    private val closeChannel = Channel<Unit>(Channel.BUFFERED)

    /** Emits once when the screen should close. */
    val closeEvents: Flow<Unit> = closeChannel.receiveAsFlow()

    init {
        if (!isNewNote) {
            viewModelScope.launch {
                repository.getNote(noteId)?.let { note ->
                    original = note
                    title = note.title
                    content = note.content
                    lastEdited = note.updatedAt
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        title = value
    }

    fun onContentChange(value: String) {
        content = value
    }

    /** Saves the note (if it has any text and something changed), then closes the screen. */
    fun saveAndClose() {
        if (isFinishing) return
        isFinishing = true
        viewModelScope.launch {
            val newTitle = title.trim()
            val newContent = content.trim()
            val existing = original
            val isBlank = newTitle.isEmpty() && newContent.isEmpty()
            val unchanged = existing != null &&
                existing.title == newTitle && existing.content == newContent

            if (!isBlank && !unchanged) {
                val now = System.currentTimeMillis()
                repository.saveNote(
                    Note(
                        id = if (isNewNote) 0L else noteId,
                        title = newTitle,
                        content = newContent,
                        createdAt = existing?.createdAt ?: now,
                        updatedAt = now
                    )
                )
            }
            closeChannel.send(Unit)
        }
    }

    fun deleteNote() {
        val existing = original ?: return
        if (isFinishing) return
        isFinishing = true
        viewModelScope.launch {
            repository.deleteNote(existing)
            closeChannel.send(Unit)
        }
    }
}

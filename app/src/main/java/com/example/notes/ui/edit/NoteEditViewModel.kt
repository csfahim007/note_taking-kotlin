package com.example.notes.ui.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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

data class ChecklistItem(
    val id: Long = System.nanoTime(),
    val text: String,
    val isChecked: Boolean = false
)

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

    var isTodo by mutableStateOf(false)
        private set

    var isCompleted by mutableStateOf(false)
        private set

    val checklistItems = mutableStateListOf<ChecklistItem>()

    /** Epoch millis of the last save; null for a note that has not been saved yet. */
    var lastEdited by mutableStateOf<Long?>(null)
        private set

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
                    isTodo = note.isTodo
                    isCompleted = note.isCompleted
                    lastEdited = note.updatedAt

                    if (note.isTodo) {
                        parseContentToChecklist(note.content)
                    }
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

    fun toggleIsTodo() {
        isTodo = !isTodo
        if (isTodo) {
            if (checklistItems.isEmpty()) {
                parseContentToChecklist(content)
            }
            if (checklistItems.isEmpty()) {
                checklistItems.add(ChecklistItem(text = ""))
            }
        } else {
            content = serializeChecklistToContent()
        }
    }

    fun toggleIsCompleted() {
        isCompleted = !isCompleted
        if (isTodo && isCompleted) {
            val updated = checklistItems.map { it.copy(isChecked = true) }
            checklistItems.clear()
            checklistItems.addAll(updated)
        }
    }

    fun toggleTaskItem(index: Int) {
        if (index in checklistItems.indices) {
            val item = checklistItems[index]
            checklistItems[index] = item.copy(isChecked = !item.isChecked)
            updateCompletedStateFromTasks()
        }
    }

    fun addTaskItem(text: String = "") {
        checklistItems.add(ChecklistItem(text = text))
        updateCompletedStateFromTasks()
    }

    fun updateTaskItemText(index: Int, newText: String) {
        if (index in checklistItems.indices) {
            checklistItems[index] = checklistItems[index].copy(text = newText)
        }
    }

    fun removeTaskItem(index: Int) {
        if (index in checklistItems.indices) {
            checklistItems.removeAt(index)
            updateCompletedStateFromTasks()
        }
    }

    private fun parseContentToChecklist(textContent: String) {
        checklistItems.clear()
        if (textContent.isBlank()) return
        val lines = textContent.split("\n")
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("[x] ", ignoreCase = true) || trimmed.startsWith("[x]", ignoreCase = true) -> {
                    val taskText = trimmed.substring(if (trimmed.startsWith("[x] ", ignoreCase = true)) 4 else 3)
                    checklistItems.add(ChecklistItem(text = taskText, isChecked = true))
                }
                trimmed.startsWith("[ ] ") || trimmed.startsWith("[ ]") -> {
                    val taskText = trimmed.substring(if (trimmed.startsWith("[ ] ")) 4 else 3)
                    checklistItems.add(ChecklistItem(text = taskText, isChecked = false))
                }
                trimmed.isNotEmpty() -> {
                    checklistItems.add(ChecklistItem(text = trimmed, isChecked = false))
                }
            }
        }
    }

    private fun serializeChecklistToContent(): String {
        return checklistItems
            .filter { it.text.isNotBlank() }
            .joinToString("\n") { item ->
                if (item.isChecked) "[x] ${item.text}" else "[ ] ${item.text}"
            }
    }

    private fun updateCompletedStateFromTasks() {
        val nonBlankTasks = checklistItems.filter { it.text.isNotBlank() }
        if (nonBlankTasks.isNotEmpty() && nonBlankTasks.all { it.isChecked }) {
            isCompleted = true
        } else if (nonBlankTasks.any { !it.isChecked }) {
            isCompleted = false
        }
    }

    /** Saves the note (if it has any text and something changed), then closes the screen. */
    fun saveAndClose() {
        if (isFinishing) return
        isFinishing = true
        viewModelScope.launch {
            val newTitle = title.trim()
            val newContent = if (isTodo) serializeChecklistToContent() else content.trim()
            val existing = original
            val isBlank = newTitle.isEmpty() && newContent.isEmpty()
            val unchanged = existing != null &&
                existing.title == newTitle &&
                existing.content == newContent &&
                existing.isTodo == isTodo &&
                existing.isCompleted == isCompleted

            if (!isBlank && !unchanged) {
                val now = System.currentTimeMillis()
                repository.saveNote(
                    Note(
                        id = if (isNewNote) 0L else noteId,
                        title = newTitle,
                        content = newContent,
                        isTodo = isTodo,
                        isCompleted = isCompleted,
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

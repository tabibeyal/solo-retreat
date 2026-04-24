package com.soloretreat.ui.integration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soloretreat.data.local.entity.JournalEntry
import com.soloretreat.data.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel() {

    val entries: StateFlow<List<JournalEntry>> = journalRepository.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editing = MutableStateFlow<JournalEntry?>(null)
    val editing: StateFlow<JournalEntry?> = _editing.asStateFlow()

    fun loadEntry(entryId: String) {
        viewModelScope.launch {
            _editing.value = journalRepository.getEntryById(entryId)
        }
    }

    fun clearEditing() {
        _editing.value = null
    }

    fun saveEntry(text: String, tags: String) {
        viewModelScope.launch {
            journalRepository.createEntry(text, tags)
        }
    }

    fun updateEntry(entryId: String, text: String, tags: String) {
        viewModelScope.launch {
            val existing = journalRepository.getEntryById(entryId) ?: return@launch
            journalRepository.updateEntry(
                existing.copy(entryText = text, tags = tags)
            )
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            journalRepository.deleteEntry(entry)
        }
    }
}
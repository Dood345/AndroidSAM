package com.appsters.androidsam.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appsters.androidsam.data.AppDatabase
import com.appsters.androidsam.data.ChatMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).chatMessageDao()
    private val _currentPreset = MutableStateFlow("SAM")

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages = _currentPreset.flatMapLatest { preset ->
        dao.getMessagesForPreset(preset)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setCurrentPreset(presetName: String) {
        _currentPreset.value = presetName
    }

    fun saveMessage(message: ChatMessage) {
        viewModelScope.launch {
            dao.insertMessage(message)
        }
    }
}
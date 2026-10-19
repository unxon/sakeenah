package com.sakinah.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sakinah.reader.data.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = UserPreferences(application)
    val darkMode: StateFlow<Boolean> = preferences.darkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    fun setDarkMode(value: Boolean) { viewModelScope.launch { preferences.setDarkMode(value) } }
}

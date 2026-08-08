package com.current.news.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.current.news.data.SettingsRepository
import com.current.news.data.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    val themeMode: StateFlow<ThemeMode> = repository.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    /** Null = Global (no country filter). */
    val countryCode: StateFlow<String?> = repository.countryCode
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setCountry(code: String?) {
        viewModelScope.launch {
            repository.setCountry(code)
        }
    }
}

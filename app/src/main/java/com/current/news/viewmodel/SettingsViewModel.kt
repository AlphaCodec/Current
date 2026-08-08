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

    /** Country codes the user has chosen to follow. Empty = no filter (general mix). */
    val selectedCountries: StateFlow<Set<String>> = repository.selectedCountries
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    fun setSelectedCountries(codes: Set<String>) {
        viewModelScope.launch {
            repository.setSelectedCountries(codes)
        }
    }
}

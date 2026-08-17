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

    /** Listen (text-to-speech) playback rate — 1.0 is normal speed. */
    val ttsSpeed: StateFlow<Float> = repository.ttsSpeed
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1.0f)

    fun setTtsSpeed(speed: Float) {
        viewModelScope.launch { repository.setTtsSpeed(speed) }
    }

    /** Listen (text-to-speech) pitch — 1.0 is normal pitch. */
    val ttsPitch: StateFlow<Float> = repository.ttsPitch
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1.0f)

    fun setTtsPitch(pitch: Float) {
        viewModelScope.launch { repository.setTtsPitch(pitch) }
    }
}

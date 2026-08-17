package com.current.news.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

private val Context.settingsDataStore by preferencesDataStore(name = "current_settings")

/**
 * Persists the user's Appearance choice (Light / Dark / System), reading
 * preferences (country filter), and Listen (text-to-speech) playback
 * settings across app restarts. Backed by Jetpack DataStore rather than
 * SharedPreferences so reads/writes are async and observable as a Flow.
 */
class SettingsRepository(private val context: Context) {

    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val countryCodeKey = stringPreferencesKey("country_code")
    private val ttsSpeedKey = floatPreferencesKey("tts_speed")
    private val ttsPitchKey = floatPreferencesKey("tts_pitch")

    val themeMode: Flow<ThemeMode> = context.settingsDataStore.data.map { prefs ->
        when (prefs[themeModeKey]) {
            ThemeMode.LIGHT.name -> ThemeMode.LIGHT
            ThemeMode.DARK.name -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }

    /** Null means "Global" — no country filter applied. */
    val countryCode: Flow<String?> = context.settingsDataStore.data.map { prefs ->
        prefs[countryCodeKey]
    }

    suspend fun setCountry(code: String?) {
        context.settingsDataStore.edit { prefs ->
            if (code == null) prefs.remove(countryCodeKey) else prefs[countryCodeKey] = code
        }
    }

    /** Text-to-speech playback rate, where 1.0 is the platform's normal speed. */
    val ttsSpeed: Flow<Float> = context.settingsDataStore.data.map { prefs ->
        prefs[ttsSpeedKey] ?: 1.0f
    }

    suspend fun setTtsSpeed(speed: Float) {
        context.settingsDataStore.edit { prefs -> prefs[ttsSpeedKey] = speed }
    }

    /** Text-to-speech pitch, where 1.0 is the platform's normal pitch. */
    val ttsPitch: Flow<Float> = context.settingsDataStore.data.map { prefs ->
        prefs[ttsPitchKey] ?: 1.0f
    }

    suspend fun setTtsPitch(pitch: Float) {
        context.settingsDataStore.edit { prefs -> prefs[ttsPitchKey] = pitch }
    }
}


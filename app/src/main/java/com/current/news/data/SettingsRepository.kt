package com.current.news.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

/** A country the user can choose to follow, keyed by NewsData.io's `country` code. */
data class Country(val code: String, val displayName: String)

/**
 * Countries offered in Reading preferences. Codes match NewsData.io's
 * `country` query parameter (https://newsdata.io/documentation).
 */
val availableCountries: List<Country> = listOf(
    Country("us", "United States"),
    Country("gb", "United Kingdom"),
    Country("in", "India"),
    Country("ca", "Canada"),
    Country("au", "Australia"),
    Country("de", "Germany"),
    Country("fr", "France"),
    Country("jp", "Japan"),
    Country("cn", "China"),
    Country("br", "Brazil"),
    Country("za", "South Africa"),
    Country("ae", "United Arab Emirates"),
    Country("sg", "Singapore"),
    Country("ng", "Nigeria"),
    Country("mx", "Mexico")
)

private val Context.settingsDataStore by preferencesDataStore(name = "current_settings")

/**
 * Persists the user's Appearance choice (Light / Dark / System) and Reading
 * preferences (followed countries) across app restarts. Backed by Jetpack
 * DataStore rather than SharedPreferences so reads/writes are async and
 * observable as a Flow.
 */
class SettingsRepository(private val context: Context) {

    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val countriesKey = stringSetPreferencesKey("reading_countries")

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

    /** Empty set means "no country filter" — home shows the general/global mix. */
    val selectedCountries: Flow<Set<String>> = context.settingsDataStore.data.map { prefs ->
        prefs[countriesKey].orEmpty()
    }

    suspend fun setSelectedCountries(codes: Set<String>) {
        context.settingsDataStore.edit { prefs ->
            prefs[countriesKey] = codes
        }
    }
}

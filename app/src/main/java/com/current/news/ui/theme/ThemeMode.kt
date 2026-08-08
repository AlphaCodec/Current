package com.current.news.ui.theme

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark");

    companion object {
        fun fromName(name: String?): ThemeMode = values().firstOrNull { it.name == name } ?: SYSTEM
    }
}

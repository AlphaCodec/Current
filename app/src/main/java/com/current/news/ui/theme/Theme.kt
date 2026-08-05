package com.current.news.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CurrentDarkColors = darkColorScheme(
    primary = Red,
    onPrimary = TextHi,
    secondary = Gold,
    background = Ink,
    onBackground = TextHi,
    surface = Ink2,
    onSurface = TextHi,
    surfaceVariant = Ink3,
    onSurfaceVariant = TextMid,
    outline = LineSoft,
    error = Red
)

@Composable
fun CurrentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? android.app.Activity)?.window
        window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
            it.statusBarColor = Ink.toArgb()
            it.navigationBarColor = Ink.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = CurrentDarkColors,
        typography = CurrentTypography,
        content = content
    )
}

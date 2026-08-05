package com.current.news.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
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

private val CurrentLightColors = lightColorScheme(
    primary = Red,
    onPrimary = Color(0xFFFFFFFF),
    secondary = Gold,
    background = LightBg,
    onBackground = LightTextHi,
    surface = LightSurface,
    onSurface = LightTextHi,
    surfaceVariant = LightSurface2,
    onSurfaceVariant = LightTextMid,
    outline = LightLineSoft,
    error = Red
)

/**
 * @param darkTheme resolved boolean (System mode should already be resolved
 * to isSystemInDarkTheme() by the caller before reaching here).
 */
@Composable
fun CurrentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val statusBarColor = if (darkTheme) Ink else LightBg
    if (!view.isInEditMode) {
        val window = (view.context as? android.app.Activity)?.window
        window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
            it.statusBarColor = statusBarColor.toArgb()
            it.navigationBarColor = statusBarColor.toArgb()
        }
    }

    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val materialColors = if (darkTheme) CurrentDarkColors else CurrentLightColors

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = CurrentTypography,
            content = content
        )
    }
}

package com.current.news.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Palette used by the app's "chrome" screens (Home, Explore, Search, Saved,
 * Profile). The article reader intentionally stays on the warm Paper
 * palette regardless of this setting — that's a deliberate reading-mode
 * design choice, not an oversight.
 */
data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val line: Color,
    val lineSoft: Color,
    val textHi: Color,
    val textMid: Color,
    val textLo: Color,
    val red: Color,
    val redDim: Color,
    val redDimText: Color,
    val gold: Color
)

val DarkAppColors = AppColors(
    background = Ink,
    surface = Ink2,
    surfaceVariant = Ink3,
    line = Line,
    lineSoft = LineSoft,
    textHi = TextHi,
    textMid = TextMid,
    textLo = TextLo,
    red = Red,
    redDim = RedDim,
    redDimText = Color(0xFFF3D3D0),
    gold = Gold
)

val LightAppColors = AppColors(
    background = LightBg,
    surface = LightSurface,
    surfaceVariant = LightSurface2,
    line = LightLine,
    lineSoft = LightLineSoft,
    textHi = LightTextHi,
    textMid = LightTextMid,
    textLo = LightTextLo,
    red = Red,
    redDim = RedDimLight,
    redDimText = Color(0xFF8A2A22),
    gold = Color(0xFF9C7A2E)
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

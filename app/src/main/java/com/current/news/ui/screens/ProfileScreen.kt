package com.current.news.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.imageLoader
import com.current.news.data.ThemeMode
import com.current.news.ui.theme.*
import com.current.news.viewmodel.SettingsViewModel

@Composable
fun ProfileScreen(settingsViewModel: SettingsViewModel) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val themeMode by settingsViewModel.themeMode.collectAsState()
    var showAppearanceDialog by remember { mutableStateOf(false) }

    // Bumped after clearing so the cache-size row recomputes below.
    var cacheClearTick by remember { mutableIntStateOf(0) }
    val cacheSizeLabel = remember(cacheClearTick) { formatCacheSize(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text("Profile", fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = colors.textHi)
        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(56.dp).clip(CircleShape).background(colors.surfaceVariant).border(1.dp, colors.lineSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = colors.textMid, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Reader", fontFamily = DisplayFont, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = colors.textHi)
                Text("Member since 2026", fontFamily = MonoFont, fontSize = 10.sp, color = colors.textLo)
            }
        }
        Spacer(Modifier.height(28.dp))

        Text("PREFERENCES", fontFamily = MonoFont, fontSize = 10.5.sp, letterSpacing = 1.sp, color = colors.textLo)
        Spacer(Modifier.height(8.dp))
        SettingsRow(Icons.Outlined.Notifications, "Notifications", "") { }
        SettingsRow(
            icon = Icons.Outlined.DarkMode,
            label = "Appearance",
            value = themeMode.label(),
            onClick = { showAppearanceDialog = true }
        )
        SettingsRow(Icons.Outlined.TextFields, "Reading preferences", "") { }

        Spacer(Modifier.height(24.dp))
        Text("STORAGE", fontFamily = MonoFont, fontSize = 10.5.sp, letterSpacing = 1.sp, color = colors.textLo)
        Spacer(Modifier.height(8.dp))
        SettingsRow(
            icon = Icons.Outlined.CleaningServices,
            label = "Clear image cache",
            value = cacheSizeLabel,
            onClick = {
                val loader = context.imageLoader
                loader.memoryCache?.clear()
                loader.diskCache?.clear()
                cacheClearTick++
            }
        )
        Text(
            "Thumbnails are capped automatically at ~75MB — this just clears it early if you want the space back now.",
            fontFamily = BodyFont,
            fontSize = 11.sp,
            color = colors.textLo,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    if (showAppearanceDialog) {
        AppearanceDialog(
            current = themeMode,
            onDismiss = { showAppearanceDialog = false },
            onSelect = { mode ->
                settingsViewModel.setThemeMode(mode)
                showAppearanceDialog = false
            }
        )
    }
}

private fun formatCacheSize(context: android.content.Context): String {
    val bytes = context.imageLoader.diskCache?.size ?: 0L
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb < 0.1) "Empty" else String.format("%.1f MB", mb)
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
    ThemeMode.SYSTEM -> "System"
}

@Composable
private fun AppearanceDialog(
    current: ThemeMode,
    onDismiss: () -> Unit,
    onSelect: (ThemeMode) -> Unit
) {
    val colors = LocalAppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text("Appearance", fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = colors.textHi)
        },
        text = {
            Column {
                ThemeMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mode) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(mode.label(), fontFamily = BodyFont, fontSize = 14.sp, color = colors.textHi)
                            Text(
                                when (mode) {
                                    ThemeMode.LIGHT -> "Always use the light palette"
                                    ThemeMode.DARK -> "Always use the dark palette"
                                    ThemeMode.SYSTEM -> "Match your device setting"
                                },
                                fontFamily = MonoFont,
                                fontSize = 9.5.sp,
                                color = colors.textLo
                            )
                        }
                        RadioButton(
                            selected = mode == current,
                            onClick = { onSelect(mode) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.red,
                                unselectedColor = colors.lineSoft
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Text(
                "Done",
                fontFamily = MonoFont,
                fontSize = 11.sp,
                color = colors.red,
                modifier = Modifier
                    .clickable { onDismiss() }
                    .padding(12.dp)
            )
        }
    )
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = colors.textMid, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, fontFamily = BodyFont, fontSize = 14.sp, color = colors.textHi)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotEmpty()) {
                Text(value, fontFamily = MonoFont, fontSize = 10.5.sp, color = colors.textLo)
                Spacer(Modifier.width(6.dp))
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = colors.textLo, modifier = Modifier.size(16.dp))
        }
    }
}

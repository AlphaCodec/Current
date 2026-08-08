package com.current.news.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import com.current.news.data.Country
import com.current.news.data.ThemeMode
import com.current.news.data.availableCountries
import com.current.news.ui.theme.*
import com.current.news.viewmodel.SettingsViewModel

@Composable
fun ProfileScreen(settingsViewModel: SettingsViewModel) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val selectedCountries by settingsViewModel.selectedCountries.collectAsState()
    var showAppearanceDialog by remember { mutableStateOf(false) }
    var showCountryDialog by remember { mutableStateOf(false) }

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
        SettingsRow(
            icon = Icons.Outlined.TextFields,
            label = "Reading preferences",
            value = countrySummaryLabel(selectedCountries),
            onClick = { showCountryDialog = true }
        )

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

    if (showCountryDialog) {
        CountryPreferenceDialog(
            selected = selectedCountries,
            onDismiss = { showCountryDialog = false },
            onToggle = { code ->
                val updated = if (code in selectedCountries) {
                    selectedCountries - code
                } else {
                    selectedCountries + code
                }
                settingsViewModel.setSelectedCountries(updated)
            }
        )
    }
}

private fun countrySummaryLabel(selected: Set<String>): String = when (selected.size) {
    0 -> "All regions"
    1 -> availableCountries.firstOrNull { it.code == selected.first() }?.displayName ?: "1 selected"
    else -> "${selected.size} selected"
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

/**
 * Multi-select country picker for Reading preferences. Selecting one or more
 * countries filters the Home feed to news from those regions (via NewsData.io's
 * `country` param); selecting none falls back to the general/global mix.
 */
@Composable
private fun CountryPreferenceDialog(
    selected: Set<String>,
    onDismiss: () -> Unit,
    onToggle: (String) -> Unit
) {
    val colors = LocalAppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Public, contentDescription = null, tint = colors.textMid, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Reading preferences", fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = colors.textHi)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Choose the countries you want news from. Leave everything unchecked for a general mix.",
                    fontFamily = BodyFont,
                    fontSize = 11.5.sp,
                    color = colors.textLo
                )
            }
        },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                items(availableCountries, key = { it.code }) { country ->
                    CountryRow(
                        country = country,
                        checked = country.code in selected,
                        onClick = { onToggle(country.code) }
                    )
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
private fun CountryRow(country: Country, checked: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(country.displayName, fontFamily = BodyFont, fontSize = 14.sp, color = colors.textHi)
        Checkbox(
            checked = checked,
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(
                checkedColor = colors.red,
                uncheckedColor = colors.lineSoft
            )
        )
    }
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

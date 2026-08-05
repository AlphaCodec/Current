package com.current.news.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.current.news.ui.theme.*

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text("Profile", fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextHi)
        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(56.dp).clip(CircleShape).background(Ink3).border(1.dp, LineSoft, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = TextMid, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Reader", fontFamily = DisplayFont, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = TextHi)
                Text("Member since 2026", fontFamily = MonoFont, fontSize = 10.sp, color = TextLo)
            }
        }
        Spacer(Modifier.height(28.dp))

        Text("PREFERENCES", fontFamily = MonoFont, fontSize = 10.5.sp, letterSpacing = 1.sp, color = TextLo)
        Spacer(Modifier.height(8.dp))
        SettingsRow(Icons.Outlined.Notifications, "Notifications")
        SettingsRow(Icons.Outlined.DarkMode, "Appearance")
        SettingsRow(Icons.Outlined.TextFields, "Reading preferences")
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = TextMid, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, fontFamily = BodyFont, fontSize = 14.sp, color = TextHi)
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextLo, modifier = Modifier.size(16.dp))
    }
}

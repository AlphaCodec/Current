package com.current.news.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.current.news.data.Article
import com.current.news.ui.theme.*

enum class AppTab(val route: String, val label: String) {
    Home("home", "Home"),
    Explore("explore", "Explore"),
    Saved("saved", "Saved"),
    Profile("profile", "Profile")
}

@Composable
fun BottomNavBar(current: AppTab, onSelect: (AppTab) -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .border(width = 1.dp, color = colors.line)
            .height(64.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppTab.values().forEach { tab ->
            val active = tab == current
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onSelect(tab) }
                    .padding(8.dp)
            ) {
                val icon = when (tab) {
                    AppTab.Home -> if (active) Icons.Filled.Home else Icons.Outlined.Home
                    AppTab.Explore -> if (active) Icons.Filled.Explore else Icons.Outlined.Explore
                    AppTab.Saved -> if (active) Icons.Filled.Bookmark else Icons.Outlined.Bookmark
                    AppTab.Profile -> if (active) Icons.Filled.Person else Icons.Outlined.Person
                }
                Icon(
                    imageVector = icon,
                    contentDescription = tab.label,
                    tint = if (active) colors.textHi else colors.textLo,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = tab.label.uppercase(),
                    color = if (active) colors.textHi else colors.textLo,
                    fontFamily = MonoFont,
                    fontSize = 8.5.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun Pill(text: String, active: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Text(
        text = text,
        fontFamily = MonoFont,
        fontSize = 10.5.sp,
        letterSpacing = 0.5.sp,
        color = if (active) colors.background else colors.textMid,
        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (active) colors.textHi else Color.Transparent)
            .border(1.dp, if (active) colors.textHi else colors.lineSoft, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
fun GhostChip(text: String, active: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Text(
        text = text.uppercase(),
        fontFamily = MonoFont,
        fontSize = 10.sp,
        letterSpacing = 0.5.sp,
        color = if (active) colors.red else colors.textMid,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) colors.red.copy(alpha = 0.1f) else Color.Transparent)
            .border(1.dp, if (active) colors.red else colors.lineSoft, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
fun SectionLabel(text: String) {
    val colors = LocalAppColors.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text.uppercase(),
            fontFamily = MonoFont,
            fontSize = 10.5.sp,
            letterSpacing = 1.sp,
            color = colors.textLo
        )
        Spacer(Modifier.width(8.dp))
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(colors.lineSoft)
        )
    }
}

@Composable
fun StoryRow(article: Article, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(listOf(article.thumbColorStart, article.thumbColorEnd))
                )
        ) {
            if (!article.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = article.category.uppercase(),
                fontFamily = MonoFont,
                fontSize = 9.5.sp,
                letterSpacing = 0.5.sp,
                color = colors.red
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = article.title,
                fontFamily = DisplayFont,
                fontWeight = FontWeight.Medium,
                fontSize = 14.5.sp,
                lineHeight = 19.sp,
                color = colors.textHi,
                maxLines = 3
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${article.author} · ${article.timeAgo}",
                fontFamily = MonoFont,
                fontSize = 10.sp,
                color = colors.textLo
            )
        }
    }
}

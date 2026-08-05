package com.current.news.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.current.news.data.NewsRepository
import com.current.news.data.Topic
import com.current.news.data.Writer
import com.current.news.ui.components.SectionLabel
import com.current.news.ui.theme.*

@Composable
fun ExploreScreen(onOpenSearch: () -> Unit) {
    val colors = LocalAppColors.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Explore", fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = colors.textHi)
                Box(
                    Modifier.size(32.dp).clip(CircleShape).border(1.dp, colors.lineSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Person, contentDescription = "Profile", tint = colors.textMid, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.lineSoft, RoundedCornerShape(12.dp))
                    .clickable { onOpenSearch() }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.textLo, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(10.dp))
                Text("Search stories, topics, writers", fontFamily = BodyFont, fontSize = 13.sp, color = colors.textLo)
            }
            Spacer(Modifier.height(22.dp))

            SectionLabel("Browse topics")
            Spacer(Modifier.height(12.dp))
        }

        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(166.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(NewsRepository.topics) { topic -> TopicTile(topic) }
            }
            Spacer(Modifier.height(22.dp))
            SectionLabel("Writers to follow")
            Spacer(Modifier.height(12.dp))
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(NewsRepository.writers) { writer -> WriterChip(writer) }
            }
        }
    }
}

@Composable
private fun TopicTile(topic: Topic) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(topic.colorStart, topic.colorEnd)))
            .border(1.dp, colors.lineSoft, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            topic.label,
            fontFamily = DisplayFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.5.sp,
            color = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
private fun WriterChip(writer: Writer) {
    val colors = LocalAppColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Box(
            Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(colors.surfaceVariant, colors.background)))
                .border(1.dp, colors.lineSoft, CircleShape)
        )
        Spacer(Modifier.height(8.dp))
        Text(writer.name, fontFamily = BodyFont, fontWeight = FontWeight.Medium, fontSize = 11.5.sp, color = colors.textHi)
        Text(writer.beat, fontFamily = MonoFont, fontSize = 9.sp, color = colors.textLo)
    }
}

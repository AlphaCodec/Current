package com.current.news.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.current.news.ui.components.GhostChip
import com.current.news.ui.components.StoryRow
import com.current.news.ui.theme.*
import com.current.news.viewmodel.NewsViewModel

@Composable
fun SearchScreen(
    viewModel: NewsViewModel,
    onBack: () -> Unit,
    onOpenArticle: (String) -> Unit
) {
    val state by viewModel.searchState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, LineSoft, CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = TextMid, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Ink3)
                    .border(1.dp, TextHi, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = TextMid, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    if (state.query.isEmpty()) {
                        Text("Search stories, topics, writers", fontFamily = BodyFont, fontSize = 13.sp, color = TextLo)
                    }
                    BasicTextField(
                        value = state.query,
                        onValueChange = { viewModel.updateQuery(it) },
                        singleLine = true,
                        textStyle = TextStyle(fontFamily = BodyFont, fontSize = 13.sp, color = TextHi),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Red),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            Text(
                text = "${state.results.size} results · sorted by relevance",
                fontFamily = MonoFont,
                fontSize = 10.5.sp,
                color = TextLo,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.searchFilters) { filter ->
                    GhostChip(text = filter, active = filter == state.activeFilter) {
                        viewModel.setFilter(filter)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        if (state.query.isNotBlank() && state.results.isEmpty()) {
            item {
                Text(
                    "No stories match \"${state.query}\". Try a different keyword.",
                    fontFamily = BodyFont,
                    fontSize = 13.sp,
                    color = TextMid,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }

        items(state.results) { article ->
            StoryRow(article) { onOpenArticle(article.id) }
        }
    }
}

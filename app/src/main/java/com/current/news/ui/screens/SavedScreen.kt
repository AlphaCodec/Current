package com.current.news.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.current.news.ui.components.StoryRow
import com.current.news.ui.theme.*
import com.current.news.viewmodel.NewsViewModel

@Composable
fun SavedScreen(
    viewModel: NewsViewModel,
    onOpenArticle: (String) -> Unit
) {
    val saved by viewModel.savedArticles.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text("Saved", fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextHi)
        Spacer(Modifier.height(4.dp))
        Text(
            "${saved.size} ${if (saved.size == 1) "story" else "stories"} saved for later",
            fontFamily = MonoFont,
            fontSize = 10.5.sp,
            color = TextLo
        )
        Spacer(Modifier.height(16.dp))

        if (saved.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, tint = TextLo, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(12.dp))
                Text("Nothing saved yet", fontFamily = DisplayFont, fontSize = 15.sp, color = TextHi)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Tap the bookmark icon on any story to keep it here.",
                    fontFamily = BodyFont,
                    fontSize = 12.5.sp,
                    color = TextLo
                )
            }
        } else {
            LazyColumn {
                items(saved) { article ->
                    StoryRow(article) { onOpenArticle(article.id) }
                }
            }
        }
    }
}

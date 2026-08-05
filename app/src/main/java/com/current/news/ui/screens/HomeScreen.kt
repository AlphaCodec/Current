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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.current.news.data.Article
import com.current.news.ui.components.Pill
import com.current.news.ui.components.SectionLabel
import com.current.news.ui.components.StoryRow
import com.current.news.ui.theme.*
import com.current.news.viewmodel.NewsViewModel

@Composable
fun HomeScreen(
    viewModel: NewsViewModel,
    onOpenArticle: (String) -> Unit
) {
    val state by viewModel.homeState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Text("Cur", fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextHi)
                    Text("rent", fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Red)
                }
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, LineSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Person, contentDescription = "Profile", tint = TextMid, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.editions) { edition ->
                    Pill(text = edition, active = edition == state.selectedEdition) {
                        viewModel.selectEdition(edition)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(RedDim)
                    .border(1.dp, Color(0xFF7A2B26), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Red)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("LIVE NOW", fontFamily = MonoFont, fontSize = 9.5.sp, letterSpacing = 0.6.sp, color = Red)
                    Spacer(Modifier.height(2.dp))
                    Text(state.liveHeadline, fontFamily = BodyFont, fontSize = 12.sp, color = Color(0xFFF3D3D0), fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        state.hero?.let { hero ->
            item {
                HeroCard(hero) { onOpenArticle(hero.id) }
                Spacer(Modifier.height(20.dp))
            }
        }

        item {
            SectionLabel("Top stories")
            Spacer(Modifier.height(4.dp))
        }

        items(state.stories) { article ->
            StoryRow(article) { onOpenArticle(article.id) }
        }
    }
}

@Composable
private fun HeroCard(article: Article, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF2B2F36), Color(0xFF14161A))))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(article.category.uppercase(), fontFamily = MonoFont, fontSize = 10.sp, letterSpacing = 0.6.sp, color = Gold)
            Spacer(Modifier.height(8.dp))
            Text(
                article.title,
                fontFamily = DisplayFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 26.sp,
                color = Color.White
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "${article.author} · ${article.readTime} · ${article.timeAgo}",
                fontFamily = MonoFont,
                fontSize = 10.5.sp,
                color = Color.White.copy(alpha = 0.55f)
            )
        }
    }
}

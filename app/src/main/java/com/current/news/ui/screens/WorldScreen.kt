package com.current.news.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.current.news.ui.components.InfiniteScrollHandler
import com.current.news.ui.components.AutoLoadMoreWhenContentFits
import com.current.news.ui.components.LoadMoreErrorFooter
import com.current.news.ui.components.LoadingMoreFooter
import com.current.news.ui.components.ScrollToTopButton
import com.current.news.ui.components.SectionLabel
import com.current.news.ui.components.StoryRow
import com.current.news.ui.theme.*
import com.current.news.viewmodel.NewsViewModel

/**
 * Replaces the old Explore tab. Rather than re-presenting the same
 * categories Home already filters by, this is a deliberately different
 * job: one unfiltered, chronological stream of everything the source has —
 * no category narrowing — plus quick access to Search. Respects the
 * country reading preference set in Profile, same as Home.
 */
@Composable
fun WorldScreen(
    viewModel: NewsViewModel,
    onOpenSearch: () -> Unit,
    onOpenArticle: (String) -> Unit
) {
    val state by viewModel.worldState.collectAsState()
    val colors = LocalAppColors.current
    val listState = rememberLazyListState()

    InfiniteScrollHandler(listState = listState) { viewModel.loadMoreWorld() }
    AutoLoadMoreWhenContentFits(
        listState = listState,
        contentSignal = state.stories.size,
        isLoading = state.isLoading,
        isLoadingMore = state.isLoadingMore,
        canLoadMore = state.canLoadMore
    ) { viewModel.loadMoreWorld() }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
        state = listState,
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
                Text("World", fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = colors.textHi)
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

            if (state.usingSampleData) {
                Text(
                    "Showing sample data — add a free NEWSDATA_API_KEY to load real news. See README.",
                    fontFamily = MonoFont,
                    fontSize = 9.5.sp,
                    color = colors.gold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            SectionLabel("Everything, unfiltered")
            Spacer(Modifier.height(4.dp))
        }

        if (state.isLoading && state.stories.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.red, modifier = Modifier.size(28.dp))
                }
            }
            return@LazyColumn
        }

        if (state.error != null && state.stories.isEmpty()) {
            item {
                WorldErrorState(message = state.error!!, onRetry = { viewModel.retryWorld() })
            }
            return@LazyColumn
        }

        items(state.stories, key = { it.id }) { article ->
            StoryRow(article) { onOpenArticle(article.id) }
        }

        if (state.isLoadingMore) {
            item { LoadingMoreFooter() }
        } else if (state.loadMoreError != null) {
            item {
                LoadMoreErrorFooter(message = state.loadMoreError!!) { viewModel.loadMoreWorld() }
            }
        } else if (!state.canLoadMore && state.stories.isNotEmpty()) {
            item {
                Text(
                    "You're all caught up",
                    fontFamily = MonoFont,
                    fontSize = 10.5.sp,
                    color = colors.textLo,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        }

        ScrollToTopButton(
            listState = listState,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
    }
}

@Composable
private fun WorldErrorState(message: String, onRetry: () -> Unit) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Couldn't load news", fontFamily = DisplayFont, fontSize = 16.sp, color = colors.textHi)
        Spacer(Modifier.height(6.dp))
        Text(message, fontFamily = BodyFont, fontSize = 12.5.sp, color = colors.textMid, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(
            "RETRY",
            fontFamily = MonoFont,
            fontSize = 11.sp,
            letterSpacing = 0.6.sp,
            color = colors.red,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, colors.red, RoundedCornerShape(8.dp))
                .clickable { onRetry() }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

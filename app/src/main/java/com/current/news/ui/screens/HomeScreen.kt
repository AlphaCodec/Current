package com.current.news.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.current.news.data.Article
import com.current.news.ui.components.Pill
import com.current.news.ui.components.SectionLabel
import com.current.news.ui.components.StoryRow
import com.current.news.ui.components.InfiniteScrollHandler
import com.current.news.ui.components.LoadingMoreFooter
import com.current.news.ui.components.LoadMoreErrorFooter
import com.current.news.ui.components.ScrollToTopButton
import com.current.news.ui.theme.*
import com.current.news.viewmodel.HomeUiState
import com.current.news.viewmodel.NewsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: NewsViewModel,
    onOpenArticle: (String) -> Unit
) {
    val state by viewModel.homeState.collectAsState()
    val colors = LocalAppColors.current
    val listState = rememberLazyListState()

    InfiniteScrollHandler(listState = listState) { viewModel.loadMoreHome() }

    Box(Modifier.fillMaxSize()) {
        HomeFeedList(
            state = state,
            listState = listState,
            colors = colors,
            viewModel = viewModel,
            onOpenArticle = onOpenArticle
        )

        ScrollToTopButton(
            listState = listState,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        )
    }
}

@Composable
private fun HomeFeedList(
    state: HomeUiState,
    listState: LazyListState,
    colors: AppColors,
    viewModel: NewsViewModel,
    onOpenArticle: (String) -> Unit
) {
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
                Row {
                    Text("Cur", fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = colors.textHi)
                    Text("rent", fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = colors.red)
                }
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.dp, colors.lineSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Person, contentDescription = "Profile", tint = colors.textMid, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(18.dp))
        }

        if (state.usingSampleData) {
            item {
                Text(
                    "Showing sample data — add a free NEWSDATA_API_KEY to load real news. See README.",
                    fontFamily = MonoFont,
                    fontSize = 9.5.sp,
                    color = colors.gold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
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
                ErrorState(message = state.error!!, onRetry = { viewModel.retryHome() })
            }
            return@LazyColumn
        }

        state.justInHeadline?.let { headline ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.redDim)
                        .border(1.dp, colors.red.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(colors.red)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("JUST IN", fontFamily = MonoFont, fontSize = 9.5.sp, letterSpacing = 0.6.sp, color = colors.red)
                        Spacer(Modifier.height(2.dp))
                        Text(headline, fontFamily = BodyFont, fontSize = 12.sp, color = colors.redDimText, fontWeight = FontWeight.Medium, maxLines = 2)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        if (state.featured.isNotEmpty()) {
            item {
                FeaturedCarousel(featured = state.featured, onOpenArticle = onOpenArticle)
                Spacer(Modifier.height(20.dp))
            }
        }

        item {
            SectionLabel("Top stories")
            Spacer(Modifier.height(4.dp))
        }

        items(state.stories, key = { it.id }) { article ->
            StoryRow(article) { onOpenArticle(article.id) }
        }

        if (state.isLoadingMore) {
            item { LoadingMoreFooter() }
        } else if (state.loadMoreError != null) {
            item {
                LoadMoreErrorFooter(message = state.loadMoreError!!) { viewModel.loadMoreHome() }
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
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Couldn't load news", fontFamily = DisplayFont, fontSize = 16.sp, color = colors.textHi)
        Spacer(Modifier.height(6.dp))
        Text(message, fontFamily = BodyFont, fontSize = 12.5.sp, color = colors.textMid)
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

@Composable
private fun FeaturedCarousel(featured: List<Article>, onOpenArticle: (String) -> Unit) {
    val colors = LocalAppColors.current
    val pagerState = rememberPagerState(pageCount = { featured.size })
    val scope = rememberCoroutineScope()

    // Gentle auto-advance — but it backs off the moment the user actually
    // touches the carousel, so "interactive control" always wins over the
    // automatic rotation rather than fighting it.
    LaunchedEffect(pagerState, featured.size) {
        if (featured.size <= 1) return@LaunchedEffect
        while (true) {
            delay(5000)
            if (!pagerState.isScrollInProgress) {
                val next = (pagerState.currentPage + 1) % featured.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val article = featured[page]
            HeroCard(article) { onOpenArticle(article.id) }
        }

        if (featured.size > 1) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                featured.indices.forEach { index ->
                    val active = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (active) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (active) colors.red else colors.lineSoft)
                            .clickable {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCard(article: Article, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(article.thumbColorStart, article.thumbColorEnd)))
            .clickable { onClick() }
    ) {
        if (!article.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        // Scrim so the headline stays legible over a photographic image.
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))))
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(article.category.uppercase(), fontFamily = MonoFont, fontSize = 10.sp, letterSpacing = 0.6.sp, color = colors.gold)
            Spacer(Modifier.height(8.dp))
            Text(
                article.title,
                fontFamily = DisplayFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                lineHeight = 26.sp,
                color = Color.White,
                maxLines = 3
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "${article.sourceName} · ${article.readTime} · ${article.timeAgo}",
                fontFamily = MonoFont,
                fontSize = 10.5.sp,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

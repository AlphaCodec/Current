package com.current.news.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
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
import kotlinx.coroutines.launch

enum class AppTab(val route: String, val label: String) {
    Home("home", "Home"),
    World("world", "World"),
    Saved("saved", "Saved"),
    Profile("profile", "Profile")
}

/**
 * Watches [listState] and calls [onLoadMore] whenever the user is scrolled
 * to within [buffer] items of the end of the list. Drop this inside any
 * screen alongside its LazyColumn; it renders nothing itself.
 *
 * Deliberately does NOT de-duplicate consecutive "at the bottom" signals:
 * if a previous load-more attempt failed, we still want a later scroll
 * event near the bottom to try again rather than latching permanently.
 * The real duplicate-call guard lives in the ViewModel (isLoading /
 * isLoadingMore / canLoadMore checks), which is cheap to call redundantly.
 */
/**
 * Watches [listState] and calls [onLoadMore] whenever the user is scrolled
 * to within [buffer] items of the end of the list. Drop this inside any
 * screen alongside its LazyColumn; it renders nothing itself.
 *
 * The check is computed directly inside snapshotFlow's block rather than
 * through an extra `remember { derivedStateOf {} }` layer — snapshotFlow
 * already reactively re-evaluates its block whenever a snapshot state it
 * reads (listState.layoutInfo here) changes, so the derivedStateOf wrapper
 * was redundant indirection, and specifically the kind that can miss the
 * very first big state transition (loading spinner -> fully populated
 * list) while still recovering fine on any later recomposition. That
 * mismatch — works after any unrelated interaction, not on first load —
 * is the exact bug this was causing.
 *
 * Deliberately does NOT de-duplicate consecutive "at the bottom" signals:
 * if a previous load-more attempt failed, we still want a later scroll
 * event near the bottom to try again rather than latching permanently.
 * The real duplicate-call guard lives in the ViewModel (isLoading /
 * isLoadingMore / canLoadMore checks), which is cheap to call redundantly.
 */
@Composable
fun InfiniteScrollHandler(
    listState: LazyListState,
    buffer: Int = 4,
    onLoadMore: () -> Unit
) {
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 1 - buffer
        }.collect { atEnd -> if (atEnd) onLoadMore() }
    }
}

/** Small footer row shown at the bottom of an infinite-scroll list while more results load. */
@Composable
fun LoadingMoreFooter() {
    val colors = LocalAppColors.current
    Box(
        Modifier.fillMaxWidth().padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = colors.red, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
    }
}

/** Footer shown when a load-more request failed — explicit, tappable retry. */
@Composable
fun LoadMoreErrorFooter(message: String, onRetry: () -> Unit) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, fontFamily = BodyFont, fontSize = 11.5.sp, color = colors.textMid, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "TAP TO RETRY",
            fontFamily = MonoFont,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp,
            color = colors.red,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, colors.red, RoundedCornerShape(8.dp))
                .clickable { onRetry() }
                .padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
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
                    AppTab.World -> if (active) Icons.Filled.Public else Icons.Outlined.Public
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

/**
 * A floating circular button that fades/scales in once the user has
 * scrolled a bit into a list, and animates the list back to the top when
 * tapped. Drop it inside a Box alongside the list it controls, aligned to
 * BottomEnd.
 */
@Composable
fun ScrollToTopButton(listState: LazyListState, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val scope = rememberCoroutineScope()

    // Only show once there's meaningfully something to scroll back past —
    // avoids flashing in for a two-item list that barely scrolls at all.
    val visible by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 3 }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.textHi)
                .clickable {
                    scope.launch { listState.animateScrollToItem(0) }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.KeyboardArrowUp,
                contentDescription = "Scroll to top",
                tint = colors.background,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

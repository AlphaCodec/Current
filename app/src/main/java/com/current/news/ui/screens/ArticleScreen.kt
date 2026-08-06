package com.current.news.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.current.news.ui.theme.*
import com.current.news.viewmodel.NewsViewModel

@Composable
fun ArticleScreen(
    articleId: String,
    viewModel: NewsViewModel,
    onBack: () -> Unit
) {
    val article = viewModel.article(articleId)
    val savedIds by viewModel.savedArticles.collectAsState()
    val isSaved = savedIds.any { it.id == articleId }
    val context = LocalContext.current

    if (article == null) {
        Box(Modifier.fillMaxSize().background(Paper), contentAlignment = Alignment.Center) {
            Text("Story not found", color = PaperMuted, fontFamily = BodyFont)
        }
        return
    }

    val listState = rememberLazyListState()

    Column(Modifier.fillMaxSize().background(Paper)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ReaderIconButton(text = "←", onClick = onBack)
            ReaderIconButton(text = "Aa", onClick = { })
        }

        // Reading progress indicator, driven by scroll offset.
        val progress = (listState.firstVisibleItemIndex.toFloat() /
            (article.body.size + 3).coerceAtLeast(1)).coerceIn(0f, 1f)
        Box(Modifier.fillMaxWidth().height(2.dp).background(PaperLine)) {
            Box(
                Modifier
                    .fillMaxWidth(fraction = (progress + 0.12f).coerceAtMost(1f))
                    .height(2.dp)
                    .background(Red)
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 22.dp),
            contentPadding = PaddingValues(top = 22.dp, bottom = 24.dp)
        ) {
            item {
                Text(article.category.uppercase(), fontFamily = MonoFont, fontSize = 10.5.sp, letterSpacing = 0.6.sp, color = Red)
                Spacer(Modifier.height(12.dp))
                Text(
                    article.title,
                    fontFamily = DisplayFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 27.sp,
                    lineHeight = 32.sp,
                    color = PaperInk
                )
                Spacer(Modifier.height(14.dp))
                Text(article.dek, fontFamily = BodyFont, fontSize = 14.5.sp, lineHeight = 22.sp, color = Color(0xFF57534A))
                Spacer(Modifier.height(18.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                        .border(width = 0.dp, color = Color.Transparent)
                ) {
                    Box(Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFC8BFA8)))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(article.author, fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = PaperInk)
                        Text("${article.sourceName} · ${article.readTime} · ${article.timeAgo}", fontFamily = MonoFont, fontSize = 10.sp, color = PaperMuted)
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(PaperLine))
                Spacer(Modifier.height(18.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF8F8A78), Color(0xFF5B5648))))
                ) {
                    if (!article.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = article.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                if (article.caption.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(article.caption, fontFamily = MonoFont, fontSize = 10.sp, color = PaperMuted)
                }
                Spacer(Modifier.height(20.dp))
            }

            itemsIndexed(article.body)

            if (!article.articleUrl.isNullOrBlank()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "READ FULL STORY AT ${article.sourceName.uppercase()} →",
                        fontFamily = MonoFont,
                        fontSize = 10.5.sp,
                        letterSpacing = 0.4.sp,
                        color = Red,
                        modifier = Modifier
                            .clickable {
                                val colorParams = CustomTabColorSchemeParams.Builder()
                                    .setToolbarColor(android.graphics.Color.parseColor("#F7F3EC")) // Paper
                                    .build()
                                val customTabsIntent = CustomTabsIntent.Builder()
                                    .setDefaultColorSchemeParams(colorParams)
                                    .setShowTitle(true)
                                    .build()
                                customTabsIntent.launchUrl(context, android.net.Uri.parse(article.articleUrl))
                            }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .border(width = 1.dp, color = PaperLine)
                .background(Paper)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReaderToolItem(
                icon = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.Bookmark,
                label = "Save",
                tint = if (isSaved) Red else Color(0xFF8A8478)
            ) { viewModel.toggleSave(article) }
            ReaderToolItem(icon = Icons.Outlined.Share, label = "Share") {
                val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, "${article.title}\n${article.articleUrl ?: ""}")
                }
                context.startActivity(android.content.Intent.createChooser(sendIntent, null))
            }
            ReaderToolItem(icon = Icons.Outlined.Headphones, label = "Listen") { }
            ReaderToolItem(icon = Icons.Outlined.MoreHoriz, label = "More") { }
        }
    }
}

private fun LazyListScope.itemsIndexed(paragraphs: List<String>) {
    paragraphs.forEachIndexed { index, paragraph ->
        item {
            if (index == 0) {
                DropCapParagraph(paragraph)
            } else {
                Text(
                    paragraph,
                    fontFamily = BodyFont,
                    fontSize = 14.5.sp,
                    lineHeight = 25.sp,
                    color = PaperBody,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }
        }
    }
}

@Composable
private fun DropCapParagraph(paragraph: String) {
    val first = paragraph.firstOrNull()?.toString() ?: ""
    val rest = paragraph.drop(1)
    val annotated = buildAnnotatedString {
        withStyle(
            SpanStyle(
                fontFamily = DisplayFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 30.sp,
                color = PaperInk
            )
        ) { append(first) }
        withStyle(
            SpanStyle(
                fontFamily = BodyFont,
                fontSize = 14.5.sp,
                color = PaperBody
            )
        ) { append(rest) }
    }
    BasicText(text = annotated, modifier = Modifier.padding(bottom = 14.dp))
}

@Composable
private fun ReaderIconButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .border(1.dp, PaperLine, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontFamily = BodyFont, fontSize = 13.sp, color = Color(0xFF57534A))
    }
}

@Composable
private fun ReaderToolItem(
    icon: ImageVector,
    label: String,
    tint: Color = Color(0xFF8A8478),
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(8.dp)) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(3.dp))
        Text(label.uppercase(), fontFamily = MonoFont, fontSize = 8.sp, color = Color(0xFF8A8478))
    }
}

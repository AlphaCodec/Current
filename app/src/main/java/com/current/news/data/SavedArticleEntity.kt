package com.current.news.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * [Article] holds Compose `Color` values, which don't round-trip cleanly
 * through Gson (JSON) serialization. This is a plain, JSON-friendly mirror
 * used only for persisting bookmarks to disk — colors stored as ARGB ints.
 */
data class SavedArticleEntity(
    val id: String,
    val category: String,
    val title: String,
    val dek: String,
    val author: String,
    val sourceName: String,
    val timeAgo: String,
    val readTime: String,
    val publishedAtMillis: Long,
    val imageUrl: String?,
    val sourceIconUrl: String?,
    val articleUrl: String?,
    val thumbColorStartArgb: Int,
    val thumbColorEndArgb: Int,
    val body: List<String>,
    val caption: String
)

fun Article.toEntity(): SavedArticleEntity = SavedArticleEntity(
    id = id,
    category = category,
    title = title,
    dek = dek,
    author = author,
    sourceName = sourceName,
    timeAgo = timeAgo,
    readTime = readTime,
    publishedAtMillis = publishedAtMillis,
    imageUrl = imageUrl,
    sourceIconUrl = sourceIconUrl,
    articleUrl = articleUrl,
    thumbColorStartArgb = thumbColorStart.toArgb(),
    thumbColorEndArgb = thumbColorEnd.toArgb(),
    body = body,
    caption = caption
)

fun SavedArticleEntity.toArticle(): Article = Article(
    id = id,
    category = category,
    title = title,
    dek = dek,
    author = author,
    sourceName = sourceName,
    timeAgo = timeAgo,
    readTime = readTime,
    publishedAtMillis = publishedAtMillis,
    imageUrl = imageUrl,
    sourceIconUrl = sourceIconUrl,
    articleUrl = articleUrl,
    isHero = false,
    thumbColorStart = Color(thumbColorStartArgb),
    thumbColorEnd = Color(thumbColorEndArgb),
    body = body,
    caption = caption
)

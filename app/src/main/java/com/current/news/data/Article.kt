package com.current.news.data

import androidx.compose.ui.graphics.Color

/**
 * Core content model for a single story. Populated either from the
 * NewsData.io API (see NewsRepository) or, when no API key is configured
 * or the network call fails, from a small bundled sample set so the app
 * still runs and demonstrates its UI.
 */
data class Article(
    val id: String,
    val category: String,
    val title: String,
    val dek: String,
    val author: String,
    val sourceName: String,
    val timeAgo: String,
    val readTime: String,
    val publishedAtMillis: Long,
    val imageUrl: String? = null,
    val sourceIconUrl: String? = null,
    val articleUrl: String? = null,
    val isHero: Boolean = false,
    val thumbColorStart: Color,
    val thumbColorEnd: Color,
    val body: List<String>,
    val caption: String = ""
)

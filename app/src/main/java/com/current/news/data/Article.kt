package com.current.news.data

import androidx.compose.ui.graphics.Color

/**
 * Core content model for a single story.
 */
data class Article(
    val id: String,
    val category: String,
    val title: String,
    val dek: String,
    val author: String,
    val timeAgo: String,
    val readTime: String,
    val isLive: Boolean = false,
    val isHero: Boolean = false,
    val thumbColorStart: Color,
    val thumbColorEnd: Color,
    val body: List<String>,
    val caption: String = ""
)

data class Writer(
    val id: String,
    val name: String,
    val beat: String
)

data class Topic(
    val id: String,
    val label: String,
    val colorStart: Color,
    val colorEnd: Color
)

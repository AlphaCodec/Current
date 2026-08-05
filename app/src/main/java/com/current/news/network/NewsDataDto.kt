package com.current.news.network

import com.google.gson.annotations.SerializedName

/**
 * Response shape for https://newsdata.io/api/1/latest
 */
data class NewsDataResponse(
    val status: String,
    val totalResults: Int? = null,
    val results: List<NewsDataArticleDto>? = null,
    val nextPage: String? = null,
    // Present when status == "error"
    val results_message: String? = null
)

data class NewsDataArticleDto(
    @SerializedName("article_id") val articleId: String,
    val title: String?,
    val link: String?,
    val description: String?,
    val content: String?,
    @SerializedName("pubDate") val pubDate: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("source_id") val sourceId: String?,
    @SerializedName("source_name") val sourceName: String?,
    val creator: List<String>?,
    val category: List<String>?,
    val country: List<String>?,
    val language: String?
)

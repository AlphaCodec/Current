package com.current.news.data

import androidx.compose.ui.graphics.Color
import com.current.news.BuildConfig
import com.current.news.network.NewsDataArticleDto
import com.current.news.network.NewsDataClient
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.min

sealed class RepoResult<out T> {
    data class Success<T>(val data: T) : RepoResult<T>()
    data class Error(val message: String) : RepoResult<Nothing>()
}

data class ArticlesPage(
    val articles: List<Article>,
    val nextPageToken: String?
)

/**
 * Talks to the NewsData.io free API (see network/NewsDataApi.kt). If no API
 * key is configured, or the request fails (offline, rate limit, etc.), this
 * falls back to a small bundled sample set — so the app is always runnable,
 * and clearly signals which mode it's in via [Article] having empty
 * [Article.imageUrl]/live network data vs sample gradients.
 */
object NewsRepository {

    /**
     * Display label -> NewsData.io `category` query value.
     * All 17 categories NewsData.io's `/latest` endpoint supports (verified
     * against their docs), plus "For you" as an alias for their curated
     * "top" mix. Ordered roughly by how often a general reader would reach
     * for them — the LazyRow this backs scrolls horizontally, so nothing
     * here needs trimming to fit.
     */
    val editions: List<Pair<String, String?>> = listOf(
        "For you" to "top",
        "World" to "world",
        "Politics" to "politics",
        "Business" to "business",
        "Technology" to "technology",
        "Science" to "science",
        "Health" to "health",
        "Sports" to "sports",
        "Entertainment" to "entertainment",
        "Environment" to "environment",
        "Food" to "food",
        "Lifestyle" to "lifestyle",
        "Travel" to "tourism",
        "Crime" to "crime",
        "Education" to "education",
        "Domestic" to "domestic",
        "Other" to "other"
    )

    val hasApiKey: Boolean get() = BuildConfig.NEWSDATA_API_KEY.isNotBlank()

    suspend fun fetchArticles(
        category: String? = null,
        query: String? = null,
        country: String? = null,
        page: String? = null
    ): RepoResult<ArticlesPage> {
        if (!hasApiKey) {
            // Sample data doesn't paginate or filter by country — one page, no nextPageToken.
            return RepoResult.Success(ArticlesPage(sampleArticles(category), nextPageToken = null))
        }
        return try {
            val response = NewsDataClient.api.getLatest(
                apiKey = BuildConfig.NEWSDATA_API_KEY,
                category = category,
                query = query,
                country = country,
                page = page
            )
            if (response.status == "success") {
                val mapped = response.results.orEmpty().mapIndexedNotNull { index, dto ->
                    // Only the very first page's first article is treated as "hero".
                    dto.toArticle(isHero = page == null && index == 0)
                }
                if (mapped.isEmpty() && page == null) {
                    RepoResult.Error("No results found.")
                } else {
                    RepoResult.Success(ArticlesPage(mapped, response.nextPage))
                }
            } else {
                RepoResult.Error(response.results_message ?: "The news service returned an error.")
            }
        } catch (e: java.io.IOException) {
            RepoResult.Error("Couldn't reach the network. Check your connection and try again.")
        } catch (e: retrofit2.HttpException) {
            val message = when (e.code()) {
                401, 403 -> "That API key was rejected. Double-check NEWSDATA_API_KEY."
                429 -> "Daily request limit reached on the free plan. Try again tomorrow."
                else -> "The news service returned an error (HTTP ${e.code()})."
            }
            RepoResult.Error(message)
        } catch (e: Exception) {
            RepoResult.Error("Something went wrong loading news: ${e.message ?: "unknown error"}")
        }
    }

    // ---- Mapping ----

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun NewsDataArticleDto.toArticle(isHero: Boolean): Article? {
        val safeTitle = title?.trim().orEmpty()
        if (safeTitle.isEmpty()) return null

        val publishedAtMillis = pubDate?.let {
            try { dateFormat.parse(it)?.time } catch (e: Exception) { null }
        } ?: System.currentTimeMillis()

        val categoryLabel = category?.firstOrNull()
            ?.replaceFirstChar { c -> c.uppercase() }
            ?: "News"

        val dek = description?.trim().orEmpty().ifBlank { "Tap to read the full story at the source." }
        val bodyText = content?.trim()?.takeIf { it.isNotBlank() && it != "ONLY AVAILABLE IN PAID PLANS" }
        val body = when {
            bodyText != null -> bodyText.split(Regex("\n+")).filter { it.isNotBlank() }
            dek.isNotBlank() -> listOf(dek)
            else -> listOf("Full story available at the source.")
        }

        val wordCount = (bodyText ?: dek).split(Regex("\\s+")).size
        val minutes = max(1, min(12, wordCount / 200))

        val (start, end) = gradientFor(articleId)

        return Article(
            id = articleId,
            category = categoryLabel,
            title = safeTitle,
            dek = dek,
            author = creator?.firstOrNull()?.trim()?.takeIf { it.isNotBlank() && it != "null" } ?: "Staff",
            sourceName = sourceName ?: sourceId ?: "Unknown source",
            timeAgo = relativeTime(publishedAtMillis),
            readTime = "$minutes min read",
            publishedAtMillis = publishedAtMillis,
            imageUrl = imageUrl,
            sourceIconUrl = sourceIcon,
            articleUrl = link,
            isHero = isHero,
            thumbColorStart = start,
            thumbColorEnd = end,
            body = body
        )
    }

    private fun gradientFor(seed: String): Pair<Color, Color> {
        val palettes = listOf(
            Color(0xFF3A3D44) to Color(0xFF1B1D21),
            Color(0xFF3A2A2C) to Color(0xFF1A1214),
            Color(0xFF2A3A2E) to Color(0xFF12181B),
            Color(0xFF2E3550) to Color(0xFF14161C),
            Color(0xFF453522) to Color(0xFF1A1610)
        )
        val idx = (seed.hashCode().let { if (it < 0) -it else it }) % palettes.size
        return palettes[idx]
    }

    private fun relativeTime(publishedAtMillis: Long): String {
        val diffMs = System.currentTimeMillis() - publishedAtMillis
        val minutes = diffMs / 60000
        return when {
            minutes < 1 -> "just now"
            minutes < 60 -> "${minutes}m ago"
            minutes < 60 * 24 -> "${minutes / 60}h ago"
            else -> "${minutes / (60 * 24)}d ago"
        }
    }

    // ---- Sample fallback (no API key configured) ----

    private fun sampleArticles(category: String?): List<Article> {
        val now = System.currentTimeMillis()
        val all = listOf(
            Article(
                id = "sample-1",
                category = "Investigation",
                title = "Inside the shipping delays reshaping global retail",
                dek = "Ports from Rotterdam to Long Beach face a backlog that traces back to a single canal closure — and retailers are quietly rewriting their sourcing playbooks.",
                author = "Priya Shah",
                sourceName = "Sample data",
                timeAgo = "12m ago",
                readTime = "6 min read",
                publishedAtMillis = now - 12 * 60_000,
                isHero = true,
                thumbColorStart = Color(0xFF3A3D44),
                thumbColorEnd = Color(0xFF1B1D21),
                caption = "A container ship idles outside the Port of Rotterdam.",
                body = listOf(
                    "This is sample content shown because no NEWSDATA_API_KEY is configured. Add a free key from newsdata.io to see real, live articles here instead.",
                    "Retailers who once prized just-in-time delivery are now paying a premium for redundancy — warehousing closer to demand, diversified shipping lanes, and contracts that price in delay as the default, not the exception."
                )
            ),
            Article(
                id = "sample-2",
                category = "Technology",
                title = "Chipmakers race to shrink the next process node",
                dek = "Sample story — connect a real API key to replace this with live technology headlines.",
                author = "Staff",
                sourceName = "Sample data",
                timeAgo = "40m ago",
                readTime = "4 min read",
                publishedAtMillis = now - 40 * 60_000,
                thumbColorStart = Color(0xFF2E3550),
                thumbColorEnd = Color(0xFF14161C),
                body = listOf("This is placeholder sample content. Configure NEWSDATA_API_KEY to load real technology news.")
            ),
            Article(
                id = "sample-3",
                category = "World",
                title = "Coastal cities test new flood barrier design ahead of monsoon",
                dek = "Sample story — connect a real API key to replace this with live world headlines.",
                author = "Staff",
                sourceName = "Sample data",
                timeAgo = "1h ago",
                readTime = "5 min read",
                publishedAtMillis = now - 60 * 60_000,
                thumbColorStart = Color(0xFF2A3A2E),
                thumbColorEnd = Color(0xFF12181B),
                body = listOf("This is placeholder sample content. Configure NEWSDATA_API_KEY to load real world news.")
            )
        )
        if (category == null || category == "top") return all
        return all.filter { it.category.equals(category, ignoreCase = true) }.ifEmpty { all }
    }
}

package com.current.news.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.current.news.data.Article
import com.current.news.data.NewsRepository
import com.current.news.data.RepoResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val selectedEdition: String = "For you",
    val editions: List<String> = NewsRepository.editions.map { it.first },
    val justInHeadline: String? = null,
    val hero: Article? = null,
    val stories: List<Article> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = false,
    val loadMoreError: String? = null,
    val error: String? = null,
    val usingSampleData: Boolean = !NewsRepository.hasApiKey
)

data class SearchUiState(
    val query: String = "",
    val activeFilter: String = "All",
    val results: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = false,
    val loadMoreError: String? = null,
    val error: String? = null
)

class NewsViewModel : ViewModel() {

    // Every article the app has seen this session, keyed by id — lets the
    // article reader look up a story without a dedicated "get by id" call,
    // and also doubles as our source of truth for de-duplication.
    private val articleCache = HashMap<String, Article>()

    /**
     * NewsData.io's free-tier `/latest` pagination is a moving window, not a
     * stable cursor — as new articles publish, a `nextPage` fetch can hand
     * back an article you already have, so we always dedupe by id. But the
     * more common source of visible duplicates is different: the same wire
     * story (AP/Reuters/etc.) gets syndicated by multiple outlets with
     * different `article_id`s but the same or near-identical headline. So we
     * also dedupe on a normalized title — first-seen wins.
     */
    private fun List<Article>.dedupedBy(existing: List<Article> = emptyList()): List<Article> {
        val seenIds = HashSet<String>()
        val seenTitles = HashSet<String>()
        val result = ArrayList<Article>()
        for (a in existing + this) {
            val titleKey = normalizeTitle(a.title)
            // Only let a normalized title collide with itself if it's long/specific
            // enough to be meaningful — short generic titles ("Live updates") could
            // otherwise false-positive against unrelated stories.
            val isDuplicate = a.id in seenIds || (titleKey.length >= 15 && titleKey in seenTitles)
            if (!isDuplicate) {
                seenIds.add(a.id)
                seenTitles.add(titleKey)
                result.add(a)
            }
        }
        return result
    }

    private fun normalizeTitle(title: String): String =
        title.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "") // strip punctuation so "Fed holds rates." == "Fed holds rates"
            .replace(Regex("\\s+"), " ")
            .trim()

    // ---- Feed ----
    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()
    private var homeNextPage: String? = null
    private var homeCategory: String? = null

    init {
        loadEdition("For you")
    }

    fun selectEdition(edition: String) {
        if (edition == _homeState.value.selectedEdition && !_homeState.value.isLoading) return
        loadEdition(edition)
    }

    fun retryHome() = loadEdition(_homeState.value.selectedEdition)

    private fun loadEdition(edition: String) {
        val categoryQuery = NewsRepository.editions.firstOrNull { it.first == edition }?.second
        homeCategory = categoryQuery
        homeNextPage = null
        _homeState.update {
            it.copy(
                selectedEdition = edition,
                isLoading = true,
                isLoadingMore = false,
                canLoadMore = false,
                loadMoreError = null,
                error = null
            )
        }
        viewModelScope.launch {
            when (val result = NewsRepository.fetchArticles(category = categoryQuery)) {
                is RepoResult.Success -> {
                    val page = result.data
                    val deduped = page.articles.dedupedBy()
                    deduped.forEach { articleCache[it.id] = it }
                    homeNextPage = page.nextPageToken
                    val hero = deduped.firstOrNull { it.isHero } ?: deduped.firstOrNull()
                    val rest = deduped.filterNot { it.id == hero?.id }
                    _homeState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            hero = hero,
                            justInHeadline = rest.firstOrNull()?.title ?: hero?.title,
                            stories = rest,
                            canLoadMore = homeNextPage != null,
                            usingSampleData = !NewsRepository.hasApiKey
                        )
                    }
                }
                is RepoResult.Error -> {
                    _homeState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    /** Called when the home feed scrolls near the bottom. Safe to call repeatedly. */
    fun loadMoreHome() {
        val state = _homeState.value
        if (state.isLoading || state.isLoadingMore || !state.canLoadMore) return
        val nextPage = homeNextPage ?: return

        _homeState.update { it.copy(isLoadingMore = true, loadMoreError = null) }
        viewModelScope.launch {
            when (val result = NewsRepository.fetchArticles(category = homeCategory, page = nextPage)) {
                is RepoResult.Success -> {
                    val page = result.data
                    page.articles.forEach { articleCache[it.id] = it }
                    homeNextPage = page.nextPageToken
                    _homeState.update { current ->
                        val combined = page.articles.dedupedBy(existing = listOfNotNull(current.hero) + current.stories)
                        val hero = current.hero ?: combined.firstOrNull()
                        current.copy(
                            isLoadingMore = false,
                            loadMoreError = null,
                            stories = combined.filterNot { it.id == hero?.id },
                            canLoadMore = homeNextPage != null
                        )
                    }
                }
                is RepoResult.Error -> {
                    // Keep canLoadMore as-is so the retry affordance below stays
                    // visible and a subsequent tap/scroll can try again.
                    _homeState.update { it.copy(isLoadingMore = false, loadMoreError = result.message) }
                }
            }
        }
    }

    // ---- Search ----
    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()
    val searchFilters: List<String> = listOf("All", "Articles", "Opinion")

    private var searchJob: Job? = null
    private var searchNextPage: String? = null

    fun updateQuery(query: String) {
        _searchState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchState.update {
                it.copy(results = emptyList(), isLoading = false, canLoadMore = false, loadMoreError = null, error = null)
            }
            return
        }
        searchJob = viewModelScope.launch {
            delay(400) // debounce so we don't fire a request per keystroke
            runSearch(query)
        }
    }

    fun setFilter(filter: String) {
        _searchState.update { it.copy(activeFilter = filter) }
        if (_searchState.value.query.isNotBlank()) {
            viewModelScope.launch { runSearch(_searchState.value.query) }
        }
    }

    private suspend fun runSearch(query: String) {
        searchNextPage = null
        _searchState.update { it.copy(isLoading = true, loadMoreError = null, error = null) }
        when (val result = NewsRepository.fetchArticles(query = query)) {
            is RepoResult.Success -> {
                val page = result.data
                val deduped = page.articles.dedupedBy()
                deduped.forEach { articleCache[it.id] = it }
                searchNextPage = page.nextPageToken
                val filtered = applyFilter(deduped, _searchState.value.activeFilter)
                _searchState.update {
                    it.copy(
                        isLoading = false,
                        results = filtered,
                        canLoadMore = searchNextPage != null,
                        error = null
                    )
                }
            }
            is RepoResult.Error -> {
                _searchState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    /** Called when the search results list scrolls near the bottom. */
    fun loadMoreSearch() {
        val state = _searchState.value
        if (state.isLoading || state.isLoadingMore || !state.canLoadMore || state.query.isBlank()) return
        val nextPage = searchNextPage ?: return

        _searchState.update { it.copy(isLoadingMore = true, loadMoreError = null) }
        viewModelScope.launch {
            when (val result = NewsRepository.fetchArticles(query = state.query, page = nextPage)) {
                is RepoResult.Success -> {
                    val page = result.data
                    page.articles.forEach { articleCache[it.id] = it }
                    searchNextPage = page.nextPageToken
                    _searchState.update { current ->
                        val combined = page.articles.dedupedBy(existing = current.results)
                        val filtered = applyFilter(combined, current.activeFilter)
                        current.copy(
                            isLoadingMore = false,
                            loadMoreError = null,
                            results = filtered,
                            canLoadMore = searchNextPage != null
                        )
                    }
                }
                is RepoResult.Error -> {
                    _searchState.update { it.copy(isLoadingMore = false, loadMoreError = result.message) }
                }
            }
        }
    }

    private fun applyFilter(list: List<Article>, filter: String): List<Article> = when (filter) {
        "Opinion" -> list.filter { it.category.equals("Opinion", ignoreCase = true) }
        "Articles" -> list.filterNot { it.category.equals("Opinion", ignoreCase = true) }
        else -> list
    }

    // ---- Bookmarks (in-memory for this session) ----
    private val _savedArticles = MutableStateFlow<List<Article>>(emptyList())
    val savedArticles: StateFlow<List<Article>> = _savedArticles.asStateFlow()

    fun isSaved(id: String): Boolean = _savedArticles.value.any { it.id == id }

    fun toggleSave(article: Article) {
        _savedArticles.update { current ->
            if (current.any { it.id == article.id }) {
                current.filterNot { it.id == article.id }
            } else {
                current + article
            }
        }
    }

    // ---- Lookup ----
    fun article(id: String): Article? = articleCache[id]
}

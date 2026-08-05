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
    val error: String? = null,
    val usingSampleData: Boolean = !NewsRepository.hasApiKey
)

data class SearchUiState(
    val query: String = "",
    val activeFilter: String = "All",
    val results: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class NewsViewModel : ViewModel() {

    // Every article the app has seen this session, keyed by id — lets the
    // article reader look up a story without a dedicated "get by id" call.
    private val articleCache = HashMap<String, Article>()

    // ---- Feed ----
    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

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
        _homeState.update { it.copy(selectedEdition = edition, isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = NewsRepository.fetchArticles(category = categoryQuery)) {
                is RepoResult.Success -> {
                    result.data.forEach { articleCache[it.id] = it }
                    val hero = result.data.firstOrNull { it.isHero } ?: result.data.firstOrNull()
                    val rest = result.data.filterNot { it.id == hero?.id }
                    _homeState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            hero = hero,
                            justInHeadline = rest.firstOrNull()?.title ?: hero?.title,
                            stories = rest,
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

    // ---- Search ----
    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()
    val searchFilters: List<String> = listOf("All", "Articles", "Opinion")

    private var searchJob: Job? = null

    fun updateQuery(query: String) {
        _searchState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchState.update { it.copy(results = emptyList(), isLoading = false, error = null) }
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
        _searchState.update { it.copy(isLoading = true, error = null) }
        when (val result = NewsRepository.fetchArticles(query = query)) {
            is RepoResult.Success -> {
                result.data.forEach { articleCache[it.id] = it }
                val filtered = when (_searchState.value.activeFilter) {
                    "Opinion" -> result.data.filter { it.category.equals("Opinion", ignoreCase = true) }
                    "Articles" -> result.data.filterNot { it.category.equals("Opinion", ignoreCase = true) }
                    else -> result.data
                }
                _searchState.update { it.copy(isLoading = false, results = filtered, error = null) }
            }
            is RepoResult.Error -> {
                _searchState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
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

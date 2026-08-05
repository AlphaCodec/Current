package com.current.news.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.current.news.data.Article
import com.current.news.data.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val selectedEdition: String = "For you",
    val editions: List<String> = NewsRepository.editions,
    val liveHeadline: String = NewsRepository.liveHeadline,
    val hero: Article? = NewsRepository.articles.firstOrNull { it.isHero },
    val stories: List<Article> = NewsRepository.articles.filterNot { it.isHero }
)

data class SearchUiState(
    val query: String = "",
    val activeFilter: String = "All",
    val results: List<Article> = emptyList()
)

class NewsViewModel : ViewModel() {

    // ---- Feed ----
    private val _selectedEdition = MutableStateFlow("For you")
    val homeState: StateFlow<HomeUiState> = _selectedEdition
        .combine(MutableStateFlow(NewsRepository.articles)) { edition, all ->
            val filtered = if (edition == "For you") all else all.filter {
                it.category.equals(edition, ignoreCase = true) ||
                    edition.equals("World", true) && it.category in listOf("World", "Politics")
            }
            HomeUiState(
                selectedEdition = edition,
                hero = all.firstOrNull { it.isHero },
                stories = (if (filtered.isEmpty()) all else filtered).filterNot { it.isHero }
            )
        }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, HomeUiState())

    fun selectEdition(edition: String) {
        _selectedEdition.value = edition
    }

    // ---- Search ----
    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState

    private val filters = listOf("All", "Articles", "Live", "Opinion")
    val searchFilters: List<String> = filters

    fun updateQuery(query: String) {
        val results = NewsRepository.search(query)
        _searchState.value = _searchState.value.copy(query = query, results = applyFilter(results, _searchState.value.activeFilter))
    }

    fun setFilter(filter: String) {
        val base = NewsRepository.search(_searchState.value.query)
        _searchState.value = _searchState.value.copy(activeFilter = filter, results = applyFilter(base, filter))
    }

    private fun applyFilter(list: List<Article>, filter: String): List<Article> = when (filter) {
        "Articles" -> list.filter { it.category != "Opinion" }
        "Live" -> list.filter { it.isLive }
        "Opinion" -> list.filter { it.category == "Opinion" }
        else -> list
    }

    // ---- Bookmarks ----
    private val _savedIds = MutableStateFlow<Set<String>>(emptySet())
    val savedArticles: StateFlow<List<Article>> = _savedIds
        .combine(MutableStateFlow(NewsRepository.articles)) { ids, all -> all.filter { it.id in ids } }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, emptyList())

    fun isSaved(id: String): Boolean = id in _savedIds.value

    fun toggleSave(id: String) {
        viewModelScope.launch {
            val current = _savedIds.value
            _savedIds.value = if (id in current) current - id else current + id
        }
    }

    fun article(id: String): Article? = NewsRepository.byId(id)
}

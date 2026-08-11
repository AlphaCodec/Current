package com.current.news.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.bookmarksDataStore by preferencesDataStore(name = "current_bookmarks")

/**
 * Persists bookmarked articles to disk (DataStore, as JSON) so they survive
 * app restarts and process death — previously these lived only in an
 * in-memory StateFlow and were lost the moment the app closed.
 */
class BookmarksRepository(private val context: Context) {

    private val savedKey = stringPreferencesKey("saved_articles_json")
    private val gson = Gson()
    private val listType = object : TypeToken<List<SavedArticleEntity>>() {}.type

    val savedArticles: Flow<List<Article>> = context.bookmarksDataStore.data.map { prefs ->
        val json = prefs[savedKey] ?: return@map emptyList()
        try {
            val entities: List<SavedArticleEntity> = gson.fromJson(json, listType) ?: emptyList()
            entities.map { it.toArticle() }
        } catch (e: Exception) {
            // Corrupt/old-shape JSON shouldn't crash the app — just start fresh.
            emptyList()
        }
    }

    suspend fun setSavedArticles(articles: List<Article>) {
        val entities = articles.map { it.toEntity() }
        val json = gson.toJson(entities)
        context.bookmarksDataStore.edit { prefs ->
            prefs[savedKey] = json
        }
    }
}

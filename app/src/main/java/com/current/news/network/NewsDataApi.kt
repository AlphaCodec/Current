package com.current.news.network

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsDataApi {

    /**
     * Fetches the latest articles. Either [category] or [query] (or both)
     * narrows the result set; passing neither returns a general "top" mix.
     *
     * NewsData.io free tier: 200 requests/day, ~10 articles per request,
     * no full article body (description only), commercial use permitted.
     */
    @GET("api/1/latest")
    suspend fun getLatest(
        @Query("apikey") apiKey: String,
        @Query("category") category: String? = null,
        @Query("q") query: String? = null,
        @Query("language") language: String = "en",
        @Query("image") image: String = "1", // only return articles that have an image
        @Query("page") page: String? = null // pagination token from a previous response's nextPage
    ): NewsDataResponse

    companion object {
        const val BASE_URL = "https://newsdata.io/"
    }
}

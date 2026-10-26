package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

sealed class SearchResult {
    data class Success(val matches: List<DiscoveredMovie>) : SearchResult()
    data class Empty(val query: String, val webSearchUrl: String) : SearchResult()
    data class Error(val message: String, val webSearchUrl: String) : SearchResult()
}

class MovieSearchService(
    private val providers: List<MovieSearchProvider> = listOf(
        WikipediaSearchProvider(),
        TvMazeSearchProvider()
    )
) {
    // In-memory query cache to avoid repetitive network requests
    private val cache = ConcurrentHashMap<String, List<DiscoveredMovie>>()

    /**
     * Search public web sources for matching movie titles and metadata.
     * Includes caching, duplicate removal, and 12-second timeout.
     */
    suspend fun searchMovies(query: String): SearchResult = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) {
            return@withContext SearchResult.Empty(query, getBrowserSearchUrl(query))
        }

        // Check cache first
        val cached = cache[cleanQuery.lowercase()]
        if (cached != null && cached.isNotEmpty()) {
            return@withContext SearchResult.Success(cached)
        }

        val allResults = mutableListOf<DiscoveredMovie>()
        val seenTitles = mutableSetOf<String>()

        try {
            val completedInTime = withTimeoutOrNull(12_000L) {
                for (provider in providers) {
                    try {
                        val results = provider.search(cleanQuery)
                        for (movie in results) {
                            val key = "${movie.title.lowercase()}_${movie.releaseYear ?: 0}"
                            if (!seenTitles.contains(key)) {
                                seenTitles.add(key)
                                allResults.add(movie)
                            }
                        }
                        // If Wikipedia found high-quality matches with posters, we have plenty of candidates
                        if (allResults.size >= 4) break
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if (completedInTime == null && allResults.isEmpty()) {
                return@withContext SearchResult.Error(
                    "Search request timed out. You can open public search directly in your browser.",
                    getBrowserSearchUrl(cleanQuery)
                )
            }

            if (allResults.isEmpty()) {
                return@withContext SearchResult.Empty(
                    cleanQuery,
                    getBrowserSearchUrl(cleanQuery)
                )
            }

            // Rank by title similarity
            val sorted = allResults.sortedWith(
                compareByDescending<DiscoveredMovie> { it.title.equals(cleanQuery, ignoreCase = true) }
                    .thenByDescending { it.posterUrl != null }
                    .thenByDescending { it.releaseYear != null }
            )

            cache[cleanQuery.lowercase()] = sorted
            SearchResult.Success(sorted)
        } catch (e: Exception) {
            e.printStackTrace()
            SearchResult.Error(
                "Unable to retrieve public movie details: ${e.localizedMessage ?: "Network error"}",
                getBrowserSearchUrl(cleanQuery)
            )
        }
    }

    /**
     * Generates a direct web search URL for manual user browsing if automated access fails.
     */
    fun getBrowserSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return "https://en.wikipedia.org/w/index.php?search=$encoded+film"
    }

    fun getGoogleSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return "https://www.google.com/search?q=$encoded+movie"
    }

    /**
     * Clear search cache if needed.
     */
    fun clearCache() {
        cache.clear()
    }
}

package com.example.service

import com.example.data.EntertainmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

sealed class SearchResult {
    data class Success(val matches: List<DiscoveredMovie>) : SearchResult()
    data class Empty(val query: String, val webSearchUrl: String) : SearchResult()
    data class Error(val message: String, val webSearchUrl: String) : SearchResult()
}

data class GlobalSearchResponse(
    val items: List<DiscoveredMovie>,
    val totalEstimatedCount: Int,
    val currentPage: Int,
    val hasMorePages: Boolean,
    val isYearShowcase: Boolean,
    val yearShowcaseNumber: Int?,
    val didYouMeanSuggestion: String?,
    val webSearchUrl: String
)

class MovieSearchService(
    private val wikipediaProvider: WikipediaSearchProvider = WikipediaSearchProvider(),
    private val tvMazeProvider: TvMazeSearchProvider = TvMazeSearchProvider(),
    private val animeProvider: AnimeSearchProvider = AnimeSearchProvider()
) {
    // In-memory query cache with composite key
    private val cache = ConcurrentHashMap<String, List<DiscoveredMovie>>()
    private val suggestionCache = ConcurrentHashMap<String, String>()

    /**
     * Primary quick search method (backwards-compatible with initial CineVault workflow).
     */
    suspend fun searchMovies(query: String): SearchResult = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) {
            return@withContext SearchResult.Empty(query, getBrowserSearchUrl(query))
        }

        val global = searchGlobalEntertainment(cleanQuery, filterType = null, page = 1, pageSize = 8)
        if (global.items.isNotEmpty()) {
            SearchResult.Success(global.items)
        } else {
            SearchResult.Empty(cleanQuery, global.webSearchUrl)
        }
    }

    /**
     * Real-time global entertainment search engine supporting:
     * - Fuzzy titles, typos, and partial names
     * - Alternate titles & keywords
     * - Actors, Directors, Genres
     * - Release Year showcase with pagination
     * - Content types: Movies, TV Series, Anime, Dramas
     */
    suspend fun searchGlobalEntertainment(
        query: String,
        filterType: String? = null,
        page: Int = 1,
        pageSize: Int = 12
    ): GlobalSearchResponse = withContext(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.isBlank()) {
            return@withContext GlobalSearchResponse(
                items = emptyList(),
                totalEstimatedCount = 0,
                currentPage = 1,
                hasMorePages = false,
                isYearShowcase = false,
                yearShowcaseNumber = null,
                didYouMeanSuggestion = null,
                webSearchUrl = getBrowserSearchUrl("")
            )
        }

        // Check if query is a 4-digit release year (e.g. 1999, 2024, 2010)
        val isYearQuery = clean.matches(Regex("^(19\\d{2}|20[0-2]\\d|2030)$"))
        val targetYear = if (isYearQuery) clean.toInt() else null

        val cacheKey = "global_${clean.lowercase()}_${filterType ?: "all"}_page_$page"
        val cached = cache[cacheKey]
        val cachedSuggestion = suggestionCache[clean.lowercase()]
        if (cached != null) {
            return@withContext GlobalSearchResponse(
                items = cached,
                totalEstimatedCount = if (isYearQuery) 50 else cached.size,
                currentPage = page,
                hasMorePages = if (isYearQuery) cached.size >= pageSize else false,
                isYearShowcase = isYearQuery,
                yearShowcaseNumber = targetYear,
                didYouMeanSuggestion = cachedSuggestion,
                webSearchUrl = getBrowserSearchUrl(clean)
            )
        }

        val allResults = mutableListOf<DiscoveredMovie>()
        val seenKeys = mutableSetOf<String>()
        var didYouMean: String? = cachedSuggestion

        try {
            // First check Curated Entertainment Registry for instant, verified matching (Marvel, Hollywood, Classics)
            val curatedMatches = if (!isYearQuery) CuratedEntertainmentRegistry.findMatching(clean) else emptyList()
            for (item in curatedMatches) {
                val key = "${item.title.lowercase()}_${item.releaseYear ?: 0}_${item.contentType}"
                if (!seenKeys.contains(key)) {
                    seenKeys.add(key)
                    val (score, reason) = FuzzyRelevanceRanker.scoreAndExplain(item, clean)
                    allResults.add(item.copy(relevanceScore = maxOf(score, 90f), matchReason = reason))
                }
            }

            // If we have strong curated matches, use a fast 2.5s network budget; otherwise 6.5s max to allow deep worldwide results
            val networkTimeoutMs = if (allResults.size >= 4) 2500L else 6500L

            withTimeoutOrNull(networkTimeoutMs) {
                if (isYearQuery && targetYear != null) {
                    val wikiYearTask = async { wikipediaProvider.searchByYear(targetYear, page, pageSize = 8) }
                    val animeYearTask = async {
                        if (filterType == null || filterType == EntertainmentType.ANIME) {
                            animeProvider.searchByYear(targetYear, page = page)
                        } else emptyList()
                    }

                    val wikiResults = wikiYearTask.await()
                    val animeResults = animeYearTask.await()

                    for (m in wikiResults + animeResults) {
                        val key = "${m.title.lowercase()}_${m.releaseYear ?: 0}"
                        if (!seenKeys.contains(key)) {
                            seenKeys.add(key)
                            allResults.add(m.copy(matchReason = "Released in $targetYear"))
                        }
                    }
                } else {
                    // Query external providers concurrently with smart type filtering
                    val wikiTask = async { wikipediaProvider.search(clean) }
                    val tvMazeTask = async {
                        if (filterType == null || filterType == EntertainmentType.TV_SERIES || filterType == EntertainmentType.DRAMA) {
                            tvMazeProvider.search(clean)
                        } else emptyList()
                    }
                    val animeTask = async {
                        val isAnimeQuery = clean.contains("anime", ignoreCase = true) || filterType == EntertainmentType.ANIME
                        if (isAnimeQuery) animeProvider.search(clean) else emptyList()
                    }
                    val suggestionTask = async {
                        if (didYouMean == null && clean.length >= 3 && allResults.isEmpty()) {
                            wikipediaProvider.getFuzzySuggestion(clean)
                        } else null
                    }

                    val wikiResults = try { wikiTask.await() } catch (e: Exception) { emptyList() }
                    val tvResults = try { tvMazeTask.await() } catch (e: Exception) { emptyList() }
                    val animeResults = try { animeTask.await() } catch (e: Exception) { emptyList() }
                    val fetchedSuggestion = try { suggestionTask.await() } catch (e: Exception) { null }

                    if (fetchedSuggestion != null && !fetchedSuggestion.equals(clean, ignoreCase = true)) {
                        didYouMean = fetchedSuggestion
                        suggestionCache[clean.lowercase()] = fetchedSuggestion
                    }

                    for (item in wikiResults + tvResults + animeResults) {
                        val key = "${item.title.lowercase()}_${item.releaseYear ?: 0}_${item.contentType}"
                        if (!seenKeys.contains(key)) {
                            seenKeys.add(key)
                            val (score, reason) = FuzzyRelevanceRanker.scoreAndExplain(item, clean)
                            allResults.add(item.copy(relevanceScore = score, matchReason = reason))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Resilient fallback: ensure curated results are preserved even on timeout or error
            if (allResults.isEmpty()) {
                val fallbackMatches = CuratedEntertainmentRegistry.findMatching(clean)
                for (item in fallbackMatches) {
                    val (score, reason) = FuzzyRelevanceRanker.scoreAndExplain(item, clean)
                    allResults.add(item.copy(relevanceScore = score, matchReason = reason))
                }
            }
        }

        // Apply Entertainment Type Filter if selected
        var filtered: List<DiscoveredMovie> = allResults
        if (!filterType.isNullOrBlank() && filterType != "ALL") {
            filtered = filtered.filter { it.contentType.equals(filterType, ignoreCase = true) }
        }

        // Sort by relevance score descending, then by poster availability
        val sorted = filtered.sortedWith(
            compareByDescending<DiscoveredMovie> { it.relevanceScore }
                .thenByDescending { it.posterUrl != null }
                .thenByDescending { it.releaseYear ?: 0 }
        )

        // Store in cache
        cache[cacheKey] = sorted

        val hasMore = if (isYearQuery) sorted.size >= pageSize else false

        GlobalSearchResponse(
            items = sorted,
            totalEstimatedCount = if (isYearQuery) 60 else sorted.size,
            currentPage = page,
            hasMorePages = hasMore,
            isYearShowcase = isYearQuery,
            yearShowcaseNumber = targetYear,
            didYouMeanSuggestion = didYouMean,
            webSearchUrl = getBrowserSearchUrl(clean)
        )
    }

    fun getBrowserSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return "https://en.wikipedia.org/w/index.php?search=$encoded+film"
    }

    fun getGoogleSearchUrl(query: String): String {
        val encoded = URLEncoder.encode(query, "UTF-8")
        return "https://www.google.com/search?q=$encoded+movie"
    }

    fun clearCache() {
        cache.clear()
        suggestionCache.clear()
    }
}

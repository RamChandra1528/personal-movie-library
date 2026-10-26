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
    private val animeProvider: AnimeSearchProvider = AnimeSearchProvider(),
    private val imdbProvider: ImdbSearchProvider = ImdbSearchProvider(),
    private val collectionProvider: CollectionSearchProvider = CollectionSearchProvider()
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

        val cacheKey = "global_v2_${clean.lowercase()}_${filterType ?: "all"}_page_${page}_size_$pageSize"
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
            withTimeoutOrNull(14_000L) {
                if (isYearQuery && targetYear != null) {
                    // YEAR SHOWCASE MODE: Fetch all entertainment from that year with pagination
                    val wikiYearTask = async { wikipediaProvider.searchByYear(targetYear, page, pageSize = 8) }
                    val animeYearTask = async { animeProvider.searchByYear(targetYear, page = page) }

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
                    // GLOBAL KEYWORD / TITLE / ACTOR / GENRE SEARCH
                    val wikiTask = async { wikipediaProvider.search(clean) }
                    val tvMazeTask = async { tvMazeProvider.search(clean) }
                    val animeTask = async { animeProvider.search(clean) }
                    val imdbTask = async { imdbProvider.search(clean) }
                    val collectionTask = async { collectionProvider.search(clean) }
                    val suggestionTask = async {
                        if (didYouMean == null && clean.length >= 3) {
                            wikipediaProvider.getFuzzySuggestion(clean)
                        } else null
                    }

                    val wikiResults = wikiTask.await()
                    val tvResults = tvMazeTask.await()
                    val animeResults = animeTask.await()
                    val imdbResults = imdbTask.await()
                    val collectionResults = collectionTask.await()
                    val fetchedSuggestion = suggestionTask.await()

                    if (fetchedSuggestion != null && !fetchedSuggestion.equals(clean, ignoreCase = true)) {
                        didYouMean = fetchedSuggestion
                        suggestionCache[clean.lowercase()] = fetchedSuggestion
                    }

                    // Put the title-first IMDb matches and collection browse results first.
                    // They make broad Marvel/Hollywood searches useful and preserve direct
                    // feature-film matches that Wikipedia's article search may omit.
                    for (item in imdbResults + collectionResults + wikiResults + tvResults + animeResults) {
                        val key = "${item.title.lowercase()}_${item.releaseYear ?: 0}_${item.contentType}"
                        if (!seenKeys.contains(key)) {
                            seenKeys.add(key)
                            // Broad collection searches are intentional browse requests, not
                            // loose title matches. Keep that useful collection at the top.
                            val (score, reason) = if (item.sourceName == collectionProvider.providerName) {
                                88f to "Featured collection"
                            } else {
                                FuzzyRelevanceRanker.scoreAndExplain(item, clean)
                            }
                            allResults.add(item.copy(relevanceScore = score, matchReason = reason))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

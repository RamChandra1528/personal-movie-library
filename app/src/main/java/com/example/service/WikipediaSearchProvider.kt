package com.example.service

import com.example.data.EntertainmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class WikipediaSearchProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) : MovieSearchProvider {

    override val providerName: String = "Wikipedia & Wikimedia"

    private val userAgent = "CineVault-Android/1.0 (https://cinevault.app; contact@cinevault.app)"

    private val commonGenres = listOf(
        "Sci-Fi", "Science Fiction", "Drama", "Action", "Adventure",
        "Thriller", "Comedy", "Romance", "Horror", "Mystery", "Crime",
        "Fantasy", "Animation", "Documentary", "War", "Western",
        "Biography", "Musical", "Family", "History", "Superhero", "Cyberpunk"
    )

    override suspend fun search(query: String): List<DiscoveredMovie> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return@withContext emptyList()

        try {
            // First search Wikipedia for pages matching "<query> film" or "<query>"
            val encodedSearch = URLEncoder.encode("$cleanQuery film", "UTF-8")
            val searchUrl = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$encodedSearch&srlimit=6&format=json"

            val request = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", userAgent)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val responseBody = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(responseBody)
            val queryObj = json.optJSONObject("query") ?: return@withContext emptyList()
            val searchArr = queryObj.optJSONArray("search") ?: return@withContext emptyList()

            val titles = mutableListOf<String>()
            for (i in 0 until searchArr.length()) {
                val item = searchArr.getJSONObject(i)
                val title = item.optString("title")
                if (title.isNotEmpty()) {
                    titles.add(title)
                }
            }

            // Also check OpenSearch for fuzzy corrections if titles is small
            if (titles.size < 3) {
                val fuzzyTitles = fetchOpenSearchSuggestions(cleanQuery)
                for (ft in fuzzyTitles) {
                    if (!titles.contains(ft)) titles.add(ft)
                }
            }

            // Fetch summary for top results in parallel
            val deferred = titles.take(5).map { title ->
                async {
                    fetchSummaryForTitle(title)
                }
            }

            return@withContext deferred.awaitAll().filterNotNull()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Search entertainment released in a specific year with pagination.
     */
    suspend fun searchByYear(year: Int, page: Int = 1, pageSize: Int = 10): List<DiscoveredMovie> = withContext(Dispatchers.IO) {
        try {
            val offset = (page - 1) * pageSize
            val queryStr = URLEncoder.encode("incategory:${year}_films OR incategory:${year}_television_series_debuts", "UTF-8")
            val searchUrl = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$queryStr&srlimit=$pageSize&sroffset=$offset&format=json"

            val request = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", userAgent)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val body = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            val searchArr = json.optJSONObject("query")?.optJSONArray("search") ?: return@withContext emptyList()

            val titles = mutableListOf<String>()
            for (i in 0 until searchArr.length()) {
                val title = searchArr.getJSONObject(i).optString("title")
                if (title.isNotBlank()) titles.add(title)
            }

            val deferred = titles.map { title ->
                async {
                    fetchSummaryForTitle(title, forceYear = year)
                }
            }
            deferred.awaitAll().filterNotNull()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * OpenSearch suggestion for spelling mistakes and fuzzy typing.
     */
    suspend fun getFuzzySuggestion(query: String): String? = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query.trim(), "UTF-8")
            val url = "https://en.wikipedia.org/w/api.php?action=opensearch&search=$encoded&limit=4&format=json"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val array = JSONArray(body)
            if (array.length() >= 2) {
                val suggestions = array.getJSONArray(1)
                for (i in 0 until suggestions.length()) {
                    val s = suggestions.getString(i)
                    if (s.contains("film", ignoreCase = true) || s.contains("series", ignoreCase = true)) {
                        return@withContext cleanMovieTitle(s)
                    }
                }
                if (suggestions.length() > 0) {
                    val first = suggestions.getString(0)
                    if (!first.equals(query, ignoreCase = true)) {
                        return@withContext cleanMovieTitle(first)
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchOpenSearchSuggestions(query: String): List<String> {
        return try {
            val encoded = URLEncoder.encode(query.trim(), "UTF-8")
            val url = "https://en.wikipedia.org/w/api.php?action=opensearch&search=$encoded&limit=5&format=json"
            val request = Request.Builder().url(url).header("User-Agent", userAgent).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val body = response.body?.string() ?: return emptyList()
            val array = JSONArray(body)
            val list = mutableListOf<String>()
            if (array.length() >= 2) {
                val suggestions = array.getJSONArray(1)
                for (i in 0 until suggestions.length()) {
                    list.add(suggestions.getString(i))
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getMovieDetails(identifier: String): DiscoveredMovie? = withContext(Dispatchers.IO) {
        fetchSummaryForTitle(identifier)
    }

    private fun fetchSummaryForTitle(wikiTitle: String, forceYear: Int? = null): DiscoveredMovie? {
        return try {
            val encodedTitle = URLEncoder.encode(wikiTitle.replace(" ", "_"), "UTF-8")
            val url = "https://en.wikipedia.org/api/rest_v1/page/summary/$encodedTitle"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null

            val body = response.body?.string() ?: return null
            val json = JSONObject(body)

            val rawTitle = json.optString("title", wikiTitle)
            val cleanTitle = cleanMovieTitle(rawTitle)
            val extract = json.optString("extract", "")
            val description = json.optString("description", "")

            // Poster: Prefer originalimage, fallback to thumbnail
            val posterUrl = json.optJSONObject("originalimage")?.optString("source")
                ?: json.optJSONObject("thumbnail")?.optString("source")

            val sourceUrl = json.optJSONObject("content_urls")
                ?.optJSONObject("desktop")
                ?.optString("page", "https://en.wikipedia.org/wiki/$encodedTitle")

            // Determine content type
            val lowerDesc = "$description $rawTitle $extract".lowercase()
            val contentType = when {
                lowerDesc.contains("anime") || lowerDesc.contains("manga") -> EntertainmentType.ANIME
                lowerDesc.contains("korean drama") || lowerDesc.contains("television drama") || lowerDesc.contains("drama series") -> EntertainmentType.DRAMA
                lowerDesc.contains("television series") || lowerDesc.contains("tv series") || lowerDesc.contains("web series") || lowerDesc.contains("miniseries") -> EntertainmentType.TV_SERIES
                else -> EntertainmentType.MOVIE
            }

            // Extract Year from description, rawTitle or extract
            val year = forceYear
                ?: extractYear(rawTitle)
                ?: extractYear(description)
                ?: extractYear(extract)

            // Extract Director
            val director = extractDirector(description)
                ?: extractDirector(extract)
                ?: if (contentType == EntertainmentType.TV_SERIES) "Showrunners" else "Production Team"

            // Extract Cast
            val castList = extractCast(extract)

            // Extract Genres
            val foundGenres = extractGenres("$description $extract")

            // Extract Runtime if mentioned in text
            val runtime = extractRuntime(extract)

            // Alternate titles
            val altTitles = mutableListOf<String>()
            val norm = json.optJSONObject("titles")?.optString("normalized")
            if (norm != null && norm != cleanTitle && !altTitles.contains(norm)) altTitles.add(norm)

            // Generate similar titles based on director or genres
            val similar = generateSimilarTitles(cleanTitle, foundGenres, director)

            DiscoveredMovie(
                title = cleanTitle,
                releaseYear = year,
                releaseDate = year?.let { "$it" },
                posterUrl = posterUrl,
                backdropUrl = posterUrl,
                genres = if (foundGenres.isNotEmpty()) foundGenres else listOf("Drama"),
                synopsis = extract.ifBlank { description },
                director = director,
                cast = castList,
                runtimeMinutes = runtime,
                language = "English",
                externalRating = if (posterUrl != null) "Verified Public Source" else null,
                sourceUrl = sourceUrl,
                sourceName = "Wikipedia / Wikimedia Commons",
                contentType = contentType,
                alternateTitles = altTitles,
                similarTitles = similar
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun cleanMovieTitle(title: String): String {
        return title
            .replace(Regex("\\s*\\((?:\\d{4}\\s+)?film\\)", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*\\(.*film.*\\)", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*\\(.*television.*\\)", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*\\(.*TV series.*\\)", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun extractYear(text: String): Int? {
        val pattern = Pattern.compile("\\b(19\\d{2}|20[0-2]\\d|2030)\\b")
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)?.toIntOrNull()
        }
        return null
    }

    private fun extractDirector(text: String): String? {
        val patterns = listOf(
            Pattern.compile("directed by\\s+([A-Z][a-zA-Z'\\-\\.\\s]+?)(?:,|\\.|;|\\band\\b|\\bwho\\b|\\bin\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("created by\\s+([A-Z][a-zA-Z'\\-\\.\\s]+?)(?:,|\\.|;|\\band\\b)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("director[:\\s]+([A-Z][a-zA-Z'\\-\\.\\s]+?)(?:,|\\.|\\n)", Pattern.CASE_INSENSITIVE)
        )
        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val name = matcher.group(1)?.trim()
                if (name != null && name.length in 3..40 && !name.contains("film", ignoreCase = true)) {
                    return name
                }
            }
        }
        return null
    }

    private fun extractCast(text: String): List<String> {
        val castPattern = Pattern.compile("(?:ensemble cast led by|starring|features|stars)\\s+([A-Z][a-zA-Z'\\-\\.\\s,\\band\\b]+?)(?:\\.|\\;|\n)", Pattern.CASE_INSENSITIVE)
        val matcher = castPattern.matcher(text)
        if (matcher.find()) {
            val castChunk = matcher.group(1) ?: return emptyList()
            return castChunk
                .split(Regex("[,;]|\\band\\b"))
                .map { it.trim() }
                .filter { it.length in 3..35 && !it.contains("film", ignoreCase = true) }
                .take(6)
        }
        return emptyList()
    }

    private fun extractGenres(text: String): List<String> {
        val matched = mutableListOf<String>()
        val lowerText = text.lowercase()
        for (genre in commonGenres) {
            if (lowerText.contains(genre.lowercase())) {
                val clean = if (genre.equals("Science Fiction", ignoreCase = true)) "Sci-Fi" else genre
                if (!matched.contains(clean)) {
                    matched.add(clean)
                }
            }
        }
        return matched.take(4)
    }

    private fun extractRuntime(text: String): Int? {
        val pattern = Pattern.compile("(\\d{2,3})\\s*(?:minutes|mins|min)\\b", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        if (matcher.find()) {
            return matcher.group(1)?.toIntOrNull()
        }
        return null
    }

    private fun generateSimilarTitles(title: String, genres: List<String>, director: String): List<String> {
        val list = mutableListOf<String>()
        if (title.contains("Interstellar", ignoreCase = true)) {
            list.addAll(listOf("Inception", "Arrival", "Gravity", "Contact"))
        } else if (title.contains("Matrix", ignoreCase = true)) {
            list.addAll(listOf("Blade Runner 2049", "Dark City", "Equilibrium"))
        } else if (title.contains("Dark Knight", ignoreCase = true)) {
            list.addAll(listOf("Batman Begins", "The Prestige", "Memento"))
        } else if (genres.contains("Sci-Fi")) {
            list.addAll(listOf("Interstellar", "Inception", "Blade Runner"))
        } else if (genres.contains("Action")) {
            list.addAll(listOf("Mad Max: Fury Road", "John Wick", "Gladiator"))
        } else if (genres.contains("Animation") || genres.contains("Anime")) {
            list.addAll(listOf("Spirited Away", "Your Name", "Princess Mononoke"))
        }
        return list.take(4)
    }
}

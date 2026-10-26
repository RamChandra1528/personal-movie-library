package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
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
        "Biography", "Musical", "Family", "History", "Superhero"
    )

    override suspend fun search(query: String): List<DiscoveredMovie> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return@withContext emptyList()

        try {
            // First search Wikipedia for pages matching "<query> film" or "<query>"
            val encodedSearch = URLEncoder.encode("$cleanQuery film", "UTF-8")
            val searchUrl = "https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=$encodedSearch&srlimit=5&format=json"

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

            // Fetch summary for top results in parallel
            val deferred = titles.take(4).map { title ->
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

    override suspend fun getMovieDetails(identifier: String): DiscoveredMovie? = withContext(Dispatchers.IO) {
        fetchSummaryForTitle(identifier)
    }

    private fun fetchSummaryForTitle(wikiTitle: String): DiscoveredMovie? {
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

            // Extract Year from description, rawTitle or extract
            val year = extractYear(rawTitle)
                ?: extractYear(description)
                ?: extractYear(extract)

            // Extract Director
            val director = extractDirector(description)
                ?: extractDirector(extract)
                ?: "Unknown Director"

            // Extract Cast
            val castList = extractCast(extract)

            // Extract Genres
            val foundGenres = extractGenres("$description $extract")

            // Extract Runtime if mentioned in text
            val runtime = extractRuntime(extract)

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
                sourceName = "Wikipedia / Wikimedia Commons"
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
}

package com.example.service

import com.example.data.EntertainmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Looks up title suggestions from IMDb's public suggestion index.  Unlike the
 * encyclopaedia lookup, this index is title-first, so feature films remain
 * discoverable even when their Wikipedia page has an ambiguous name.
 */
class ImdbSearchProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()
) : MovieSearchProvider {

    override val providerName: String = "IMDb title index"

    override suspend fun search(query: String): List<DiscoveredMovie> = withContext(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.isBlank()) return@withContext emptyList()

        try {
            val encoded = URLEncoder.encode(clean, "UTF-8")
            val url = "https://v3.sg.media-imdb.com/suggestion/x/$encoded.json"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "CineVault-Android/1.0 (contact@cinevault.app)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val body = response.body?.string() ?: return@withContext emptyList()
            parseResponse(body)
        } catch (_: Exception) {
            // Other providers remain available if IMDb is temporarily unavailable.
            emptyList()
        }
    }

    override suspend fun getMovieDetails(identifier: String): DiscoveredMovie? =
        search(identifier).let { results ->
            results.firstOrNull { it.title.equals(identifier, ignoreCase = true) } ?: results.firstOrNull()
        }

    internal fun parseResponse(jsonString: String): List<DiscoveredMovie> {
        val entries = JSONObject(jsonString).optJSONArray("d") ?: return emptyList()
        val results = mutableListOf<DiscoveredMovie>()

        for (index in 0 until entries.length()) {
            val item = entries.optJSONObject(index) ?: continue
            val id = item.optString("id")
            val title = item.optString("l").trim()
            if (!id.startsWith("tt") || title.isBlank()) continue

            val kind = item.optString("q")
            val kindId = item.optString("qid")
            val contentType = when {
                kindId.equals("movie", ignoreCase = true) || kind.equals("feature", ignoreCase = true) -> EntertainmentType.MOVIE
                kind.contains("tv", ignoreCase = true) -> EntertainmentType.TV_SERIES
                else -> EntertainmentType.MOVIE
            }
            val image = item.optJSONObject("i")?.optString("imageUrl")?.takeIf { it.isNotBlank() }
            val people = item.optString("s")
                .substringBefore("(")
                .split(",")
                .map(String::trim)
                .filter(String::isNotBlank)
                .take(4)

            results += DiscoveredMovie(
                title = title,
                releaseYear = item.optInt("y", 0).takeIf { it > 0 },
                posterUrl = image,
                backdropUrl = image,
                genres = listOf(if (contentType == EntertainmentType.MOVIE) "Movie" else "TV Series"),
                synopsis = item.optString("s"),
                cast = people,
                language = "English",
                sourceUrl = "https://www.imdb.com/title/$id/",
                sourceName = providerName,
                contentType = contentType,
                imdbId = id
            )
        }
        return results
    }
}

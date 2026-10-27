package com.example.service

import com.example.data.EntertainmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class TvMazeSearchProvider(
    private val client: OkHttpClient = NetworkClient.client
) : MovieSearchProvider {

    override val providerName: String = "TVMaze Global TV & Series Registry"

    override suspend fun search(query: String): List<DiscoveredMovie> = withContext(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.isEmpty()) return@withContext emptyList()

        try {
            val encoded = URLEncoder.encode(clean, "UTF-8")
            val url = "https://api.tvmaze.com/search/shows?q=$encoded"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "CineVault-Android/1.0 (contact@cinevault.app)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val body = response.body?.string() ?: return@withContext emptyList()
            val array = JSONArray(body)
            val results = mutableListOf<DiscoveredMovie>()

            for (i in 0 until array.length().coerceAtMost(6)) {
                val item = array.getJSONObject(i)
                val show = item.optJSONObject("show") ?: continue

                val name = show.optString("name")
                val premiered = show.optString("premiered")
                val year = if (premiered.length >= 4) premiered.substring(0, 4).toIntOrNull() else null

                val genresArray = show.optJSONArray("genres")
                val genresList = mutableListOf<String>()
                if (genresArray != null) {
                    for (g in 0 until genresArray.length()) {
                        genresList.add(genresArray.getString(g))
                    }
                }

                val imageObj = show.optJSONObject("image")
                val posterUrl = imageObj?.optString("original") ?: imageObj?.optString("medium")

                val ratingObj = show.optJSONObject("rating")
                val avgRating = ratingObj?.optDouble("average", Double.NaN)
                val ratingStr = if (avgRating != null && !avgRating.isNaN()) "$avgRating / 10" else null

                val rawSummary = show.optString("summary", "")
                val cleanSummary = rawSummary.replace(Regex("<[^>]*>"), "").trim()

                val runtime = show.optInt("runtime", 0).let { if (it > 0) it else null }
                val showUrl = show.optString("url")
                val externals = show.optJSONObject("externals")
                val imdbId = externals?.optString("imdb")

                val language = show.optString("language", "English")
                val network = show.optJSONObject("network")?.optString("name")
                    ?: show.optJSONObject("webChannel")?.optString("name") ?: "TV Network"

                val isDrama = genresList.contains("Drama") && (language != "English" || network.contains("Drama", ignoreCase = true))
                val isAnime = genresList.contains("Anime") || show.optString("type").equals("Animation", ignoreCase = true)

                val contentType = when {
                    isAnime -> EntertainmentType.ANIME
                    isDrama -> EntertainmentType.DRAMA
                    else -> EntertainmentType.TV_SERIES
                }

                results.add(
                    DiscoveredMovie(
                        title = name,
                        releaseYear = year,
                        releaseDate = premiered.ifBlank { null },
                        posterUrl = posterUrl,
                        backdropUrl = posterUrl,
                        genres = if (genresList.isNotEmpty()) genresList else listOf("TV Series"),
                        synopsis = cleanSummary,
                        director = network,
                        cast = emptyList(),
                        runtimeMinutes = runtime,
                        language = language,
                        externalRating = ratingStr,
                        sourceUrl = showUrl,
                        sourceName = "TVMaze Global TV & Web Series",
                        contentType = contentType,
                        imdbId = if (!imdbId.isNullOrBlank()) imdbId else null,
                        similarTitles = listOf("Stranger Things", "Breaking Bad", "Dark").filter { it != name }
                    )
                )
            }
            results
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getMovieDetails(identifier: String): DiscoveredMovie? {
        val list = search(identifier)
        return list.firstOrNull()
    }
}

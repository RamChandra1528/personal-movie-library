package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class TvMazeSearchProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()
) : MovieSearchProvider {

    override val providerName: String = "TVMaze Public Registry"

    override suspend fun search(query: String): List<DiscoveredMovie> = withContext(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.isEmpty()) return@withContext emptyList()

        try {
            val encoded = URLEncoder.encode(clean, "UTF-8")
            val url = "https://api.tvmaze.com/search/shows?q=$encoded"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "CineVault-Android/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val body = response.body?.string() ?: return@withContext emptyList()
            val array = JSONArray(body)
            val results = mutableListOf<DiscoveredMovie>()

            for (i in 0 until array.length().coerceAtMost(4)) {
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
                val ratingStr = if (avgRating != null && !avgRating.isNaN()) "$avgRating/10" else null

                val rawSummary = show.optString("summary", "")
                val cleanSummary = rawSummary.replace(Regex("<[^>]*>"), "").trim()

                val runtime = show.optInt("runtime", 0).let { if (it > 0) it else null }
                val showUrl = show.optString("url")

                results.add(
                    DiscoveredMovie(
                        title = name,
                        releaseYear = year,
                        releaseDate = premiered.ifBlank { null },
                        posterUrl = posterUrl,
                        backdropUrl = posterUrl,
                        genres = if (genresList.isNotEmpty()) genresList else listOf("Drama"),
                        synopsis = cleanSummary,
                        director = "Production Team",
                        cast = emptyList(),
                        runtimeMinutes = runtime,
                        language = show.optString("language", "English"),
                        externalRating = ratingStr,
                        sourceUrl = showUrl,
                        sourceName = "TVMaze Public Open Registry"
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

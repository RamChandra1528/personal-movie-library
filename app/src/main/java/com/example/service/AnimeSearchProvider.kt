package com.example.service

import com.example.data.EntertainmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class AnimeSearchProvider(
    private val client: OkHttpClient = NetworkClient.client
) : MovieSearchProvider {

    override val providerName: String = "Jikan Global Anime Registry"

    override suspend fun search(query: String): List<DiscoveredMovie> = withContext(Dispatchers.IO) {
        val clean = query.trim()
        if (clean.isBlank()) return@withContext emptyList()

        try {
            val encoded = URLEncoder.encode(clean, "UTF-8")
            val url = "https://api.jikan.moe/v4/anime?q=$encoded&limit=8"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "CineVault-Android/1.0 (contact@cinevault.app)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val body = response.body?.string() ?: return@withContext emptyList()
            parseJikanResponse(body)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchByYear(year: Int, page: Int = 1): List<DiscoveredMovie> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.jikan.moe/v4/anime?start_date=$year-01-01&end_date=$year-12-31&order_by=popularity&sort=desc&limit=15&page=$page"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "CineVault-Android/1.0 (contact@cinevault.app)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val body = response.body?.string() ?: return@withContext emptyList()
            parseJikanResponse(body)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseJikanResponse(jsonString: String): List<DiscoveredMovie> {
        val results = mutableListOf<DiscoveredMovie>()
        val root = JSONObject(jsonString)
        val dataArray = root.optJSONArray("data") ?: return emptyList()

        for (i in 0 until dataArray.length()) {
            val item = dataArray.getJSONObject(i)
            val title = item.optString("title")
            val titleEnglish = item.optString("title_english")
            val titleJapanese = item.optString("title_japanese")

            val altTitles = mutableListOf<String>()
            if (titleEnglish.isNotBlank() && titleEnglish != title) altTitles.add(titleEnglish)
            if (titleJapanese.isNotBlank() && titleJapanese != title) altTitles.add(titleJapanese)

            val type = item.optString("type", "TV")
            val contentType = when (type.uppercase()) {
                "MOVIE" -> EntertainmentType.MOVIE
                else -> EntertainmentType.ANIME
            }

            // Year / Aired
            val year = item.optInt("year", 0).let { if (it > 0) it else null }
                ?: item.optJSONObject("aired")?.optJSONObject("prop")?.optJSONObject("from")?.optInt("year", 0)?.let { if (it > 0) it else null }

            val airedString = item.optJSONObject("aired")?.optString("string")

            // Poster
            val imagesObj = item.optJSONObject("images")?.optJSONObject("jpg")
            val posterUrl = imagesObj?.optString("large_image_url") ?: imagesObj?.optString("image_url")

            // Rating
            val score = item.optDouble("score", Double.NaN)
            val ratingStr = if (!score.isNaN() && score > 0.0) "$score / 10" else null

            // Genres
            val genresList = mutableListOf("Anime")
            val genresArr = item.optJSONArray("genres")
            if (genresArr != null) {
                for (g in 0 until genresArr.length()) {
                    val gName = genresArr.getJSONObject(g).optString("name")
                    if (gName.isNotBlank() && !genresList.contains(gName)) {
                        genresList.add(gName)
                    }
                }
            }

            // Studios as Director/Studio
            val studiosArr = item.optJSONArray("studios")
            val studio = if (studiosArr != null && studiosArr.length() > 0) {
                studiosArr.getJSONObject(0).optString("name")
            } else {
                "Animation Studio"
            }

            // Duration runtime in minutes
            val durationStr = item.optString("duration", "")
            val runtimeMin = extractMinutes(durationStr)

            val synopsis = item.optString("synopsis", "")
            val url = item.optString("url")

            // Suggested similar titles
            val similarList = mutableListOf<String>()
            val relationsArr = item.optJSONArray("relations")
            if (relationsArr != null) {
                for (r in 0 until relationsArr.length().coerceAtMost(3)) {
                    val entryArr = relationsArr.getJSONObject(r).optJSONArray("entry")
                    if (entryArr != null && entryArr.length() > 0) {
                        similarList.add(entryArr.getJSONObject(0).optString("name"))
                    }
                }
            }

            results.add(
                DiscoveredMovie(
                    title = title,
                    releaseYear = year,
                    releaseDate = airedString,
                    posterUrl = posterUrl,
                    backdropUrl = posterUrl,
                    genres = genresList,
                    synopsis = synopsis,
                    director = studio,
                    cast = emptyList(),
                    runtimeMinutes = runtimeMin,
                    language = "Japanese",
                    externalRating = ratingStr,
                    sourceUrl = url,
                    sourceName = "MyAnimeList / Jikan Global Registry",
                    contentType = contentType,
                    alternateTitles = altTitles,
                    similarTitles = similarList
                )
            )
        }
        return results
    }

    private fun extractMinutes(str: String): Int? {
        val regex = Regex("(\\d+)\\s*min", RegexOption.IGNORE_CASE)
        val match = regex.find(str)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    override suspend fun getMovieDetails(identifier: String): DiscoveredMovie? {
        val list = search(identifier)
        return list.firstOrNull()
    }
}

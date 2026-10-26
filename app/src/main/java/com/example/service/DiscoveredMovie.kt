package com.example.service

import com.example.data.EntertainmentType
import com.example.data.MovieEntity
import com.example.data.WatchStatus
import java.net.URLEncoder

/**
 * Global entertainment metadata discovered from public web registries without requiring an API key.
 */
data class DiscoveredMovie(
    val title: String,
    val releaseYear: Int? = null,
    val releaseDate: String? = null,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val genres: List<String> = emptyList(),
    val synopsis: String = "",
    val director: String = "",
    val cast: List<String> = emptyList(),
    val runtimeMinutes: Int? = null,
    val language: String? = "English",
    val externalRating: String? = null,
    val sourceUrl: String? = null,
    val sourceName: String = "Wikipedia",
    val contentType: String = EntertainmentType.MOVIE,
    val alternateTitles: List<String> = emptyList(),
    val imdbId: String? = null,
    val similarTitles: List<String> = emptyList(),
    val matchReason: String? = null,
    val relevanceScore: Float = 0f
) {
    val googleSearchUrl: String
        get() {
            val q = URLEncoder.encode("$title ${releaseYear ?: ""} movie series", "UTF-8")
            return "https://www.google.com/search?q=$q"
        }

    val imdbSearchUrl: String
        get() {
            return if (!imdbId.isNullOrBlank()) {
                "https://www.imdb.com/title/$imdbId/"
            } else {
                val q = URLEncoder.encode(title, "UTF-8")
                "https://www.imdb.com/find?q=$q"
            }
        }

    val tmdbSearchUrl: String
        get() {
            val q = URLEncoder.encode(title, "UTF-8")
            return "https://www.themoviedb.org/search?query=$q"
        }

    fun toMovieEntity(): MovieEntity {
        return MovieEntity(
            title = title,
            originalTitle = alternateTitles.firstOrNull(),
            releaseYear = releaseYear,
            releaseDate = releaseDate,
            posterUrl = posterUrl,
            backdropUrl = backdropUrl,
            genres = genres.joinToString(", "),
            synopsis = synopsis,
            director = director,
            cast = cast.joinToString(", "),
            runtimeMinutes = runtimeMinutes,
            language = language ?: "English",
            externalRating = externalRating,
            sourceUrl = sourceUrl,
            sourceAttribution = sourceName,
            personalRating = 0f,
            watchStatus = WatchStatus.PLAN_TO_WATCH,
            contentType = contentType,
            alternateTitles = alternateTitles.joinToString(", "),
            imdbId = imdbId,
            similarTitles = similarTitles.joinToString(", ")
        )
    }
}

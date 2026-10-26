package com.example.service

import com.example.data.MovieEntity
import com.example.data.WatchStatus

/**
 * Movie metadata discovered from public web sources without requiring an API key.
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
    val sourceName: String = "Wikipedia"
) {
    fun toMovieEntity(): MovieEntity {
        return MovieEntity(
            title = title,
            originalTitle = null,
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
            watchStatus = WatchStatus.PLAN_TO_WATCH
        )
    }
}

package com.example

import com.example.data.MovieEntity
import com.example.data.WatchStatus
import com.example.service.DiscoveredMovie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CineVaultUnitTest {

    @Test
    fun testMovieEntityRuntimeFormatting() {
        val movie = MovieEntity(
            title = "Interstellar",
            releaseYear = 2014,
            runtimeMinutes = 169
        )

        assertEquals("2h 49m", movie.formattedRuntime)
        assertEquals("Interstellar", movie.title)
        assertEquals(2014, movie.releaseYear)
    }

    @Test
    fun testMovieEntityGenresListParsing() {
        val movie = MovieEntity(
            title = "Inception",
            genres = "Action, Sci-Fi, Thriller"
        )

        assertEquals(3, movie.genresList.size)
        assertTrue(movie.genresList.contains("Sci-Fi"))
        assertTrue(movie.genresList.contains("Action"))
        assertTrue(movie.genresList.contains("Thriller"))
    }

    @Test
    fun testDiscoveredMovieToMovieEntity() {
        val discovered = DiscoveredMovie(
            title = "Interstellar",
            releaseYear = 2014,
            posterUrl = "https://example.com/poster.jpg",
            genres = listOf("Sci-Fi", "Drama"),
            synopsis = "A team of explorers travel through a wormhole in space...",
            director = "Christopher Nolan",
            cast = listOf("Matthew McConaughey", "Anne Hathaway"),
            runtimeMinutes = 169
        )

        val entity = discovered.toMovieEntity()
        assertEquals("Interstellar", entity.title)
        assertEquals(2014, entity.releaseYear)
        assertEquals("Sci-Fi, Drama", entity.genres)
        assertEquals(WatchStatus.PLAN_TO_WATCH, entity.watchStatus)
        assertEquals(0f, entity.personalRating)
    }

    @Test
    fun testWatchStatusDisplayNames() {
        assertEquals("Completed", WatchStatus.getDisplayName(WatchStatus.COMPLETED))
        assertEquals("Watching", WatchStatus.getDisplayName(WatchStatus.WATCHING))
        assertEquals("Plan to Watch", WatchStatus.getDisplayName(WatchStatus.PLAN_TO_WATCH))
        assertEquals("Dropped", WatchStatus.getDisplayName(WatchStatus.DROPPED))
    }
}

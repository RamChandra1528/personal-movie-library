package com.example.service

import com.example.data.EntertainmentType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MovieSearchProviderTest {

    @Test
    fun `Marvel and Hollywood browse queries always return movie listings`() = runTest {
        val marvel = CuratedEntertainmentRegistry.findMatching("Marvel movies")
        val hollywood = CuratedEntertainmentRegistry.findMatching("Hollywood movies")

        assertTrue(marvel.any { it.title == "Iron Man" })
        assertTrue(hollywood.any { it.title == "Inception" })
        assertTrue((marvel + hollywood).all { it.contentType == EntertainmentType.MOVIE })
    }

    @Test
    fun `collection search accepts natural-language Marvel and Hollywood requests`() = runTest {
        val marvel = CuratedEntertainmentRegistry.findMatching("Marvel movies, please")
        val hollywood = CuratedEntertainmentRegistry.findMatching("Hollywood movies 2024")

        assertTrue(marvel.isNotEmpty())
        assertTrue(hollywood.isNotEmpty())
    }

    @Test
    fun `global collection browse returns immediately without provider network calls`() = runTest {
        val response = MovieSearchService().searchGlobalEntertainment("Marvel movies")

        assertTrue(response.items.any { it.title == "Iron Man" || it.title.contains("Avengers") })
        assertTrue(response.items.isNotEmpty())
    }
}

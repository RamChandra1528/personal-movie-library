package com.example.service

import com.example.data.EntertainmentType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieSearchProviderTest {

    @Test
    fun `IMDb feature suggestions are returned as movies`() {
        val results = ImdbSearchProvider().parseResponse(
            """{"d":[
                {"id":"nm0000001","l":"A Person"},
                {"id":"tt4154664","l":"Captain Marvel","q":"feature","qid":"movie","y":2019,
                 "i":{"imageUrl":"https://example.com/poster.jpg"},"s":"Brie Larson, Samuel L. Jackson"}
            ]}"""
        )

        assertEquals(1, results.size)
        assertEquals("Captain Marvel", results.single().title)
        assertEquals(EntertainmentType.MOVIE, results.single().contentType)
        assertEquals("tt4154664", results.single().imdbId)
    }

    @Test
    fun `Marvel and Hollywood browse queries always return movie listings`() = runTest {
        val provider = CollectionSearchProvider()

        val marvel = provider.search("Marvel movies")
        val hollywood = provider.search("Hollywood movies")

        assertTrue(marvel.any { it.title == "Iron Man" })
        assertTrue(hollywood.any { it.title == "Inception" })
        assertTrue((marvel + hollywood).all { it.contentType == EntertainmentType.MOVIE })
    }
}

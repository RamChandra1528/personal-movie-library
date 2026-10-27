package com.example

import com.example.data.EntertainmentType
import com.example.data.MovieEntity
import com.example.data.WatchStatus
import com.example.data.toCloudMovie
import com.example.service.DiscoveredMovie
import com.example.service.FuzzyRelevanceRanker
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
            runtimeMinutes = 169,
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Interstellar: The IMAX Experience"),
            similarTitles = listOf("Inception", "Arrival")
        )

        val entity = discovered.toMovieEntity()
        assertEquals("Interstellar", entity.title)
        assertEquals(2014, entity.releaseYear)
        assertEquals("Sci-Fi, Drama", entity.genres)
        assertEquals(WatchStatus.PLAN_TO_WATCH, entity.watchStatus)
        assertEquals(EntertainmentType.MOVIE, entity.contentType)
        assertEquals("Inception, Arrival", entity.similarTitles)
    }

    @Test
    fun testFuzzyRelevanceRankerExactAndPartialMatches() {
        val movie = DiscoveredMovie(
            title = "The Dark Knight",
            releaseYear = 2008,
            director = "Christopher Nolan",
            cast = listOf("Christian Bale", "Heath Ledger"),
            genres = listOf("Action", "Crime", "Drama"),
            alternateTitles = listOf("Batman: The Dark Knight")
        )

        // Exact match
        val (exactScore, exactReason) = FuzzyRelevanceRanker.scoreAndExplain(movie, "The Dark Knight")
        assertEquals(100f, exactScore, 0.01f)
        assertEquals("Exact Title Match", exactReason)

        // Prefix match
        val (prefixScore, prefixReason) = FuzzyRelevanceRanker.scoreAndExplain(movie, "The Dark")
        assertEquals(90f, prefixScore, 0.01f)
        assertEquals("Prefix Title Match", prefixReason)

        // Alternate title match
        val (altScore, _) = FuzzyRelevanceRanker.scoreAndExplain(movie, "Batman: The Dark Knight")
        assertEquals(80f, altScore, 0.01f)

        // Actor match
        val (actorScore, actorReason) = FuzzyRelevanceRanker.scoreAndExplain(movie, "Heath Ledger")
        assertEquals(75f, actorScore, 0.01f)
        assertTrue(actorReason.contains("Heath Ledger"))

        // Director match
        val (dirScore, dirReason) = FuzzyRelevanceRanker.scoreAndExplain(movie, "Christopher Nolan")
        assertEquals(74f, dirScore, 0.01f)
        assertTrue(dirReason.contains("Christopher Nolan"))
    }

    @Test
    fun testFuzzyRelevanceRankerSpellingMistake() {
        val movie = DiscoveredMovie(
            title = "Interstellar",
            releaseYear = 2014
        )

        // Minor typo: "Interstellr" vs "Interstellar"
        val similarity = FuzzyRelevanceRanker.calculateSimilarity("interstellr", "interstellar")
        assertTrue("Similarity should be high for 1 missing letter: $similarity", similarity >= 0.85f)

        val (fuzzyScore, fuzzyReason) = FuzzyRelevanceRanker.scoreAndExplain(movie, "interstellr")
        assertTrue(fuzzyScore >= 60f)
        assertTrue(fuzzyReason.contains("Fuzzy Match"))
    }

    @Test
    fun testEntertainmentTypeDisplayNamesAndIcons() {
        assertEquals("Movie", EntertainmentType.getDisplayName(EntertainmentType.MOVIE))
        assertEquals("TV Series", EntertainmentType.getDisplayName(EntertainmentType.TV_SERIES))
        assertEquals("Anime", EntertainmentType.getDisplayName(EntertainmentType.ANIME))
        assertEquals("Drama", EntertainmentType.getDisplayName(EntertainmentType.DRAMA))

        assertEquals("🎬", EntertainmentType.getIcon(EntertainmentType.MOVIE))
        assertEquals("📺", EntertainmentType.getIcon(EntertainmentType.TV_SERIES))
        assertEquals("⚡", EntertainmentType.getIcon(EntertainmentType.ANIME))
        assertEquals("🎭", EntertainmentType.getIcon(EntertainmentType.DRAMA))
    }

    @Test
    fun testWatchStatusDisplayNames() {
        assertEquals("Completed", WatchStatus.getDisplayName(WatchStatus.COMPLETED))
        assertEquals("Watching", WatchStatus.getDisplayName(WatchStatus.WATCHING))
        assertEquals("Plan to Watch", WatchStatus.getDisplayName(WatchStatus.PLAN_TO_WATCH))
        assertEquals("Dropped", WatchStatus.getDisplayName(WatchStatus.DROPPED))
    }

    @Test
    fun testCuratedMarvelMoviesSearch() {
        val marvelResults = com.example.service.CuratedEntertainmentRegistry.findMatching("Marvel movies")
        assertTrue("Marvel movies search should return results", marvelResults.isNotEmpty())
        val titles = marvelResults.map { it.title }
        assertTrue("Should include Avengers: Endgame", titles.contains("Avengers: Endgame"))
        assertTrue("Should include Iron Man", titles.contains("Iron Man"))
        assertTrue("Should include Spider-Man: No Way Home", titles.contains("Spider-Man: No Way Home"))

        // Every Marvel movie should have valid poster and release year
        marvelResults.forEach { movie ->
            assertTrue(movie.posterUrl != null && movie.posterUrl!!.startsWith("http"))
            assertTrue(movie.releaseYear != null && movie.releaseYear!! >= 2008)
        }
    }

    @Test
    fun testCuratedHollywoodMoviesSearch() {
        val hollywoodResults = com.example.service.CuratedEntertainmentRegistry.findMatching("Hollywood movies")
        assertTrue("Hollywood movies search should return results", hollywoodResults.isNotEmpty())
        val titles = hollywoodResults.map { it.title }
        assertTrue("Should include Inception", titles.contains("Inception"))
        assertTrue("Should include Titanic", titles.contains("Titanic"))
        assertTrue("Should include The Dark Knight", titles.contains("The Dark Knight"))
        assertTrue("Should include Interstellar", titles.contains("Interstellar"))

        hollywoodResults.forEach { movie ->
            assertTrue(movie.posterUrl != null && movie.posterUrl!!.startsWith("http"))
            assertTrue(movie.director.isNotBlank())
        }
    }

    @Test
    fun testFuzzyRelevanceRankerFranchiseScoring() {
        val endgame = com.example.service.CuratedEntertainmentRegistry.ALL_CURATED.first { it.title == "Avengers: Endgame" }
        val (scoreMarvel, reasonMarvel) = FuzzyRelevanceRanker.scoreAndExplain(endgame, "Marvel movies")
        assertTrue("Endgame should have high score for 'Marvel movies': $scoreMarvel", scoreMarvel >= 90f)
        assertTrue(reasonMarvel.contains("Marvel"))

        val inception = com.example.service.CuratedEntertainmentRegistry.ALL_CURATED.first { it.title == "Inception" }
        val (scoreHw, reasonHw) = FuzzyRelevanceRanker.scoreAndExplain(inception, "Hollywood movies")
        assertTrue("Inception should have high score for 'Hollywood movies': $scoreHw", scoreHw >= 90f)
        assertTrue(reasonHw.contains("Hollywood"))
    }

    @Test
    fun testCloudMovieMappingAndUserIsolation() {
        val original = MovieEntity(
            id = 42L,
            title = "The Matrix",
            releaseYear = 1999,
            director = "Lana & Lilly Wachowski",
            personalRating = 5.0f,
            watchStatus = WatchStatus.COMPLETED,
            isFavorite = true
        )

        val testUserId = "user_test_abc_123"
        val cloudMovie = original.toCloudMovie(
            userId = testUserId,
            cloudDocId = "movie_the_matrix_1999"
        )

        assertEquals("movie_the_matrix_1999", cloudMovie.id)
        assertEquals(testUserId, cloudMovie.userId)
        assertEquals("The Matrix", cloudMovie.title)
        assertEquals(1999, cloudMovie.releaseYear)
        assertEquals(5.0f, cloudMovie.personalRating)
        assertEquals(WatchStatus.COMPLETED, cloudMovie.watchStatus)
        assertTrue(cloudMovie.isFavorite)

        val writeMap = cloudMovie.toWriteMap(isCreate = true)
        assertEquals("movie_the_matrix_1999", writeMap["id"])
        assertEquals("The Matrix", writeMap["title"])
        assertEquals(testUserId, writeMap["userId"])
        assertTrue(writeMap.containsKey("createdAt"))
        assertTrue(writeMap.containsKey("updatedAt"))

        val restoredEntity = cloudMovie.toEntity(localId = 42L)
        assertEquals(42L, restoredEntity.id)
        assertEquals("The Matrix", restoredEntity.title)
        assertEquals(1999, restoredEntity.releaseYear)
        assertEquals(5.0f, restoredEntity.personalRating)
        assertTrue(restoredEntity.isFavorite)
    }

    @Test
    fun testIndustryQueryNormalization() {
        assertEquals("bollywood", FuzzyRelevanceRanker.normalizeEntertainmentQuery("bollywoood"))
        assertEquals("tollywood", FuzzyRelevanceRanker.normalizeEntertainmentQuery("tollywoood"))
        assertEquals("web series", FuzzyRelevanceRanker.normalizeEntertainmentQuery("we sresies"))
        assertEquals("tv series", FuzzyRelevanceRanker.normalizeEntertainmentQuery("tv resylt"))
        assertEquals("indian", FuzzyRelevanceRanker.normalizeEntertainmentQuery("o indian"))
    }

    @Test
    fun testCuratedBollywoodAndTollywoodMatches() {
        val bollywoodResults = com.example.service.CuratedEntertainmentRegistry.findMatching("bollywoood")
        assertTrue("Bollywood search with typos should return matches", bollywoodResults.isNotEmpty())
        assertTrue(bollywoodResults.any { it.title.equals("Jawan", ignoreCase = true) || it.title.equals("Pathaan", ignoreCase = true) })

        val tollywoodResults = com.example.service.CuratedEntertainmentRegistry.findMatching("tollywoood")
        assertTrue("Tollywood search with typos should return matches", tollywoodResults.isNotEmpty())
        assertTrue(tollywoodResults.any { it.title.contains("Pushpa", ignoreCase = true) || it.title.contains("RRR", ignoreCase = true) })
    }

    @Test
    fun testCuratedTvAndWebSeriesMatches() {
        val tvResults = com.example.service.CuratedEntertainmentRegistry.findMatching("tv resylt")
        assertTrue("TV series search with typos should return matches", tvResults.isNotEmpty())
        assertTrue(tvResults.any { it.contentType == EntertainmentType.TV_SERIES })

        val webSeriesResults = com.example.service.CuratedEntertainmentRegistry.findMatching("we sresies")
        assertTrue("Web series search with typos should return matches", webSeriesResults.isNotEmpty())
        assertTrue(webSeriesResults.any { it.title.equals("Mirzapur", ignoreCase = true) || it.title.equals("Panchayat", ignoreCase = true) || it.title.equals("Breaking Bad", ignoreCase = true) })
    }

    @Test
    fun testCuratedRegistryTotalCoverage() {
        val all = com.example.service.CuratedEntertainmentRegistry.ALL_CURATED
        assertTrue("Curated database should have at least 40 entries", all.size >= 40)
        assertTrue("Curated database should contain TV series", all.any { it.contentType == EntertainmentType.TV_SERIES })
        assertTrue("Curated database should contain Bollywood films", all.any { it.sourceName.contains("Bollywood") || (it.language?.contains("Hindi") == true) })
        assertTrue("Curated database should contain Tollywood films", all.any { it.sourceName.contains("Tollywood") || (it.language?.contains("Telugu") == true) })
    }
}

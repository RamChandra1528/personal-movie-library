package com.example.service

import kotlin.math.max
import kotlin.math.min

object FuzzyRelevanceRanker {

    /**
     * Compute relevance score and match reason for a candidate entertainment item against a search query.
     */
    fun scoreAndExplain(movie: DiscoveredMovie, query: String): Pair<Float, String> {
        val q = query.trim().lowercase()
        val titleLower = movie.title.lowercase()
        val cleanKeyword = q
            .replace("movies", "")
            .replace("movie", "")
            .replace("films", "")
            .replace("film", "")
            .trim()

        // 0. Special Studio / Franchise Queries ("Marvel movies", "Hollywood movies", "MCU", etc.)
        if (q.contains("marvel") || q.contains("mcu")) {
            val isMarvel = movie.sourceName.contains("Marvel", ignoreCase = true) ||
                    movie.alternateTitles.any { it.contains("Marvel", ignoreCase = true) } ||
                    movie.genres.any { it.contains("Superhero", ignoreCase = true) } ||
                    titleLower.contains("avenger") || titleLower.contains("spider-man") ||
                    titleLower.contains("iron man") || titleLower.contains("thor") ||
                    titleLower.contains("deadpool") || titleLower.contains("black panther")
            if (isMarvel) {
                return 96f to "Marvel Cinematic Universe"
            }
        }

        if (q.contains("hollywood") || q.contains("blockbuster")) {
            val isHollywood = movie.sourceName.contains("Hollywood", ignoreCase = true) ||
                    movie.sourceName.contains("Warner", ignoreCase = true) ||
                    movie.sourceName.contains("Paramount", ignoreCase = true) ||
                    movie.sourceName.contains("Universal", ignoreCase = true) ||
                    movie.sourceName.contains("Century Fox", ignoreCase = true) ||
                    movie.director.contains("Nolan", ignoreCase = true) ||
                    movie.director.contains("Spielberg", ignoreCase = true) ||
                    movie.director.contains("Cameron", ignoreCase = true)
            if (isHollywood) {
                return 95f to "Hollywood Cinema Blockbuster"
            }
        }

        // 1. Exact title match (with full query or stripped query)
        if (titleLower == q || (cleanKeyword.isNotEmpty() && titleLower == cleanKeyword)) {
            return 100f to "Exact Title Match"
        }

        // 2. Starts with / Prefix match
        if (titleLower.startsWith(q) || (cleanKeyword.isNotEmpty() && titleLower.startsWith(cleanKeyword))) {
            return 90f to "Prefix Title Match"
        }

        // 3. Contains title substring
        if (titleLower.contains(q) || (cleanKeyword.isNotEmpty() && titleLower.contains(cleanKeyword))) {
            return 85f to "Title Match"
        }

        // 4. Alternate titles match
        for (alt in movie.alternateTitles) {
            val altLower = alt.lowercase()
            if (altLower == q || altLower.contains(q) || (cleanKeyword.isNotEmpty() && altLower.contains(cleanKeyword))) {
                return 80f to "Alternate Title: $alt"
            }
        }

        // 5. Year query match (e.g. "1999" or "2024")
        val queryYear = q.toIntOrNull()
        if (queryYear != null && movie.releaseYear == queryYear) {
            return 78f to "Released in $queryYear"
        }

        // 6. Actor / Cast match
        for (actor in movie.cast) {
            val actorLower = actor.lowercase()
            if (actorLower.contains(q) || q.contains(actorLower) ||
                (cleanKeyword.isNotEmpty() && actorLower.contains(cleanKeyword))) {
                return 75f to "Cast: $actor"
            }
        }

        // 7. Director match
        if (movie.director.isNotBlank()) {
            val dirLower = movie.director.lowercase()
            if (dirLower.contains(q) || q.contains(dirLower) ||
                (cleanKeyword.isNotEmpty() && dirLower.contains(cleanKeyword))) {
                return 74f to "Director: ${movie.director}"
            }
        }

        // 8. Genre match
        for (genre in movie.genres) {
            val genreLower = genre.lowercase()
            if (genreLower.contains(q) || (cleanKeyword.isNotEmpty() && genreLower.contains(cleanKeyword))) {
                return 65f to "Genre: $genre"
            }
        }

        // 9. Fuzzy Levenshtein similarity on title
        val similarity = max(
            calculateSimilarity(titleLower, q),
            if (cleanKeyword.isNotEmpty()) calculateSimilarity(titleLower, cleanKeyword) else 0f
        )
        if (similarity >= 0.70f) {
            val pct = (similarity * 100).toInt()
            return (60f + similarity * 20f) to "Fuzzy Match ($pct%)"
        }

        // 10. Keyword in synopsis
        if (movie.synopsis.lowercase().contains(q) ||
            (cleanKeyword.isNotEmpty() && movie.synopsis.lowercase().contains(cleanKeyword))) {
            return 45f to "Theme / Keyword Match"
        }

        return 20f to "Related Entertainment"
    }

    /**
     * Compute normalized Levenshtein similarity between 0.0 and 1.0.
     */
    fun calculateSimilarity(s1: String, s2: String): Float {
        val longer = if (s1.length >= s2.length) s1 else s2
        val shorter = if (s1.length >= s2.length) s2 else s1
        val longerLength = longer.length
        if (longerLength == 0) return 1.0f

        val editDistance = levenshteinDistance(longer, shorter)
        return (longerLength - editDistance) / longerLength.toFloat()
    }

    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val len0 = lhs.length + 1
        val len1 = rhs.length + 1

        var cost = IntArray(len0)
        var newCost = IntArray(len0)

        for (i in 0 until len0) cost[i] = i

        for (j in 1 until len1) {
            newCost[0] = j
            for (i in 1 until len0) {
                val match = if (lhs[i - 1] == rhs[j - 1]) 0 else 1
                val costReplace = cost[i - 1] + match
                val costInsert = cost[i] + 1
                val costDelete = newCost[i - 1] + 1
                newCost[i] = min(min(costInsert, costDelete), costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[len0 - 1]
    }
}

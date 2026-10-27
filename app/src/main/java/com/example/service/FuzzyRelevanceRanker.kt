package com.example.service

import kotlin.math.max
import kotlin.math.min

object FuzzyRelevanceRanker {

    /**
     * Compute relevance score and match reason for a candidate entertainment item against a search query.
     */
    fun scoreAndExplain(movie: DiscoveredMovie, query: String): Pair<Float, String> {
        val normalizedQuery = normalizeEntertainmentQuery(query)
        val q = normalizedQuery.trim().lowercase()
        val originalLower = query.trim().lowercase()
        val titleLower = movie.title.lowercase()
        val cleanKeyword = q
            .replace("movies", "")
            .replace("movie", "")
            .replace("films", "")
            .replace("film", "")
            .replace("series", "")
            .replace("shows", "")
            .replace("show", "")
            .trim()

        // 0. Special Industry & Franchise Recognition

        // A. Bollywood / Hindi Cinema
        val isBollywoodSearch = q.contains("bollywood") || q.contains("hindi") ||
                originalLower.contains("bolly") || originalLower.contains("hindi")
        if (isBollywoodSearch) {
            val isBollywood = (movie.language?.contains("Hindi", ignoreCase = true) == true) ||
                    movie.sourceName.contains("Bollywood", ignoreCase = true) ||
                    movie.sourceName.contains("Hindi", ignoreCase = true) ||
                    movie.cast.any { it.contains("Khan", ignoreCase = true) || it.contains("Kapoor", ignoreCase = true) || it.contains("Bachchan", ignoreCase = true) }
            if (isBollywood) {
                return 97f to "Bollywood (Hindi Cinema)"
            }
        }

        // B. Tollywood / Telugu / South Indian Cinema
        val isTollywoodSearch = q.contains("tollywood") || q.contains("telugu") ||
                originalLower.contains("tolly") || originalLower.contains("telugu") ||
                q.contains("south movie")
        if (isTollywoodSearch) {
            val isTollywood = (movie.language?.contains("Telugu", ignoreCase = true) == true) ||
                    movie.sourceName.contains("Tollywood", ignoreCase = true) ||
                    movie.sourceName.contains("Telugu", ignoreCase = true) ||
                    movie.director.contains("Rajamouli", ignoreCase = true) ||
                    movie.director.contains("Sukumar", ignoreCase = true) ||
                    movie.cast.any { it.contains("Prabhas", ignoreCase = true) || it.contains("Allu Arjun", ignoreCase = true) || it.contains("Ram Charan", ignoreCase = true) || it.contains("NTR", ignoreCase = true) }
            if (isTollywood) {
                return 97f to "Tollywood (Telugu Cinema)"
            }
        }

        // C. Indian Cinema (Pan-India)
        val isIndianSearch = q.contains("indian") || originalLower.contains("indian") ||
                originalLower.contains("o indian") || q.contains("desi") || q.contains("bharat")
        if (isIndianSearch) {
            val isIndian = movie.sourceName.contains("Indian", ignoreCase = true) ||
                    (movie.language?.contains("Hindi", ignoreCase = true) == true) ||
                    (movie.language?.contains("Telugu", ignoreCase = true) == true) ||
                    (movie.language?.contains("Tamil", ignoreCase = true) == true) ||
                    (movie.language?.contains("Kannada", ignoreCase = true) == true) ||
                    (movie.language?.contains("Malayalam", ignoreCase = true) == true)
            if (isIndian) {
                return 96f to "Indian Cinema"
            }
        }

        // D. TV Series, Web Series & Television Shows
        val isTvOrWebSearch = q.contains("tv") || q.contains("series") || q.contains("web series") ||
                q.contains("show") || originalLower.contains("tv") || originalLower.contains("sresies") ||
                originalLower.contains("resylt")
        if (isTvOrWebSearch) {
            val isTvOrWeb = movie.contentType == com.example.data.EntertainmentType.TV_SERIES ||
                    movie.contentType == com.example.data.EntertainmentType.DRAMA ||
                    movie.sourceName.contains("TV", ignoreCase = true) ||
                    movie.sourceName.contains("Series", ignoreCase = true) ||
                    movie.sourceName.contains("HBO", ignoreCase = true) ||
                    movie.sourceName.contains("Netflix", ignoreCase = true) ||
                    movie.sourceName.contains("Amazon", ignoreCase = true) ||
                    movie.sourceName.contains("Hotstar", ignoreCase = true) ||
                    movie.sourceName.contains("Sony LIV", ignoreCase = true)
            if (isTvOrWeb) {
                return 96f to "TV & Web Series"
            }
        }

        // E. Marvel Cinematic Universe
        if (q.contains("marvel") || q.contains("mcu") || originalLower.contains("marvel")) {
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

        // F. Hollywood & Blockbusters
        if (q.contains("hollywood") || q.contains("blockbuster") || originalLower.contains("hollywood")) {
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

    /**
     * Normalizes queries by collapsing repeated typo letters and mapping industry slang/typos.
     */
    fun normalizeEntertainmentQuery(query: String): String {
        var q = query.trim().lowercase()
        if (q.isBlank()) return ""

        // Collapse 3 or more repeated letters to 2 (e.g. bollywoood -> bollywood, tollywoood -> tollywood)
        q = q.replace(Regex("([a-z])\\1{2,}"), "$1$1")

        val typoMap = mapOf(
            "bollywoood" to "bollywood",
            "bollywod" to "bollywood",
            "bolywood" to "bollywood",
            "bolliwood" to "bollywood",
            "tollywoood" to "tollywood",
            "tollywod" to "tollywood",
            "tolywood" to "tollywood",
            "tolliwood" to "tollywood",
            "kollywoood" to "kollywood",
            "kollywod" to "kollywood",
            "kolywood" to "kollywood",
            "hollywoood" to "hollywood",
            "hollywod" to "hollywood",
            "we sresies" to "web series",
            "we series" to "web series",
            "web sresies" to "web series",
            "webseries" to "web series",
            "tv resylt" to "tv series",
            "tv result" to "tv series",
            "tv resylts" to "tv series",
            "tv results" to "tv series",
            "tv show" to "tv series",
            "tv shows" to "tv series",
            "tv serie" to "tv series",
            "tv seris" to "tv series",
            "o indian" to "indian",
            "indiqn" to "indian",
            "indain" to "indian",
            "mview" to "movie",
            "mviews" to "movies",
            "mvoie" to "movie",
            "mvoies" to "movies"
        )

        for ((typo, corrected) in typoMap) {
            if (q == typo) {
                return corrected
            }
            q = q.replace(Regex("\\b${Regex.escape(typo)}\\b"), corrected)
        }
        return q.trim()
    }
}

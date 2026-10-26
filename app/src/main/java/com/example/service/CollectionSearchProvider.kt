package com.example.service

import com.example.data.EntertainmentType

/** Offline discovery fallback for broad or misspelled browse requests. */
class CollectionSearchProvider : MovieSearchProvider {
    override val providerName: String = "CineVault discovery guide"

    override suspend fun search(query: String): List<DiscoveredMovie> {
        val featured = featuredCollectionFor(query)
        if (featured.isNotEmpty()) return featured

        return allTitles
            .map { item ->
                val (score, reason) = FuzzyRelevanceRanker.scoreAndExplain(item, query)
                item.copy(relevanceScore = score, matchReason = reason)
            }
            .filter { it.relevanceScore >= 64f }
            .sortedByDescending { it.relevanceScore }
            .take(MAX_RESULTS)
    }

    /** Returns a collection for exact and commonly misspelled category requests. */
    fun featuredCollectionFor(query: String): List<DiscoveredMovie> {
        val normalized = normalize(query)
        val collection = when {
            matchesCategory(normalized, marvelAliases) -> marvelMovies
            matchesCategory(normalized, hollywoodAliases) -> hollywoodMovies
            matchesCategory(normalized, bollywoodAliases) -> bollywoodMovies
            matchesCategory(normalized, southIndianAliases) -> southIndianMovies
            matchesCategory(normalized, animeAliases) -> animeMovies
            matchesCategory(normalized, worldAliases) -> worldwideMovies
            isGenericBrowse(normalized) -> worldwideMovies
            else -> emptyList()
        }
        return collection.map { it.copy(relevanceScore = 100f, matchReason = "Featured collection") }
    }

    fun searchByYear(year: Int): List<DiscoveredMovie> = allTitles
        .filter { it.releaseYear == year }
        .map { it.copy(relevanceScore = 100f, matchReason = "Released in $year") }

    override suspend fun getMovieDetails(identifier: String): DiscoveredMovie? =
        allTitles.firstOrNull { it.title.equals(identifier, ignoreCase = true) }

    private fun isGenericBrowse(query: String): Boolean = compact(query) in setOf(
        "movie", "movies", "film", "films", "cinema", "entertainment",
        "topmovies", "bestmovies", "trendingmovies", "newmovies", "worldmovies"
    )

    private fun matchesCategory(query: String, aliases: Set<String>): Boolean {
        val compactQuery = compact(query)
        if (compactQuery.isBlank()) return false
        return aliases.any { alias ->
            val compactAlias = compact(alias)
            compactQuery == compactAlias ||
                compactQuery.contains(compactAlias) ||
                compactAlias.contains(compactQuery) ||
                FuzzyRelevanceRanker.calculateSimilarity(compactQuery, compactAlias) >= 0.76f
        }
    }

    private fun normalize(query: String): String = query
        .trim()
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun compact(value: String): String = value.replace(Regex("[^a-z0-9]"), "")

    private companion object {
        const val MAX_RESULTS = 12

        fun movie(
            title: String,
            year: Int,
            genres: List<String>,
            description: String,
            language: String = "English",
            type: String = EntertainmentType.MOVIE,
            director: String = "See IMDb for full credits"
        ) = DiscoveredMovie(
            title = title,
            releaseYear = year,
            releaseDate = year.toString(),
            genres = genres,
            synopsis = description,
            director = director,
            language = language,
            sourceUrl = "https://www.imdb.com/find?q=${java.net.URLEncoder.encode(title, "UTF-8")}",
            sourceName = "CineVault discovery guide",
            contentType = type
        )

        val marvelAliases = setOf("marvel", "marvel movie", "marvel movies", "marvel films", "mcu", "mcu movies", "marvel cinematic universe")
        val hollywoodAliases = setOf("hollywood", "hollywood movie", "hollywood movies", "hollywood films", "english movies", "english movie", "holly word", "hollywod")
        val bollywoodAliases = setOf("bollywood", "bollywood movie", "bollywood movies", "hindi movie", "hindi movies", "indian hindi movies", "bolly word", "bolywood")
        val southIndianAliases = setOf("south indian", "south indian movies", "south indain", "south indain cinema", "south movies", "south cinema", "tollywood", "tamil movies", "telugu movies", "malayalam movies", "kannada movies", "south india")
        val animeAliases = setOf("anime", "anime movies", "japanese anime", "animation")
        val worldAliases = setOf("worldwide", "world wide", "world movies", "international movies", "global movies", "foreign movies", "world cinema")

        val marvelMovies = listOf(
            movie("Iron Man", 2008, listOf("Action", "Adventure", "Superhero"), "Tony Stark builds an armored suit and becomes Iron Man."),
            movie("The Avengers", 2012, listOf("Action", "Adventure", "Superhero"), "Earth's mightiest heroes unite to stop Loki."),
            movie("Guardians of the Galaxy", 2014, listOf("Action", "Adventure", "Sci-Fi"), "A band of outlaws protects a powerful orb."),
            movie("Black Panther", 2018, listOf("Action", "Adventure", "Superhero"), "T'Challa returns to Wakanda to take his place as king."),
            movie("Avengers: Endgame", 2019, listOf("Action", "Adventure", "Superhero"), "The Avengers attempt to reverse a universe-changing loss."),
            movie("Spider-Man: No Way Home", 2021, listOf("Action", "Adventure", "Superhero"), "Spider-Man faces visitors from across the multiverse.")
        )

        val hollywoodMovies = listOf(
            movie("The Shawshank Redemption", 1994, listOf("Drama"), "A banker forms an enduring friendship while imprisoned."),
            movie("The Dark Knight", 2008, listOf("Action", "Crime", "Drama"), "Batman faces Gotham's most dangerous criminal.", director = "Christopher Nolan"),
            movie("Inception", 2010, listOf("Action", "Sci-Fi", "Thriller"), "A thief enters dreams to plant an idea.", director = "Christopher Nolan"),
            movie("Interstellar", 2014, listOf("Adventure", "Drama", "Sci-Fi"), "Explorers travel through a wormhole to seek humanity's future.", director = "Christopher Nolan"),
            movie("Mad Max: Fury Road", 2015, listOf("Action", "Adventure", "Sci-Fi"), "Fugitives race across a post-apocalyptic wasteland."),
            movie("Everything Everywhere All at Once", 2022, listOf("Action", "Adventure", "Comedy"), "An unlikely hero connects with parallel versions of her life."),
            movie("Oppenheimer", 2023, listOf("Biography", "Drama", "History"), "The story of J. Robert Oppenheimer and the atomic bomb.", director = "Christopher Nolan")
        )

        val bollywoodMovies = listOf(
            movie("3 Idiots", 2009, listOf("Comedy", "Drama"), "Three engineering students question the pressure of academic success.", language = "Hindi", director = "Rajkumar Hirani"),
            movie("Zindagi Na Milegi Dobara", 2011, listOf("Comedy", "Drama", "Adventure"), "Three friends take a transformative road trip through Spain.", language = "Hindi", director = "Zoya Akhtar"),
            movie("Kahaani", 2012, listOf("Mystery", "Thriller"), "A pregnant woman searches Kolkata for her missing husband.", language = "Hindi", director = "Sujoy Ghosh"),
            movie("Dangal", 2016, listOf("Biography", "Drama", "Sport"), "A former wrestler trains his daughters to become champions.", language = "Hindi", director = "Nitesh Tiwari"),
            movie("Andhadhun", 2018, listOf("Crime", "Comedy", "Thriller"), "A pianist becomes entangled in a murder mystery.", language = "Hindi", director = "Sriram Raghavan"),
            movie("Gully Boy", 2019, listOf("Drama", "Music"), "A young man from Mumbai pursues a dream of becoming a rapper.", language = "Hindi", director = "Zoya Akhtar"),
            movie("12th Fail", 2023, listOf("Biography", "Drama"), "A student overcomes hardship while preparing for civil services.", language = "Hindi", director = "Vidhu Vinod Chopra")
        )

        val southIndianMovies = listOf(
            movie("Baahubali: The Beginning", 2015, listOf("Action", "Adventure", "Drama"), "A young man discovers his royal heritage.", language = "Telugu", director = "S. S. Rajamouli"),
            movie("Vikram Vedha", 2017, listOf("Action", "Crime", "Thriller"), "A police officer hunts a gangster who tells moral tales.", language = "Tamil", director = "Pushkar–Gayathri"),
            movie("Kumbalangi Nights", 2019, listOf("Comedy", "Drama"), "Four brothers navigate family, love, and responsibility.", language = "Malayalam", director = "Madhu C. Narayanan"),
            movie("Jersey", 2019, listOf("Drama", "Sport"), "A former cricketer returns to the game for his son.", language = "Telugu", director = "Gowtam Tinnanuri"),
            movie("Soorarai Pottru", 2020, listOf("Biography", "Drama"), "An entrepreneur fights to make air travel accessible.", language = "Tamil", director = "Sudha Kongara"),
            movie("Kantara", 2022, listOf("Action", "Drama", "Thriller"), "A village conflict becomes connected to ancient traditions.", language = "Kannada", director = "Rishab Shetty"),
            movie("RRR", 2022, listOf("Action", "Drama", "History"), "Two revolutionaries form an unlikely friendship in colonial India.", language = "Telugu", director = "S. S. Rajamouli")
        )

        val animeMovies = listOf(
            movie("Spirited Away", 2001, listOf("Anime", "Fantasy", "Adventure"), "A young girl enters a mysterious spirit world.", language = "Japanese", type = EntertainmentType.ANIME, director = "Hayao Miyazaki"),
            movie("Your Name", 2016, listOf("Anime", "Romance", "Fantasy"), "Two teenagers mysteriously begin to swap bodies.", language = "Japanese", type = EntertainmentType.ANIME, director = "Makoto Shinkai"),
            movie("A Silent Voice", 2016, listOf("Anime", "Drama"), "A former bully seeks redemption and friendship.", language = "Japanese", type = EntertainmentType.ANIME, director = "Naoko Yamada"),
            movie("Demon Slayer: Mugen Train", 2020, listOf("Anime", "Action", "Fantasy"), "Demon slayers board a train haunted by a powerful demon.", language = "Japanese", type = EntertainmentType.ANIME),
            movie("Suzume", 2022, listOf("Anime", "Adventure", "Fantasy"), "A teenager must close doors that unleash disasters.", language = "Japanese", type = EntertainmentType.ANIME, director = "Makoto Shinkai")
        )

        val worldwideMovies = listOf(
            movie("Parasite", 2019, listOf("Drama", "Thriller"), "Two families become entangled across a widening class divide.", language = "Korean", director = "Bong Joon-ho"),
            movie("Train to Busan", 2016, listOf("Action", "Horror", "Thriller"), "Passengers fight for survival during a zombie outbreak.", language = "Korean", director = "Yeon Sang-ho"),
            movie("The Intouchables", 2011, listOf("Comedy", "Drama"), "An unlikely friendship changes two lives.", language = "French"),
            movie("Pan's Labyrinth", 2006, listOf("Drama", "Fantasy", "War"), "A girl discovers a mythical labyrinth after the Spanish Civil War.", language = "Spanish", director = "Guillermo del Toro"),
            movie("City of God", 2002, listOf("Crime", "Drama"), "Two boys take different paths in Rio de Janeiro.", language = "Portuguese"),
            movie("Crouching Tiger, Hidden Dragon", 2000, listOf("Action", "Adventure", "Drama"), "A stolen sword connects warriors and forbidden love.", language = "Mandarin"),
            movie("Everything Everywhere All at Once", 2022, listOf("Action", "Adventure", "Comedy"), "A woman connects with parallel versions of her life.", language = "English")
        )

        val allTitles = (marvelMovies + hollywoodMovies + bollywoodMovies + southIndianMovies + animeMovies + worldwideMovies)
            .distinctBy { "${it.title.lowercase()}_${it.releaseYear}" }
    }
}

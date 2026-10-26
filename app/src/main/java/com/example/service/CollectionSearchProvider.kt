package com.example.service

import com.example.data.EntertainmentType

/**
 * Reliable browse results for broad collection terms.  These are only used
 * for category-style searches (for example, "Marvel movies"), where a title
 * index normally returns a studio page instead of a useful film listing.
 */
class CollectionSearchProvider : MovieSearchProvider {
    override val providerName: String = "CineVault collection guide"

    override suspend fun search(query: String): List<DiscoveredMovie> {
        val normalized = query.trim().lowercase().replace(Regex("\\s+"), " ")
        return when {
            normalized in marvelQueries -> marvelMovies
            normalized in hollywoodQueries -> hollywoodMovies
            else -> emptyList()
        }
    }

    override suspend fun getMovieDetails(identifier: String): DiscoveredMovie? =
        (marvelMovies + hollywoodMovies).firstOrNull { it.title.equals(identifier, ignoreCase = true) }

    private companion object {
        fun movie(title: String, year: Int, genres: List<String>, description: String) = DiscoveredMovie(
            title = title,
            releaseYear = year,
            releaseDate = year.toString(),
            genres = genres,
            synopsis = description,
            director = "See IMDb for full credits",
            language = "English",
            sourceUrl = "https://www.imdb.com/find?q=${java.net.URLEncoder.encode(title, "UTF-8")}",
            sourceName = "CineVault collection guide",
            contentType = EntertainmentType.MOVIE
        )

        val marvelQueries = setOf("marvel", "marvel movie", "marvel movies", "marvel films", "mcu", "mcu movies", "marvel cinematic universe")
        val hollywoodQueries = setOf("hollywood", "hollywood movie", "hollywood movies", "hollywood films", "english movies", "english movie")

        val marvelMovies = listOf(
            movie("Iron Man", 2008, listOf("Action", "Adventure", "Superhero"), "Tony Stark builds an armored suit and becomes Iron Man."),
            movie("The Avengers", 2012, listOf("Action", "Adventure", "Superhero"), "Earth's mightiest heroes unite to stop Loki."),
            movie("Guardians of the Galaxy", 2014, listOf("Action", "Adventure", "Sci-Fi"), "A band of intergalactic outlaws protects a powerful orb."),
            movie("Captain America: Civil War", 2016, listOf("Action", "Adventure", "Superhero"), "The Avengers divide over political oversight."),
            movie("Black Panther", 2018, listOf("Action", "Adventure", "Superhero"), "T'Challa returns to Wakanda to take his place as king."),
            movie("Avengers: Infinity War", 2018, listOf("Action", "Adventure", "Superhero"), "The Avengers confront Thanos and his Infinity Stones quest."),
            movie("Avengers: Endgame", 2019, listOf("Action", "Adventure", "Superhero"), "The remaining Avengers attempt to reverse a universe-changing loss."),
            movie("Spider-Man: No Way Home", 2021, listOf("Action", "Adventure", "Superhero"), "Spider-Man faces visitors from across the multiverse.")
        )

        val hollywoodMovies = listOf(
            movie("The Shawshank Redemption", 1994, listOf("Drama"), "A banker forms an enduring friendship while imprisoned."),
            movie("The Dark Knight", 2008, listOf("Action", "Crime", "Drama"), "Batman faces Gotham's most dangerous criminal."),
            movie("Inception", 2010, listOf("Action", "Sci-Fi", "Thriller"), "A thief enters dreams to plant an idea."),
            movie("Interstellar", 2014, listOf("Adventure", "Drama", "Sci-Fi"), "Explorers travel through a wormhole to seek humanity's future."),
            movie("Mad Max: Fury Road", 2015, listOf("Action", "Adventure", "Sci-Fi"), "Fugitives race across a post-apocalyptic wasteland."),
            movie("Parasite", 2019, listOf("Drama", "Thriller"), "Two families become entangled across a widening class divide."),
            movie("Everything Everywhere All at Once", 2022, listOf("Action", "Adventure", "Comedy"), "An unlikely hero connects with parallel versions of her life."),
            movie("Oppenheimer", 2023, listOf("Biography", "Drama", "History"), "The story of J. Robert Oppenheimer and the atomic bomb.")
        )
    }
}

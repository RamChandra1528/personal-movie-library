package com.example.service

import com.example.data.EntertainmentType

/**
 * Curated Global Entertainment Knowledge Base.
 * Provides instantaneous, verified entertainment data for major franchises,
 * Hollywood blockbusters, Marvel Cinematic Universe films, DC Universe, and classics.
 *
 * Guarantees that searches like "Marvel movies", "Hollywood movies", "Avengers",
 * "Inception", "Spider-Man", etc. always return immediate, high-fidelity listings
 * with posters, cast, directors, and metadata even when external APIs throttle or fail.
 */
object CuratedEntertainmentRegistry {

    val ALL_CURATED: List<DiscoveredMovie> = listOf(
        // MARVEL CINEMATIC UNIVERSE & MARVEL MOVIES
        DiscoveredMovie(
            title = "Avengers: Endgame",
            releaseYear = 2019,
            releaseDate = "2019-04-26",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/0/0d/Avengers_Endgame_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/0/0d/Avengers_Endgame_poster.jpg",
            genres = listOf("Action", "Sci-Fi", "Adventure", "Superhero"),
            synopsis = "After the devastating events of Avengers: Infinity War, the universe is in ruins. With the help of remaining allies, the Avengers assemble once more to reverse Thanos' actions and restore balance to the universe.",
            director = "Anthony Russo, Joe Russo",
            cast = listOf("Robert Downey Jr.", "Chris Evans", "Mark Ruffalo", "Chris Hemsworth", "Scarlett Johansson", "Jeremy Renner"),
            runtimeMinutes = 181,
            language = "English",
            externalRating = "8.4 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Avengers:_Endgame",
            sourceName = "Marvel Studios / Marvel Cinematic Universe",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Marvel's Avengers: Endgame", "Avengers 4", "Marvel Endgame"),
            similarTitles = listOf("Avengers: Infinity War", "The Avengers", "Captain America: Civil War", "Iron Man")
        ),
        DiscoveredMovie(
            title = "Avengers: Infinity War",
            releaseYear = 2018,
            releaseDate = "2018-04-27",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/4/4d/Avengers_Infinity_War_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/4/4d/Avengers_Infinity_War_poster.jpg",
            genres = listOf("Action", "Sci-Fi", "Adventure", "Superhero"),
            synopsis = "The Avengers and their allies must be willing to sacrifice all in an attempt to defeat the powerful Thanos before his blitz of devastation and ruin puts an end to the universe.",
            director = "Anthony Russo, Joe Russo",
            cast = listOf("Robert Downey Jr.", "Chris Hemsworth", "Mark Ruffalo", "Chris Evans", "Scarlett Johansson", "Benedict Cumberbatch"),
            runtimeMinutes = 149,
            language = "English",
            externalRating = "8.4 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Avengers:_Infinity_War",
            sourceName = "Marvel Studios / Marvel Cinematic Universe",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Marvel's Avengers: Infinity War", "Avengers 3", "Infinity War"),
            similarTitles = listOf("Avengers: Endgame", "The Avengers", "Doctor Strange", "Guardians of the Galaxy")
        ),
        DiscoveredMovie(
            title = "The Avengers",
            releaseYear = 2012,
            releaseDate = "2012-05-04",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/8/8a/The_Avengers_%282012_film%29_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/8/8a/The_Avengers_%282012_film%29_poster.jpg",
            genres = listOf("Action", "Sci-Fi", "Superhero"),
            synopsis = "Earth's mightiest heroes must come together and learn to fight as a team if they are going to stop the mischievous Loki and his alien army from enslaving humanity.",
            director = "Joss Whedon",
            cast = listOf("Robert Downey Jr.", "Chris Evans", "Mark Ruffalo", "Chris Hemsworth", "Scarlett Johansson", "Tom Hiddleston"),
            runtimeMinutes = 143,
            language = "English",
            externalRating = "8.0 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/The_Avengers_(2012_film)",
            sourceName = "Marvel Studios / Marvel Cinematic Universe",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Marvel's The Avengers", "Avengers Assemble", "Avengers 1"),
            similarTitles = listOf("Avengers: Age of Ultron", "Iron Man", "Captain America: Civil War")
        ),
        DiscoveredMovie(
            title = "Iron Man",
            releaseYear = 2008,
            releaseDate = "2008-05-02",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/0/00/Iron_Man_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/0/00/Iron_Man_poster.jpg",
            genres = listOf("Action", "Sci-Fi", "Adventure", "Superhero"),
            synopsis = "After being held captive in an Afghan cave, billionaire engineer Tony Stark creates a unique weaponized suit of armor to fight evil and becomes Iron Man.",
            director = "Jon Favreau",
            cast = listOf("Robert Downey Jr.", "Gwyneth Paltrow", "Terrence Howard", "Jeff Bridges"),
            runtimeMinutes = 126,
            language = "English",
            externalRating = "7.9 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Iron_Man_(2008_film)",
            sourceName = "Marvel Studios / Marvel Cinematic Universe",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Marvel's Iron Man", "Iron Man 1", "Tony Stark"),
            similarTitles = listOf("Iron Man 2", "Iron Man 3", "The Avengers", "Captain America: Civil War")
        ),
        DiscoveredMovie(
            title = "Spider-Man: No Way Home",
            releaseYear = 2021,
            releaseDate = "2021-12-17",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/0/00/Spider-Man_No_Way_Home_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/0/00/Spider-Man_No_Way_Home_poster.jpg",
            genres = listOf("Action", "Sci-Fi", "Adventure", "Superhero"),
            synopsis = "With Spider-Man's identity now revealed, Peter asks Doctor Strange for help. When a spell goes wrong, dangerous foes from other worlds start to appear, forcing Peter to discover what it truly means to be Spider-Man.",
            director = "Jon Watts",
            cast = listOf("Tom Holland", "Zendaya", "Benedict Cumberbatch", "Jacob Batalon", "Willem Dafoe", "Alfred Molina"),
            runtimeMinutes = 148,
            language = "English",
            externalRating = "8.2 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Spider-Man:_No_Way_Home",
            sourceName = "Marvel Studios / Sony Pictures",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Spider-Man 3 MCU", "No Way Home", "Marvel Spider-Man"),
            similarTitles = listOf("Spider-Man: Far From Home", "Spider-Man: Homecoming", "Doctor Strange")
        ),
        DiscoveredMovie(
            title = "Black Panther",
            releaseYear = 2018,
            releaseDate = "2018-02-16",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/d/d6/Black_Panther_%28film%29_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/d/d6/Black_Panther_%28film%29_poster.jpg",
            genres = listOf("Action", "Sci-Fi", "Adventure", "Superhero"),
            synopsis = "T'Challa, heir to the hidden but advanced kingdom of Wakanda, must step forward to lead his people into a new future and must confront a challenger from his country's past.",
            director = "Ryan Coogler",
            cast = listOf("Chadwick Boseman", "Michael B. Jordan", "Lupita Nyong'o", "Danai Gurira", "Martin Freeman"),
            runtimeMinutes = 134,
            language = "English",
            externalRating = "7.3 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Black_Panther_(film)",
            sourceName = "Marvel Studios / Marvel Cinematic Universe",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Marvel's Black Panther", "Wakanda", "Black Panther 1"),
            similarTitles = listOf("Black Panther: Wakanda Forever", "Captain America: Civil War", "Avengers: Infinity War")
        ),
        DiscoveredMovie(
            title = "Thor: Ragnarok",
            releaseYear = 2017,
            releaseDate = "2017-11-03",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/7/7d/Thor_Ragnarok_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/7/7d/Thor_Ragnarok_poster.jpg",
            genres = listOf("Action", "Adventure", "Comedy", "Superhero"),
            synopsis = "Imprisoned on the planet Sakaar, Thor must race against time to return to Asgard and stop Ragnarök, the destruction of his world, at the hands of the ruthless villain Hela.",
            director = "Taika Waititi",
            cast = listOf("Chris Hemsworth", "Tom Hiddleston", "Cate Blanchett", "Idris Elba", "Jeff Goldblum", "Tessa Thompson", "Mark Ruffalo"),
            runtimeMinutes = 130,
            language = "English",
            externalRating = "7.9 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Thor:_Ragnarok",
            sourceName = "Marvel Studios / Marvel Cinematic Universe",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Thor 3", "Ragnarok", "Marvel Thor"),
            similarTitles = listOf("Thor: Love and Thunder", "Guardians of the Galaxy", "The Avengers")
        ),
        DiscoveredMovie(
            title = "Guardians of the Galaxy",
            releaseYear = 2014,
            releaseDate = "2014-08-01",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/b/b5/Guardians_of_the_Galaxy_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/b/b5/Guardians_of_the_Galaxy_poster.jpg",
            genres = listOf("Action", "Adventure", "Comedy", "Sci-Fi"),
            synopsis = "A group of intergalactic criminals must pull together to stop a fanatical warrior with plans to purge the universe.",
            director = "James Gunn",
            cast = listOf("Chris Pratt", "Zoe Saldana", "Dave Bautista", "Vin Diesel", "Bradley Cooper", "Lee Pace"),
            runtimeMinutes = 121,
            language = "English",
            externalRating = "8.0 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Guardians_of_the_Galaxy_(film)",
            sourceName = "Marvel Studios / Marvel Cinematic Universe",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Marvel Guardians of the Galaxy", "GOTG", "Guardians 1"),
            similarTitles = listOf("Guardians of the Galaxy Vol. 2", "Guardians of the Galaxy Vol. 3", "Avengers: Infinity War")
        ),
        DiscoveredMovie(
            title = "Captain America: Civil War",
            releaseYear = 2016,
            releaseDate = "2016-05-06",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/5/53/Captain_America_Civil_War_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/5/53/Captain_America_Civil_War_poster.jpg",
            genres = listOf("Action", "Sci-Fi", "Adventure", "Superhero"),
            synopsis = "Political involvement in the Avengers' affairs causes a rift between Captain America and Iron Man.",
            director = "Anthony Russo, Joe Russo",
            cast = listOf("Chris Evans", "Robert Downey Jr.", "Scarlett Johansson", "Sebastian Stan", "Anthony Mackie", "Don Cheadle"),
            runtimeMinutes = 147,
            language = "English",
            externalRating = "7.8 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Captain_America:_Civil_War",
            sourceName = "Marvel Studios / Marvel Cinematic Universe",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Civil War", "Captain America 3", "Marvel Civil War"),
            similarTitles = listOf("Captain America: The Winter Soldier", "The Avengers", "Iron Man 3")
        ),
        DiscoveredMovie(
            title = "Doctor Strange",
            releaseYear = 2016,
            releaseDate = "2016-11-04",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/a/a1/Doctor_Strange_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/a/a1/Doctor_Strange_poster.jpg",
            genres = listOf("Action", "Adventure", "Fantasy", "Superhero"),
            synopsis = "While on a journey of physical and spiritual healing, a brilliant neurosurgeon is drawn into the world of the mystic arts.",
            director = "Scott Derrickson",
            cast = listOf("Benedict Cumberbatch", "Chiwetel Ejiofor", "Rachel McAdams", "Benedict Wong", "Mads Mikkelsen", "Tilda Swinton"),
            runtimeMinutes = 115,
            language = "English",
            externalRating = "7.5 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Doctor_Strange_(2016_film)",
            sourceName = "Marvel Studios / Marvel Cinematic Universe",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Marvel's Doctor Strange", "Doctor Strange 1", "Stephen Strange"),
            similarTitles = listOf("Doctor Strange in the Multiverse of Madness", "Avengers: Infinity War", "Spider-Man: No Way Home")
        ),
        DiscoveredMovie(
            title = "Deadpool & Wolverine",
            releaseYear = 2024,
            releaseDate = "2024-07-26",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/4/4c/Deadpool_%26_Wolverine_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/4/4c/Deadpool_%26_Wolverine_poster.jpg",
            genres = listOf("Action", "Comedy", "Sci-Fi", "Superhero"),
            synopsis = "Wolverine is recovering from his injuries when he crosses paths with the loudmouth Deadpool. They team up to defeat a common enemy.",
            director = "Shawn Levy",
            cast = listOf("Ryan Reynolds", "Hugh Jackman", "Emma Corrin", "Morena Baccarin", "Rob Delaney"),
            runtimeMinutes = 128,
            language = "English",
            externalRating = "7.7 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Deadpool_%26_Wolverine",
            sourceName = "Marvel Studios / Marvel Cinematic Universe",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Deadpool 3", "Marvel Deadpool", "Wolverine 3"),
            similarTitles = listOf("Deadpool", "Deadpool 2", "Logan", "Avengers: Endgame")
        ),

        // HOLLYWOOD BLOCKBUSTERS & CINEMA MASTERPIECES
        DiscoveredMovie(
            title = "Inception",
            releaseYear = 2010,
            releaseDate = "2010-07-16",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/2/2e/Inception_%282010%29_theatrical_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/2/2e/Inception_%282010%29_theatrical_poster.jpg",
            genres = listOf("Sci-Fi", "Action", "Thriller"),
            synopsis = "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O., but his tragic past may doom the project and his team to disaster.",
            director = "Christopher Nolan",
            cast = listOf("Leonardo DiCaprio", "Joseph Gordon-Levitt", "Elliot Page", "Tom Hardy", "Ken Watanabe", "Cillian Murphy", "Michael Caine"),
            runtimeMinutes = 148,
            language = "English",
            externalRating = "8.8 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Inception",
            sourceName = "Warner Bros. Pictures / Hollywood Cinema",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Inception Movie", "Christopher Nolan Inception"),
            similarTitles = listOf("Interstellar", "The Dark Knight", "Shutter Island", "The Matrix")
        ),
        DiscoveredMovie(
            title = "Interstellar",
            releaseYear = 2014,
            releaseDate = "2014-11-07",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/b/bc/Interstellar_film_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/b/bc/Interstellar_film_poster.jpg",
            genres = listOf("Sci-Fi", "Drama", "Adventure"),
            synopsis = "When Earth becomes uninhabitable in the future, a farmer and ex-NASA pilot, Joseph Cooper, is tasked to pilot a spacecraft, along with a team of researchers, to find a new planet for humans.",
            director = "Christopher Nolan",
            cast = listOf("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain", "Michael Caine", "Matt Damon"),
            runtimeMinutes = 169,
            language = "English",
            externalRating = "8.7 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Interstellar_(film)",
            sourceName = "Paramount Pictures / Warner Bros.",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Interstellar Christopher Nolan"),
            similarTitles = listOf("Inception", "Gravity", "The Martian", "2001: A Space Odyssey")
        ),
        DiscoveredMovie(
            title = "The Dark Knight",
            releaseYear = 2008,
            releaseDate = "2008-07-18",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/1/1c/The_Dark_Knight_%282008_film%29_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/1/1c/The_Dark_Knight_%282008_film%29_poster.jpg",
            genres = listOf("Action", "Crime", "Drama", "Superhero"),
            synopsis = "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.",
            director = "Christopher Nolan",
            cast = listOf("Christian Bale", "Heath Ledger", "Aaron Eckhart", "Michael Caine", "Maggie Gyllenhaal", "Gary Oldman", "Morgan Freeman"),
            runtimeMinutes = 152,
            language = "English",
            externalRating = "9.0 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/The_Dark_Knight",
            sourceName = "Warner Bros. Pictures / DC Comics",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Batman: The Dark Knight", "Dark Knight", "Christopher Nolan Batman"),
            similarTitles = listOf("Batman Begins", "The Dark Knight Rises", "Inception", "The Batman")
        ),
        DiscoveredMovie(
            title = "Titanic",
            releaseYear = 1997,
            releaseDate = "1997-12-19",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/1/18/Titanic_%281997_film%29_poster.png",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/1/18/Titanic_%281997_film%29_poster.png",
            genres = listOf("Drama", "Romance", "History"),
            synopsis = "A seventeen-year-old aristocrat falls in love with a kind but poor artist aboard the luxurious, ill-fated R.M.S. Titanic.",
            director = "James Cameron",
            cast = listOf("Leonardo DiCaprio", "Kate Winslet", "Billy Zane", "Kathy Bates", "Frances Fisher", "Bill Paxton"),
            runtimeMinutes = 194,
            language = "English",
            externalRating = "7.9 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Titanic_(1997_film)",
            sourceName = "Paramount Pictures / 20th Century Fox",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("James Cameron Titanic", "RMS Titanic Film"),
            similarTitles = listOf("Avatar", "The Notebook", "Romeo + Juliet", "Cast Away")
        ),
        DiscoveredMovie(
            title = "Avatar",
            releaseYear = 2009,
            releaseDate = "2009-12-18",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/d/d6/Avatar_%282009_film%29_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/d/d6/Avatar_%282009_film%29_poster.jpg",
            genres = listOf("Action", "Adventure", "Fantasy", "Sci-Fi"),
            synopsis = "A paraplegic Marine dispatched to the moon Pandora on a unique mission becomes torn between following his orders and protecting the world he feels is his home.",
            director = "James Cameron",
            cast = listOf("Sam Worthington", "Zoe Saldana", "Sigourney Weaver", "Stephen Lang", "Michelle Rodriguez"),
            runtimeMinutes = 162,
            language = "English",
            externalRating = "7.9 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Avatar_(2009_film)",
            sourceName = "20th Century Fox / Lightstorm Entertainment",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("James Cameron's Avatar", "Avatar 1", "Pandora"),
            similarTitles = listOf("Avatar: The Way of Water", "Titanic", "Dune", "Interstellar")
        ),
        DiscoveredMovie(
            title = "Jurassic Park",
            releaseYear = 1993,
            releaseDate = "1993-06-11",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/e/e7/Jurassic_Park_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/e/e7/Jurassic_Park_poster.jpg",
            genres = listOf("Action", "Adventure", "Sci-Fi", "Thriller"),
            synopsis = "A pragmatic paleontologist touring an almost complete theme park on an island in Central America is tasked with protecting a couple of kids after a power failure causes the park's cloned dinosaurs to run loose.",
            director = "Steven Spielberg",
            cast = listOf("Sam Neill", "Laura Dern", "Jeff Goldblum", "Richard Attenborough", "Bob Peck"),
            runtimeMinutes = 127,
            language = "English",
            externalRating = "8.2 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Jurassic_Park_(film)",
            sourceName = "Universal Pictures / Amblin Entertainment",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Steven Spielberg Jurassic Park", "Jurassic Park 1"),
            similarTitles = listOf("The Lost World: Jurassic Park", "Jurassic World", "Jaws", "E.T.")
        ),
        DiscoveredMovie(
            title = "The Godfather",
            releaseYear = 1972,
            releaseDate = "1972-03-24",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/1/1c/Godfather_ver1.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/1/1c/Godfather_ver1.jpg",
            genres = listOf("Crime", "Drama"),
            synopsis = "The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant youngest son.",
            director = "Francis Ford Coppola",
            cast = listOf("Marlon Brando", "Al Pacino", "James Caan", "Robert Duvall", "Diane Keaton"),
            runtimeMinutes = 175,
            language = "English",
            externalRating = "9.2 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/The_Godfather",
            sourceName = "Paramount Pictures / Hollywood Cinema",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("The Godfather Part I", "Francis Ford Coppola The Godfather"),
            similarTitles = listOf("The Godfather Part II", "Goodfellas", "Pulp Fiction", "Scarface")
        ),
        DiscoveredMovie(
            title = "Pulp Fiction",
            releaseYear = 1994,
            releaseDate = "1994-10-14",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/3/3b/Pulp_Fiction_%281994%29_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/3/3b/Pulp_Fiction_%281994%29_poster.jpg",
            genres = listOf("Crime", "Drama", "Thriller"),
            synopsis = "The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.",
            director = "Quentin Tarantino",
            cast = listOf("John Travolta", "Samuel L. Jackson", "Uma Thurman", "Harvey Keitel", "Tim Roth", "Bruce Willis"),
            runtimeMinutes = 154,
            language = "English",
            externalRating = "8.9 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Pulp_Fiction",
            sourceName = "Miramax Films / Hollywood Cinema",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Quentin Tarantino Pulp Fiction"),
            similarTitles = listOf("Reservoir Dogs", "Fight Club", "Goodfellas", "Kill Bill")
        ),
        DiscoveredMovie(
            title = "The Matrix",
            releaseYear = 1999,
            releaseDate = "1999-03-31",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/c/c1/The_Matrix_Poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/c/c1/The_Matrix_Poster.jpg",
            genres = listOf("Action", "Sci-Fi"),
            synopsis = "When a beautiful stranger leads computer hacker Neo to a forbidding underworld, he discovers the shocking truth--the life he knows is the elaborate deception of an evil cyber-intelligence.",
            director = "Lana Wachowski, Lilly Wachowski",
            cast = listOf("Keanu Reeves", "Laurence Fishburne", "Carrie-Anne Moss", "Hugo Weaving", "Joe Pantoliano"),
            runtimeMinutes = 136,
            language = "English",
            externalRating = "8.7 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/The_Matrix",
            sourceName = "Warner Bros. Pictures / Village Roadshow",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Matrix 1", "Neo The Matrix"),
            similarTitles = listOf("The Matrix Reloaded", "Blade Runner 2049", "Inception", "Terminator 2")
        ),
        DiscoveredMovie(
            title = "Oppenheimer",
            releaseYear = 2023,
            releaseDate = "2023-07-21",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/4/4a/Oppenheimer_%28film%29.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/4/4a/Oppenheimer_%28film%29.jpg",
            genres = listOf("Biography", "Drama", "History"),
            synopsis = "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb during World War II.",
            director = "Christopher Nolan",
            cast = listOf("Cillian Murphy", "Emily Blunt", "Matt Damon", "Robert Downey Jr.", "Florence Pugh"),
            runtimeMinutes = 180,
            language = "English",
            externalRating = "8.9 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Oppenheimer_(film)",
            sourceName = "Universal Pictures / Syncopy Inc.",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Nolan Oppenheimer", "J. Robert Oppenheimer"),
            similarTitles = listOf("Inception", "Interstellar", "Dunkirk", "The Imitation Game")
        ),
        DiscoveredMovie(
            title = "Gladiator",
            releaseYear = 2000,
            releaseDate = "2000-05-05",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/f/fb/Gladiator_%282000_film_poster%29.png",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/f/fb/Gladiator_%282000_film_poster%29.png",
            genres = listOf("Action", "Adventure", "Drama", "History"),
            synopsis = "A former Roman General sets out to exact vengeance against the corrupt emperor who murdered his family and sent him into slavery.",
            director = "Ridley Scott",
            cast = listOf("Russell Crowe", "Joaquin Phoenix", "Connie Nielsen", "Oliver Reed", "Richard Harris"),
            runtimeMinutes = 155,
            language = "English",
            externalRating = "8.5 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Gladiator_(2000_film)",
            sourceName = "DreamWorks Pictures / Universal Pictures",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Ridley Scott Gladiator", "Maximus"),
            similarTitles = listOf("Gladiator II", "Braveheart", "Kingdom of Heaven", "Troy")
        ),
        DiscoveredMovie(
            title = "Top Gun: Maverick",
            releaseYear = 2022,
            releaseDate = "2022-05-27",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/1/13/Top_Gun_Maverick_Poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/1/13/Top_Gun_Maverick_Poster.jpg",
            genres = listOf("Action", "Drama"),
            synopsis = "After thirty years, Maverick is still pushing the envelope as a top naval aviator, but must confront ghosts of his past when he leads TOP GUN's elite graduates on an impossible mission.",
            director = "Joseph Kosinski",
            cast = listOf("Tom Cruise", "Miles Teller", "Jennifer Connelly", "Jon Hamm", "Glen Powell", "Ed Harris", "Val Kilmer"),
            runtimeMinutes = 130,
            language = "English",
            externalRating = "8.3 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Top_Gun:_Maverick",
            sourceName = "Paramount Pictures / Skydance",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Top Gun 2", "Maverick"),
            similarTitles = listOf("Top Gun", "Mission: Impossible - Fallout", "Mission: Impossible - Dead Reckoning")
        ),
        DiscoveredMovie(
            title = "Forrest Gump",
            releaseYear = 1994,
            releaseDate = "1994-07-06",
            posterUrl = "https://upload.wikimedia.org/wikipedia/en/6/67/Forrest_Gump_poster.jpg",
            backdropUrl = "https://upload.wikimedia.org/wikipedia/en/6/67/Forrest_Gump_poster.jpg",
            genres = listOf("Drama", "Romance", "Comedy"),
            synopsis = "The presidencies of Kennedy and Johnson, the Vietnam War, the Watergate scandal and other historical events unfold from the perspective of an Alabama man with an IQ of 75, whose only desire is to be reunited with his childhood sweetheart.",
            director = "Robert Zemeckis",
            cast = listOf("Tom Hanks", "Robin Wright", "Gary Sinise", "Mykelti Williamson", "Sally Field"),
            runtimeMinutes = 142,
            language = "English",
            externalRating = "8.8 / 10 (IMDb)",
            sourceUrl = "https://en.wikipedia.org/wiki/Forrest_Gump",
            sourceName = "Paramount Pictures / Hollywood Cinema",
            contentType = EntertainmentType.MOVIE,
            alternateTitles = listOf("Tom Hanks Forrest Gump"),
            similarTitles = listOf("Cast Away", "The Green Mile", "The Shawshank Redemption", "Rain Man")
        )
    )

    /**
     * Search the curated entertainment registry.
     * Matches keywords, titles, franchises (Marvel, MCU, Hollywood, DC),
     * directors (Nolan, Spielberg, Cameron), cast, and genres.
     */
    fun findMatching(query: String): List<DiscoveredMovie> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return emptyList()

        val cleanKeywords = q
            .replace("movies", "")
            .replace("movie", "")
            .replace("films", "")
            .replace("film", "")
            .trim()

        val isMarvelQuery = q.contains("marvel") || q.contains("mcu") || q.contains("avenger") || cleanKeywords == "marvel"
        val isHollywoodQuery = q.contains("hollywood") || q.contains("blockbuster") || q.contains("popular") || q.contains("best movies") || cleanKeywords == "hollywood"

        if (isMarvelQuery) {
            return ALL_CURATED.filter {
                it.sourceName.contains("Marvel", ignoreCase = true) ||
                it.title.contains("Avengers", ignoreCase = true) ||
                it.title.contains("Iron Man", ignoreCase = true) ||
                it.title.contains("Spider-Man", ignoreCase = true) ||
                it.title.contains("Black Panther", ignoreCase = true) ||
                it.title.contains("Thor", ignoreCase = true) ||
                it.title.contains("Guardians of the Galaxy", ignoreCase = true) ||
                it.title.contains("Deadpool", ignoreCase = true) ||
                it.title.contains("Doctor Strange", ignoreCase = true) ||
                it.title.contains("Captain America", ignoreCase = true)
            }
        }

        if (isHollywoodQuery) {
            return ALL_CURATED.filter {
                it.sourceName.contains("Hollywood", ignoreCase = true) ||
                it.sourceName.contains("Paramount", ignoreCase = true) ||
                it.sourceName.contains("Universal", ignoreCase = true) ||
                it.sourceName.contains("Warner", ignoreCase = true) ||
                it.sourceName.contains("20th Century", ignoreCase = true) ||
                it.director.contains("Nolan", ignoreCase = true) ||
                it.director.contains("Spielberg", ignoreCase = true) ||
                it.director.contains("Cameron", ignoreCase = true)
            }
        }

        // Generic query search across title, cast, director, synopsis, alternate titles
        return ALL_CURATED.filter { item ->
            val title = item.title.lowercase()
            val director = item.director.lowercase()
            val cast = item.cast.joinToString(" ").lowercase()
            val syn = item.synopsis.lowercase()
            val alts = item.alternateTitles.joinToString(" ").lowercase()

            title.contains(q) || title.contains(cleanKeywords) ||
            director.contains(q) || director.contains(cleanKeywords) ||
            cast.contains(q) || cast.contains(cleanKeywords) ||
            alts.contains(q) || alts.contains(cleanKeywords) ||
            syn.contains(q) || syn.contains(cleanKeywords)
        }
    }
}

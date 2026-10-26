# CineVault - Personal Movie Collection & Global Entertainment Search Engine

CineVault is a personal movie archive, collection manager, and real-time global entertainment search engine built for Android using Jetpack Compose, Kotlin, Room Database, and Material Design 3.

---

## Core Capabilities

### 1. Real-Time Global Entertainment Search Engine
- **Universal Entertainment Coverage**: Queries movies, web series, TV shows, anime, and dramas across the globe.
- **Fuzzy Name & Typo Tolerance**: Uses a multi-tier matching algorithm (Levenshtein edit distance, prefix matching, and token overlap) to discover entertainment even with spelling mistakes, truncated titles, or alternate names (e.g. typing `"interstel"` matches *Interstellar*, `"dark nite"` matches *The Dark Knight*).
- **Spelling Correction ("Did You Mean?")**: Dynamically presents verified Did-You-Mean suggestions powered by Wikipedia's OpenSearch index.
- **Search by Actor, Director, Genre & Keyword**: Searches across cast members (e.g. *Leonardo DiCaprio*, *Keanu Reeves*), directors (e.g. *Christopher Nolan*), genres (e.g. *Cyberpunk*, *Sci-Fi*, *Anime*), and synopsis themes.
- **Year Showcase with Pagination**: When entering a 4-digit year (e.g. `2024`, `1999`, `1994`), CineVault transitions into Year Showcase mode, retrieving movies, TV shows, and anime released that year with paginated navigation (`Previous Page` / `Next Page`).
- **Alternate & International Titles**: Displays romanized Japanese anime titles and international localized names.
- **Similar Titles & Recommendations**: Every title recommends 3 to 4 related or similar works with 1-tap jump search.
- **Direct Portal Integration**: One-tap deep links to **Google Search**, **IMDb**, **TMDB**, and **Wikipedia** for instant verification.

### 2. Zero-API-Key Public Search Architecture
CineVault does **not** require any user-configured or paid API key:
- **Wikipedia REST & Action APIs**: `https://en.wikipedia.org/w/api.php` and `https://en.wikipedia.org/api/rest_v1/page/summary/...`
- **TVMaze Global Registry**: Open TV series, web series, British & Asian dramas API `https://api.tvmaze.com/search/shows?q=...`
- **Jikan Open API**: Free, open REST API for Japanese anime, anime movies, and OVAs `https://api.jikan.moe/v4/anime`
- **In-Memory Caching & Timeout Protection**: Requests are cached in memory with a 14-second timeout to ensure fast, responsive lookups.

### 3. Collection Management & Modern Dashboard
- **Quick Save**: Enter a title and tap **Save** for immediate discovery and saving.
- **Poster Grid**: Adaptive cards displaying posters, release years, watch status pills, and 5-star ratings.
- **Real-Time Collection Filtering**: Instant search within your saved vault, status filter chips (*Completed, Watching, Plan to Watch, Dropped, Favorites*), and dynamic genre tags.
- **Sorting Options**: Sort by Recently Added, Release Year (Newest/Oldest), Personal Rating, or Alphabetical (A-Z).
- **Movie Details Screen**: Cinematic backdrop, interactive 5-star personal rating, watch status selector, watched date picker, personal notes/review editor, full synopsis, cast/crew chips, similar titles, and portal verification buttons.
- **Vault Statistics**: Total watch time in hours and days, completion meters, top genres distribution, and average rating.

---

## Architecture & Technology Stack

- **UI Framework**: Jetpack Compose, Material Design 3 (M3), Coil 2.7 (Image loading & disk caching).
- **Architecture**: Clean MVVM (Model-View-ViewModel) with unidirectional data flow and Kotlin Coroutines & Flow.
- **Data Persistence**: Android Room Database (SQLite engine with reactive Flow queries and schema versioning).
- **Service Layer**:
  - `MovieSearchProvider` interface (provider-independent architecture)
  - `WikipediaSearchProvider`: OpenSearch suggestions, category search by year with `sroffset` pagination, metadata and posters.
  - `TvMazeSearchProvider`: Global TV series, web series, and dramas.
  - `AnimeSearchProvider`: Jikan REST API for Japanese anime titles, studios, and scores.
  - `FuzzyRelevanceRanker`: Levenshtein distance, multi-factor scoring (title, alternate titles, cast, director, year, genre).
  - `MovieSearchService`: Coordinates providers, caches responses, applies relevance ranking, and handles pagination.

---

## Windows Setup Instructions

To build and run CineVault on Windows:

### 1. Prerequisites
- **Operating System**: Windows 10 or Windows 11 (64-bit).
- **Java Development Kit**: JDK 17 or JDK 21 (Temurin, Azul, or Oracle OpenJDK).
  - Verify in Command Prompt: `java -version`
  - Ensure `JAVA_HOME` environment variable is set to your JDK path.
- **Android Studio**: Android Studio Ladybug (2024.2+) or Meerkat.
  - Includes Android SDK Platform 34/35/36 and Android SDK Build-Tools.
  - Ensure `ANDROID_HOME` or `ANDROID_SDK_ROOT` is set to `%LOCALAPPDATA%\Android\Sdk`.

### 2. Opening & Syncing the Project
1. Extract or clone this repository to a local directory (e.g. `C:\Projects\CineVault`).
2. Launch **Android Studio**.
3. Select **Open** and choose the project root directory.
4. Android Studio will automatically sync the Gradle configuration.

### 3. Running on Emulator or Physical Device
1. In Android Studio, open the **Device Manager** (`Tools > Device Manager`).
2. Create or launch an Android Virtual Device (AVD) running Android 10+ (API level 29 to 35).
3. Click the green **Run 'app'** button (or press `Shift + F10`).

### 4. Command Line Build (Windows PowerShell / CMD)
```powershell
# Navigate to the project folder
cd C:\Projects\CineVault

# Run unit and Robolectric tests
gradle :app:testDebugUnitTest

# Assemble debug APK
gradle :app:assembleDebug
```
The compiled APK will be located at:
`app\build\outputs\apk\debug\app-debug.apk`

---

## Testing

Run local JVM unit and Robolectric tests:
```bash
gradle :app:testDebugUnitTest
```
Tests cover:
- `CineVaultUnitTest`: Fuzzy relevance scoring, Levenshtein edit distance, prefix matching, alternate titles, actor matching, year matching, `EntertainmentType` formatting, and `DiscoveredMovie` to `MovieEntity` transformations.
- `ExampleRobolectricTest`: Android resource string loading and application context validation.

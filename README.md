# CineVault - Personal Movie Collection & Vault

CineVault is a personal movie archive and collection manager built for Android using Jetpack Compose, Kotlin, Room Database, and Material Design 3.

## Core Features & Workflow

1. **One-Tap Public Discovery & Save**:
   - The user enters a movie title (e.g. *Interstellar*, *Inception*, *Spirited Away*) and clicks **Save**.
   - The app automatically queries public, open film metadata registries (including Wikipedia & Wikimedia Commons and open film indexes) without requiring any paid or private API key (no TMDB, OMDB, or Google Custom Search API key needed).
   - Automatically extracts and verifies release year, high-resolution poster artwork, director, cast, genres, runtime, synopsis, and source attribution.
   - Saves directly to the local persistent database and displays in your collection.

2. **Verification & Discovered Results Picker**:
   - When multiple public matches exist, CineVault presents an interactive preview modal showing candidate posters, release years, and synopsis excerpts so the user can verify rather than inventing details.
   - Includes browser fallback links to open public searches directly if automated network access is blocked or restricted.

3. **Collection Management & Modern Dashboard**:
   - **Poster Grid**: Adaptive responsive cards featuring movie posters, release year badges, watch status pills, and personal star ratings.
   - **Real-Time Search & Filtering**: Instant search across titles, directors, cast, and genres. Filter by Watch Status (*Completed, Watching, Plan to Watch, Dropped, Favorites*) and dynamic Genre chips.
   - **Sorting**: Order by Recently Added, Release Year (Newest/Oldest), Personal Rating, or Alphabetical (A-Z).
   - **Interactive Details Screen**: Full cinematic backdrop, full overview/synopsis, director & cast chips, 5-star rating selector, watch status dropdown, watched date recorder, personal review/notes editor, and verified public page link.
   - **Collection Telemetry & Stats**: Total watch time calculated in hours and days, completion breakdown with visual progress meters, top genres distribution, and average rating.

---

## Architecture & Technology Stack

- **UI Layer**: Jetpack Compose, Material Design 3 (M3), Coil 2.7 (Asynchronous image loading & disk caching).
- **Architecture**: Clean MVVM (Model-View-ViewModel) with unidirectional data flow and Kotlin Coroutines & Flow.
- **Data Persistence**: Android Room Database (SQLite engine with reactive Flow queries and schema validation).
- **Public Search Service**: Provider-independent search architecture (`MovieSearchProvider`, `WikipediaSearchProvider`, `TvMazeSearchProvider`, `MovieSearchService`) featuring in-memory LRU caching, request timeout controls, duplicate detection, and fallback link generators.
- **Design & Branding**: Custom CineVault logo combining an artistic film frame with a vault safe motif, dark obsidian background (`#0D1117`), gold (`#F5B041`) and cyan (`#00E5FF`) accents.

---

## Windows Setup Instructions

To build and run CineVault on Windows:

### 1. Prerequisites
- **Operating System**: Windows 10 or Windows 11 (64-bit).
- **Java Development Kit**: JDK 17 or JDK 21 (Temurin, Azul, or Oracle OpenJDK).
  - Verify installation in Command Prompt: `java -version`
  - Ensure `JAVA_HOME` environment variable is set to your JDK path (e.g., `C:\Program Files\Eclipse Adoptium\jdk-17...`).
- **Android Studio**: Android Studio Ladybug (2024.2+) or Meerkat.
  - Includes Android SDK Platform 34/35/36 and Android SDK Build-Tools.
  - Ensure `ANDROID_HOME` or `ANDROID_SDK_ROOT` is set to `%LOCALAPPDATA%\Android\Sdk`.

### 2. Opening & Syncing the Project
1. Extract or clone this repository to a local directory (e.g. `C:\Projects\CineVault`).
2. Launch **Android Studio**.
3. Select **Open** and navigate to the project root directory.
4. Android Studio will automatically recognize Gradle and start the Gradle Sync.
5. If prompted by Windows Defender or Firewall, allow Android Studio and Java network access so public metadata APIs can be contacted.

### 3. Running on Emulator or Physical Device
1. In Android Studio, open the **Device Manager** (`Tools > Device Manager`).
2. Create or launch an Android Virtual Device (AVD) running Android 10+ (API level 29 to 35).
3. Click the green **Run 'app'** button (or press `Shift + F10`).
4. The application will compile, install, and launch CineVault.

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
- `CineVaultUnitTest`: Runtime formatting, genre parsing, `DiscoveredMovie` to `MovieEntity` mapping, watch status display rules.
- `ExampleRobolectricTest`: Android resource string loading and application context validation.

---

## Public Search & Zero-API-Key Policy

CineVault does **not** require any API key or subscription. It uses open, publicly accessible APIs and structured knowledge endpoints with explicit User-Agent attribution:
- **Wikipedia REST & Action APIs**: `https://en.wikipedia.org/api/rest_v1/page/summary/...`
- **TVMaze Public Registry**: `https://api.tvmaze.com/search/shows?q=...`
- **Attribution**: Every discovered movie retains its public source link and attribution details.

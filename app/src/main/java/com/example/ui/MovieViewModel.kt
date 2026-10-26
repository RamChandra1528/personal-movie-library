package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MovieEntity
import com.example.data.MovieRepository
import com.example.data.WatchStatus
import com.example.service.DiscoveredMovie
import com.example.service.GlobalSearchResponse
import com.example.service.MovieSearchService
import com.example.service.SearchResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOrder(val label: String) {
    RECENTLY_ADDED("Recently Added"),
    TITLE_ASC("Title (A-Z)"),
    TITLE_DESC("Title (Z-A)"),
    YEAR_DESC("Year (Newest)"),
    YEAR_ASC("Year (Oldest)"),
    RATING_DESC("Personal Rating")
}

enum class Screen {
    DASHBOARD,
    GLOBAL_SEARCH,
    MOVIE_DETAILS,
    STATS
}

data class CollectionStats(
    val totalMovies: Int = 0,
    val completedCount: Int = 0,
    val watchingCount: Int = 0,
    val planToWatchCount: Int = 0,
    val droppedCount: Int = 0,
    val favoritesCount: Int = 0,
    val totalRuntimeMinutes: Int = 0,
    val averageRating: Float = 0f,
    val topGenres: List<Pair<String, Int>> = emptyList()
)

class MovieViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MovieRepository
    private val searchService: MovieSearchService = MovieSearchService()

    init {
        val db = AppDatabase.getInstance(application)
        repository = MovieRepository(db.movieDao())
    }

    val rawMovies: StateFlow<List<MovieEntity>> = repository.allMovies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state filters for personal collection
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>("ALL")
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    private val _genreFilter = MutableStateFlow<String?>("ALL")
    val genreFilter: StateFlow<String?> = _genreFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.RECENTLY_ADDED)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // Navigation state
    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedMovieId = MutableStateFlow<Long?>(null)
    val selectedMovieId: StateFlow<Long?> = _selectedMovieId.asStateFlow()

    // Quick Search / Add dialog state
    private val _isSearchingWeb = MutableStateFlow(false)
    val isSearchingWeb: StateFlow<Boolean> = _isSearchingWeb.asStateFlow()

    private val _candidateResults = MutableStateFlow<List<DiscoveredMovie>>(emptyList())
    val candidateResults: StateFlow<List<DiscoveredMovie>> = _candidateResults.asStateFlow()

    private val _webSearchError = MutableStateFlow<String?>(null)
    val webSearchError: StateFlow<String?> = _webSearchError.asStateFlow()

    private val _fallbackWebUrl = MutableStateFlow<String?>(null)
    val fallbackWebUrl: StateFlow<String?> = _fallbackWebUrl.asStateFlow()

    private val _quickAddNotification = MutableStateFlow<String?>(null)
    val quickAddNotification: StateFlow<String?> = _quickAddNotification.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    // Real-Time Global Entertainment Search Engine State
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    private val _globalEntertainmentType = MutableStateFlow<String?>("ALL")
    val globalEntertainmentType: StateFlow<String?> = _globalEntertainmentType.asStateFlow()

    private val _globalSearchPage = MutableStateFlow(1)
    val globalSearchPage: StateFlow<Int> = _globalSearchPage.asStateFlow()

    private val _globalSearchResponse = MutableStateFlow<GlobalSearchResponse?>(null)
    val globalSearchResponse: StateFlow<GlobalSearchResponse?> = _globalSearchResponse.asStateFlow()

    private val _isGlobalSearching = MutableStateFlow(false)
    val isGlobalSearching: StateFlow<Boolean> = _isGlobalSearching.asStateFlow()

    private var debounceJob: Job? = null

    // Filtered & Sorted Movie List in Personal Vault
    val displayedMovies: StateFlow<List<MovieEntity>> = combine(
        rawMovies,
        _searchQuery,
        _statusFilter,
        _genreFilter,
        _sortOrder
    ) { movies, query, status, genre, sort ->
        var list = movies

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                        it.director.lowercase().contains(q) ||
                        it.cast.lowercase().contains(q) ||
                        it.genres.lowercase().contains(q) ||
                        it.alternateTitles.lowercase().contains(q)
            }
        }

        if (status != null && status != "ALL") {
            if (status == "FAVORITES") {
                list = list.filter { it.isFavorite }
            } else {
                list = list.filter { it.watchStatus == status }
            }
        }

        if (genre != null && genre != "ALL") {
            list = list.filter {
                it.genresList.any { g -> g.equals(genre, ignoreCase = true) }
            }
        }

        when (sort) {
            SortOrder.RECENTLY_ADDED -> list.sortedByDescending { it.addedTimestamp }
            SortOrder.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
            SortOrder.TITLE_DESC -> list.sortedByDescending { it.title.lowercase() }
            SortOrder.YEAR_DESC -> list.sortedByDescending { it.releaseYear ?: 0 }
            SortOrder.YEAR_ASC -> list.sortedBy { it.releaseYear ?: 9999 }
            SortOrder.RATING_DESC -> list.sortedByDescending { it.personalRating }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Collection Statistics
    val stats: StateFlow<CollectionStats> = rawMovies.combine(_searchQuery) { movies, _ ->
        val total = movies.size
        val completed = movies.count { it.watchStatus == WatchStatus.COMPLETED }
        val watching = movies.count { it.watchStatus == WatchStatus.WATCHING }
        val plan = movies.count { it.watchStatus == WatchStatus.PLAN_TO_WATCH }
        val dropped = movies.count { it.watchStatus == WatchStatus.DROPPED }
        val favs = movies.count { it.isFavorite }

        val totalMinutes = movies.mapNotNull { it.runtimeMinutes }.sum()

        val ratedMovies = movies.filter { it.personalRating > 0f }
        val avgRating = if (ratedMovies.isNotEmpty()) {
            ratedMovies.map { it.personalRating }.average().toFloat()
        } else {
            0f
        }

        val genreCounts = mutableMapOf<String, Int>()
        movies.flatMap { it.genresList }.forEach { g ->
            val clean = g.trim()
            if (clean.isNotEmpty()) {
                genreCounts[clean] = (genreCounts[clean] ?: 0) + 1
            }
        }
        val topGenres = genreCounts.toList().sortedByDescending { it.second }.take(5)

        CollectionStats(
            totalMovies = total,
            completedCount = completed,
            watchingCount = watching,
            planToWatchCount = plan,
            droppedCount = dropped,
            favoritesCount = favs,
            totalRuntimeMinutes = totalMinutes,
            averageRating = avgRating,
            topGenres = topGenres
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CollectionStats())

    // All distinct genres across saved collection
    val availableGenres: StateFlow<List<String>> = rawMovies.combine(_searchQuery) { movies, _ ->
        val set = sortedSetOf<String>()
        movies.forEach { m ->
            m.genresList.forEach { g ->
                if (g.isNotBlank()) set.add(g)
            }
        }
        set.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Movie for details screen
    val selectedMovie: StateFlow<MovieEntity?> = combine(
        rawMovies,
        _selectedMovieId
    ) { movies, id ->
        if (id == null) null else movies.firstOrNull { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Collection actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: String?) {
        _statusFilter.value = status
    }

    fun setGenreFilter(genre: String?) {
        _genreFilter.value = genre
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun openMovieDetails(movieId: Long) {
        _selectedMovieId.value = movieId
        _currentScreen.value = Screen.MOVIE_DETAILS
    }

    fun openAddDialog() {
        _candidateResults.value = emptyList()
        _webSearchError.value = null
        _fallbackWebUrl.value = null
        _showAddDialog.value = true
    }

    fun closeAddDialog() {
        _showAddDialog.value = false
        _candidateResults.value = emptyList()
        _webSearchError.value = null
    }

    fun openEditDialog() {
        _showEditDialog.value = true
    }

    fun closeEditDialog() {
        _showEditDialog.value = false
    }

    fun clearNotification() {
        _quickAddNotification.value = null
    }

    /**
     * Primary One-Tap Quick Save / Auto-Discover workflow.
     */
    fun quickSaveOrSearch(title: String, autoSaveIfDefinite: Boolean = true) {
        val clean = title.trim()
        if (clean.isBlank()) return

        viewModelScope.launch {
            _isSearchingWeb.value = true
            _webSearchError.value = null
            _fallbackWebUrl.value = null
            _candidateResults.value = emptyList()

            when (val result = searchService.searchMovies(clean)) {
                is SearchResult.Success -> {
                    val matches = result.matches
                    if (matches.isEmpty()) {
                        _webSearchError.value = "No verified public information found for \"$clean\"."
                        _fallbackWebUrl.value = searchService.getBrowserSearchUrl(clean)
                    } else if (autoSaveIfDefinite && matches.size == 1) {
                        val movie = matches.first()
                        repository.insertMovie(movie.toMovieEntity())
                        _quickAddNotification.value = "Added \"${movie.title}\" (${movie.releaseYear ?: "Film"}) to CineVault!"
                        closeAddDialog()
                    } else if (autoSaveIfDefinite && matches.isNotEmpty() && matches.first().title.equals(clean, ignoreCase = true) && matches.first().posterUrl != null) {
                        val topMatch = matches.first()
                        repository.insertMovie(topMatch.toMovieEntity())
                        _quickAddNotification.value = "Added \"${topMatch.title}\" (${topMatch.releaseYear ?: "Film"}) to CineVault!"
                        closeAddDialog()
                    } else {
                        _candidateResults.value = matches
                        _showAddDialog.value = true
                    }
                }
                is SearchResult.Empty -> {
                    _webSearchError.value = "No public records found for \"$clean\". You can add details manually or search the public web."
                    _fallbackWebUrl.value = result.webSearchUrl
                    _showAddDialog.value = true
                }
                is SearchResult.Error -> {
                    _webSearchError.value = result.message
                    _fallbackWebUrl.value = result.webSearchUrl
                    _showAddDialog.value = true
                }
            }
            _isSearchingWeb.value = false
        }
    }

    // REAL-TIME GLOBAL ENTERTAINMENT SEARCH OPERATIONS
    fun onGlobalQueryChanged(query: String) {
        _globalSearchQuery.value = query
        _globalSearchPage.value = 1
        debounceJob?.cancel()

        if (query.trim().isEmpty()) {
            _globalSearchResponse.value = null
            _isGlobalSearching.value = false
            return
        }

        debounceJob = viewModelScope.launch {
            delay(400) // 400ms debounce
            executeGlobalSearch(query.trim(), page = 1)
        }
    }

    fun executeGlobalSearch(query: String, page: Int = 1) {
        val clean = query.trim()
        if (clean.isBlank()) return

        _globalSearchPage.value = page
        viewModelScope.launch {
            _isGlobalSearching.value = true
            val response = searchService.searchGlobalEntertainment(
                query = clean,
                filterType = _globalEntertainmentType.value,
                page = page,
                pageSize = 12
            )
            _globalSearchResponse.value = response
            _isGlobalSearching.value = false
        }
    }

    fun setGlobalEntertainmentType(type: String?) {
        _globalEntertainmentType.value = type
        if (_globalSearchQuery.value.isNotBlank()) {
            executeGlobalSearch(_globalSearchQuery.value, page = 1)
        }
    }

    fun searchByYear(year: Int) {
        _globalSearchQuery.value = "$year"
        _currentScreen.value = Screen.GLOBAL_SEARCH
        executeGlobalSearch("$year", page = 1)
    }

    fun applyDidYouMeanSuggestion() {
        val suggestion = _globalSearchResponse.value?.didYouMeanSuggestion ?: return
        _globalSearchQuery.value = suggestion
        executeGlobalSearch(suggestion, page = 1)
    }

    fun loadNextPage() {
        val current = _globalSearchPage.value
        executeGlobalSearch(_globalSearchQuery.value, page = current + 1)
    }

    fun loadPreviousPage() {
        val current = _globalSearchPage.value
        if (current > 1) {
            executeGlobalSearch(_globalSearchQuery.value, page = current - 1)
        }
    }

    fun saveDiscoveredMovie(discovered: DiscoveredMovie) {
        viewModelScope.launch {
            val entity = discovered.toMovieEntity()
            val id = repository.insertMovie(entity)
            _quickAddNotification.value = "Added \"${discovered.title}\" to CineVault!"
            closeAddDialog()
            openMovieDetails(id)
        }
    }

    fun saveCustomMovie(movie: MovieEntity) {
        viewModelScope.launch {
            if (movie.id == 0L) {
                val newId = repository.insertMovie(movie)
                _quickAddNotification.value = "Added \"${movie.title}\" to CineVault!"
                closeAddDialog()
                openMovieDetails(newId)
            } else {
                repository.updateMovie(movie)
                _quickAddNotification.value = "Updated \"${movie.title}\"!"
                closeEditDialog()
            }
        }
    }

    fun updatePersonalRating(movieId: Long, rating: Float) {
        viewModelScope.launch {
            val movie = rawMovies.value.firstOrNull { it.id == movieId } ?: return@launch
            repository.updateMovie(movie.copy(personalRating = rating))
        }
    }

    fun updateWatchStatus(movieId: Long, status: String) {
        viewModelScope.launch {
            val movie = rawMovies.value.firstOrNull { it.id == movieId } ?: return@launch
            repository.updateMovie(movie.copy(watchStatus = status))
        }
    }

    fun updatePersonalNotes(movieId: Long, notes: String) {
        viewModelScope.launch {
            val movie = rawMovies.value.firstOrNull { it.id == movieId } ?: return@launch
            repository.updateMovie(movie.copy(personalNotes = notes))
        }
    }

    fun updateWatchedDate(movieId: Long, date: String?) {
        viewModelScope.launch {
            val movie = rawMovies.value.firstOrNull { it.id == movieId } ?: return@launch
            repository.updateMovie(movie.copy(watchedDate = date))
        }
    }

    fun toggleFavorite(movie: MovieEntity) {
        viewModelScope.launch {
            repository.updateMovie(movie.copy(isFavorite = !movie.isFavorite))
        }
    }

    fun deleteMovie(movie: MovieEntity) {
        viewModelScope.launch {
            repository.deleteMovie(movie)
            if (_selectedMovieId.value == movie.id) {
                _selectedMovieId.value = null
                _currentScreen.value = Screen.DASHBOARD
            }
            _quickAddNotification.value = "Removed \"${movie.title}\" from collection."
        }
    }
}

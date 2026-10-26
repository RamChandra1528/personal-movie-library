package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MovieViewModel
import com.example.ui.Screen
import com.example.ui.screens.AddMovieDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EditMovieDialog
import com.example.ui.screens.MovieDetailsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.CineVaultTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MovieViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CineVaultTheme {
                CineVaultApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CineVaultApp(viewModel: MovieViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val displayedMovies by viewModel.displayedMovies.collectAsStateWithLifecycle()
    val rawMovies by viewModel.rawMovies.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val genreFilter by viewModel.genreFilter.collectAsStateWithLifecycle()
    val availableGenres by viewModel.availableGenres.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val selectedMovie by viewModel.selectedMovie.collectAsStateWithLifecycle()

    val isSearchingWeb by viewModel.isSearchingWeb.collectAsStateWithLifecycle()
    val candidateResults by viewModel.candidateResults.collectAsStateWithLifecycle()
    val webSearchError by viewModel.webSearchError.collectAsStateWithLifecycle()
    val fallbackWebUrl by viewModel.fallbackWebUrl.collectAsStateWithLifecycle()
    val quickAddNotification by viewModel.quickAddNotification.collectAsStateWithLifecycle()

    val showAddDialog by viewModel.showAddDialog.collectAsStateWithLifecycle()
    val showEditDialog by viewModel.showEditDialog.collectAsStateWithLifecycle()

    val safePadding = WindowInsets.safeDrawing.asPaddingValues()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.DASHBOARD -> {
                    DashboardScreen(
                        movies = displayedMovies,
                        totalMovieCount = rawMovies.size,
                        searchQuery = searchQuery,
                        selectedStatus = statusFilter,
                        selectedGenre = genreFilter,
                        availableGenres = availableGenres,
                        sortOrder = sortOrder,
                        isSearchingWeb = isSearchingWeb,
                        notificationMessage = quickAddNotification,
                        onSearchQueryChanged = viewModel::setSearchQuery,
                        onStatusSelected = viewModel::setStatusFilter,
                        onGenreSelected = viewModel::setGenreFilter,
                        onSortOrderChanged = viewModel::setSortOrder,
                        onQuickSave = { title ->
                            viewModel.quickSaveOrSearch(title, autoSaveIfDefinite = true)
                        },
                        onMovieClick = viewModel::openMovieDetails,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onOpenAddDialog = viewModel::openAddDialog,
                        onOpenStats = { viewModel.navigateTo(Screen.STATS) },
                        onDismissNotification = viewModel::clearNotification
                    )
                }

                Screen.MOVIE_DETAILS -> {
                    selectedMovie?.let { movie ->
                        MovieDetailsScreen(
                            movie = movie,
                            onBack = { viewModel.navigateTo(Screen.DASHBOARD) },
                            onEdit = viewModel::openEditDialog,
                            onDelete = { viewModel.deleteMovie(movie) },
                            onToggleFavorite = { viewModel.toggleFavorite(movie) },
                            onUpdateRating = { rating -> viewModel.updatePersonalRating(movie.id, rating) },
                            onUpdateStatus = { status -> viewModel.updateWatchStatus(movie.id, status) },
                            onUpdateNotes = { notes -> viewModel.updatePersonalNotes(movie.id, notes) },
                            onUpdateWatchedDate = { date -> viewModel.updateWatchedDate(movie.id, date) }
                        )
                    } ?: run {
                        viewModel.navigateTo(Screen.DASHBOARD)
                    }
                }

                Screen.STATS -> {
                    StatsScreen(
                        stats = stats,
                        onBack = { viewModel.navigateTo(Screen.DASHBOARD) }
                    )
                }
            }

            // Dialogs
            if (showAddDialog) {
                AddMovieDialog(
                    isSearching = isSearchingWeb,
                    candidates = candidateResults,
                    searchError = webSearchError,
                    fallbackUrl = fallbackWebUrl,
                    onSearch = { query ->
                        viewModel.quickSaveOrSearch(query, autoSaveIfDefinite = false)
                    },
                    onSelectDiscovered = viewModel::saveDiscoveredMovie,
                    onSaveCustom = viewModel::saveCustomMovie,
                    onDismiss = viewModel::closeAddDialog
                )
            }

            if (showEditDialog && selectedMovie != null) {
                EditMovieDialog(
                    movie = selectedMovie!!,
                    onSave = viewModel::saveCustomMovie,
                    onDismiss = viewModel::closeEditDialog
                )
            }
        }
    }
}

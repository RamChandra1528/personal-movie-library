package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MovieEntity
import com.example.data.WatchStatus
import com.example.ui.SortOrder
import com.example.ui.components.CineVaultTopBar
import com.example.ui.components.MovieCard
import com.example.ui.theme.CineCardBorder
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineDarkBg
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineGreen
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceVariant
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.theme.CineTextTertiary

@Composable
fun DashboardScreen(
    movies: List<MovieEntity>,
    totalMovieCount: Int,
    searchQuery: String,
    selectedStatus: String?,
    selectedGenre: String?,
    availableGenres: List<String>,
    sortOrder: SortOrder,
    isSearchingWeb: Boolean,
    notificationMessage: String?,
    onSearchQueryChanged: (String) -> Unit,
    onStatusSelected: (String?) -> Unit,
    onGenreSelected: (String?) -> Unit,
    onSortOrderChanged: (SortOrder) -> Unit,
    onQuickSave: (String) -> Unit,
    onMovieClick: (Long) -> Unit,
    onToggleFavorite: (MovieEntity) -> Unit,
    onOpenAddDialog: () -> Unit,
    onOpenGlobalSearch: () -> Unit,
    onOpenStats: () -> Unit,
    currentUser: com.google.firebase.auth.FirebaseUser? = null,
    isSyncing: Boolean = false,
    onOpenAccount: () -> Unit = {},
    onDismissNotification: () -> Unit
) {
    var quickInput by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .testTag("dashboard_screen")
            .fillMaxSize(),
        color = CineDarkBg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // CineVault Top Bar
            CineVaultTopBar(
                onOpenGlobalSearch = onOpenGlobalSearch,
                onOpenStats = onOpenStats,
                onAddNewMovie = onOpenAddDialog,
                currentUser = currentUser,
                isSyncing = isSyncing,
                onOpenAccount = onOpenAccount
            )

            // Notification / Success Bar
            AnimatedVisibility(visible = notificationMessage != null) {
                notificationMessage?.let { msg ->
                    Surface(
                        modifier = Modifier
                            .testTag("notification_banner")
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = CineGold.copy(alpha = 0.18f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CineGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = CineGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    color = CineTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(
                                onClick = onDismissNotification,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = CineTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Main Scrollable Grid containing Hero, Quick Save, Filter controls, and Movie Cards
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 155.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: Hero Banner & Quick Add Input (Span full width)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(4.dp))

                        // Global Entertainment Search Engine Launcher Card
                        Card(
                            modifier = Modifier
                                .testTag("global_search_launcher_card")
                                .fillMaxWidth()
                                .clickable(onClick = onOpenGlobalSearch),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CineSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CineCyan.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = CineCyan.copy(alpha = 0.18f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null,
                                                tint = CineCyan,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Global Entertainment Search",
                                                color = CineTextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = CineCyan
                                            ) {
                                                Text(
                                                    text = "LIVE",
                                                    color = CineDarkBg,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Search movies, series, anime, actors, or years (e.g. 1999)",
                                            color = CineTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "Explore →",
                                    color = CineGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Add Card: The core workflow requested
                        Card(
                            modifier = Modifier
                                .testTag("quick_save_card")
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CineSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CineCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = CineGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "One-Tap Movie Discovery & Save",
                                        color = CineTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Enter a title (e.g. Interstellar) — poster & details are automatically fetched from public sources.",
                                    color = CineTextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = quickInput,
                                        onValueChange = { quickInput = it },
                                        placeholder = { Text("Movie name (e.g., Interstellar)") },
                                        modifier = Modifier
                                            .testTag("quick_movie_input")
                                            .weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = {
                                            if (quickInput.isNotBlank() && !isSearchingWeb) {
                                                onQuickSave(quickInput)
                                                quickInput = ""
                                            }
                                        }),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CineGold,
                                            unfocusedBorderColor = CineCardBorder,
                                            focusedTextColor = CineTextPrimary,
                                            unfocusedTextColor = CineTextPrimary,
                                            cursorColor = CineGold
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            if (quickInput.isNotBlank() && !isSearchingWeb) {
                                                onQuickSave(quickInput)
                                                quickInput = ""
                                            }
                                        },
                                        enabled = quickInput.isNotBlank() && !isSearchingWeb,
                                        modifier = Modifier
                                            .testTag("quick_save_button")
                                            .height(52.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CineGold,
                                            contentColor = CineDarkBg
                                        )
                                    ) {
                                        if (isSearchingWeb) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = CineDarkBg
                                            )
                                        } else {
                                            Text("Save", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Quick suggestion chips for convenience
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Try:",
                                        color = CineTextTertiary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.align(Alignment.CenterVertically)
                                    )
                                    listOf("Interstellar", "Inception", "The Dark Knight", "Spirited Away", "Blade Runner 2049").forEach { sample ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = CineSurfaceVariant,
                                            modifier = Modifier.clickable {
                                                onQuickSave(sample)
                                            }
                                        ) {
                                            Text(
                                                text = "+ $sample",
                                                color = CineCyan,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Collection Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = { Text("Filter your vault collection...") },
                            modifier = Modifier
                                .testTag("collection_search_bar")
                                .fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = CineTextSecondary
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchQueryChanged("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = CineTextSecondary
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CineCyan,
                                unfocusedBorderColor = CineCardBorder,
                                focusedTextColor = CineTextPrimary,
                                unfocusedTextColor = CineTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Filter Chips Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val statusFilters = listOf(
                                "ALL" to "All",
                                WatchStatus.COMPLETED to "Watched",
                                WatchStatus.WATCHING to "Watching",
                                WatchStatus.PLAN_TO_WATCH to "Plan to Watch",
                                "FAVORITES" to "★ Favorites",
                                WatchStatus.DROPPED to "Dropped"
                            )

                            statusFilters.forEach { (key, label) ->
                                val isSelected = (selectedStatus ?: "ALL") == key
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onStatusSelected(key) },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CineGold,
                                        selectedLabelColor = CineDarkBg,
                                        containerColor = CineSurface,
                                        labelColor = CineTextSecondary
                                    )
                                )
                            }
                        }

                        // Genre Filter Chips Row
                        if (availableGenres.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val allGenreSelected = (selectedGenre ?: "ALL") == "ALL"
                                FilterChip(
                                    selected = allGenreSelected,
                                    onClick = { onGenreSelected("ALL") },
                                    label = { Text("All Genres", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CineCyan,
                                        selectedLabelColor = CineDarkBg
                                    )
                                )

                                availableGenres.forEach { genre ->
                                    val isSelected = selectedGenre == genre
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onGenreSelected(genre) },
                                        label = { Text(genre, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CineCyan,
                                            selectedLabelColor = CineDarkBg
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Results Header with Count & Sort Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Personal Vault (${movies.size})",
                                color = CineTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Box {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CineSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CineCardBorder),
                                    modifier = Modifier.clickable { showSortMenu = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sort,
                                            contentDescription = "Sort",
                                            tint = CineGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = sortOrder.label,
                                            color = CineTextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false },
                                    modifier = Modifier.background(CineSurface)
                                ) {
                                    SortOrder.values().forEach { order ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = order.label,
                                                    color = if (sortOrder == order) CineGold else CineTextPrimary,
                                                    fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                onSortOrderChanged(order)
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // Section 2: Empty State
                if (movies.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Card(
                            modifier = Modifier
                                .testTag("empty_collection_card")
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CineSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CineCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalMovies,
                                    contentDescription = null,
                                    tint = CineGold,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (totalMovieCount == 0) "Your CineVault is Empty" else "No Movies Match Filters",
                                    color = CineTextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (totalMovieCount == 0)
                                        "Type a movie title above and click Save to automatically discover its poster and metadata."
                                    else
                                        "Try adjusting your search query, status, or genre filters.",
                                    color = CineTextSecondary,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )

                                if (totalMovieCount == 0) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = onOpenAddDialog,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CineGold,
                                            contentColor = CineDarkBg
                                        )
                                    ) {
                                        Text("Search & Add Your First Movie", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Section 3: Movie Poster Grid items
                    items(movies, key = { it.id }) { movie ->
                        MovieCard(
                            movie = movie,
                            onClick = { onMovieClick(movie.id) },
                            onToggleFavorite = onToggleFavorite
                        )
                    }
                }
            }
        }
    }
}

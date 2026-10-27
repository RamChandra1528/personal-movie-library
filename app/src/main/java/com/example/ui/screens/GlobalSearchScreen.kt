package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.EntertainmentType
import com.example.service.DiscoveredMovie
import com.example.service.GlobalSearchResponse
import com.example.ui.components.MoviePosterFallback
import com.example.ui.theme.CineCardBorder
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineDarkBg
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineGreen
import com.example.ui.theme.CineStarGold
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceVariant
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.theme.CineTextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlobalSearchScreen(
    searchQuery: String,
    searchResponse: GlobalSearchResponse?,
    isSearching: Boolean,
    selectedType: String?,
    currentPage: Int,
    savedTitles: Set<String>,
    onQueryChanged: (String) -> Unit,
    onSearchSubmitted: (String) -> Unit,
    onTypeSelected: (String?) -> Unit,
    onYearSelected: (Int) -> Unit,
    onApplyDidYouMean: () -> Unit,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onSaveToVault: (DiscoveredMovie) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .testTag("global_search_screen")
            .fillMaxSize(),
        color = CineDarkBg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CineSurface)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CineTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Global Search Engine",
                            color = CineTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CineCyan.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "LIVE",
                                color = CineCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Real-time index: Movies, TV series, anime, dramas, actors & years",
                        color = CineTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Search input field with instant search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChanged,
                placeholder = { Text("Search title, actor, genre, keyword, or year (e.g. 1999)...") },
                modifier = Modifier
                    .testTag("global_search_input")
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = CineGold
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = CineTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (searchQuery.isNotBlank()) onSearchSubmitted(searchQuery)
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CineGold,
                    unfocusedBorderColor = CineCardBorder,
                    focusedTextColor = CineTextPrimary,
                    unfocusedTextColor = CineTextPrimary,
                    cursorColor = CineGold
                )
            )

            // Spelling mistake / Did you mean prompt
            AnimatedVisibility(visible = searchResponse?.didYouMeanSuggestion != null) {
                searchResponse?.didYouMeanSuggestion?.let { suggestion ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { onApplyDidYouMean() },
                        shape = RoundedCornerShape(8.dp),
                        color = CineSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CineGold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CineGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Did you mean: ",
                                color = CineTextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = suggestion,
                                color = CineGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(Tap to search)",
                                color = CineCyan,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Entertainment Type Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val types = listOf(
                    "ALL" to "All 🌐",
                    EntertainmentType.MOVIE to "Movies 🎬",
                    EntertainmentType.TV_SERIES to "TV Series 📺",
                    EntertainmentType.ANIME to "Anime ⚡",
                    EntertainmentType.DRAMA to "Dramas 🎭"
                )

                types.forEach { (typeKey, label) ->
                    val isSelected = (selectedType ?: "ALL") == typeKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTypeSelected(typeKey) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CineGold,
                            selectedLabelColor = CineDarkBg,
                            containerColor = CineSurface,
                            labelColor = CineTextSecondary
                        )
                    )
                }
            }

            // Quick Year Jump Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Release Year:",
                    color = CineTextTertiary,
                    fontSize = 11.sp
                )
                listOf(2024, 2023, 2022, 2020, 2019, 1999, 1994, 1982).forEach { year ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (searchQuery == "$year") CineCyan else CineSurfaceVariant,
                        modifier = Modifier.clickable { onYearSelected(year) }
                    ) {
                        Text(
                            text = "$year",
                            color = if (searchQuery == "$year") CineDarkBg else CineTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Year Showcase Banner if query is a year
            if (searchResponse?.isYearShowcase == true && searchResponse.yearShowcaseNumber != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = CineSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CineCyan)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Year Showcase: ${searchResponse.yearShowcaseNumber}",
                                color = CineCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Displaying movies, web series, anime & dramas released in ${searchResponse.yearShowcaseNumber}",
                                color = CineTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = "Page $currentPage",
                            color = CineGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Results List
            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = CineGold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Searching global entertainment registries in real time...",
                            color = CineTextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Wikipedia, TVMaze, MyAnimeList & public databases",
                            color = CineCyan,
                            fontSize = 11.sp
                        )
                    }
                }
            } else if (searchResponse != null && searchResponse.items.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(searchResponse.items) { item ->
                        val isAlreadySaved = savedTitles.contains(item.title.lowercase())
                        GlobalEntertainmentResultCard(
                            item = item,
                            isSaved = isAlreadySaved,
                            onSave = { onSaveToVault(item) },
                            onSearchSimilar = { simTitle ->
                                onQueryChanged(simTitle)
                                onSearchSubmitted(simTitle)
                            }
                        )
                    }

                    // Pagination Footer
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onPreviousPage,
                                enabled = currentPage > 1,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CineCyan)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Previous")
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = "Page $currentPage",
                                color = CineTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            OutlinedButton(
                                onClick = onNextPage,
                                enabled = searchResponse.hasMorePages,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CineGold)
                            ) {
                                Text("Next")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            } else if (searchQuery.isNotBlank()) {
                // Empty search results
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "No direct entertainment results found for \"$searchQuery\"",
                            color = CineTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Check spelling or search Google, IMDb & TMDB directly below:",
                            color = CineTextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${searchQuery}+movie+series"))
                                    context.startActivity(intent)
                                }
                            ) {
                                Text("Google Search")
                            }
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.imdb.com/find?q=${searchQuery}"))
                                    context.startActivity(intent)
                                }
                            ) {
                                Text("IMDb")
                            }
                        }
                    }
                }
            } else {
                // Initial prompt state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = CineGold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Global Entertainment Explorer",
                            color = CineTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Search across Marvel, Hollywood blockbusters, anime, & dramas",
                            color = CineTextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Popular Categories & Franchises:",
                            color = CineGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.Center,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "Marvel Movies",
                                "Hollywood Movies",
                                "Avengers",
                                "Spider-Man",
                                "Inception",
                                "The Dark Knight",
                                "Interstellar",
                                "Titanic"
                            ).forEach { topic ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = CineSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CineCyan.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .clickable {
                                            onQueryChanged(topic)
                                            onSearchSubmitted(topic)
                                        }
                                ) {
                                    Text(
                                        text = topic,
                                        color = CineTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlobalEntertainmentResultCard(
    item: DiscoveredMovie,
    isSaved: Boolean,
    onSave: () -> Unit,
    onSearchSimilar: (String) -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CineSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CineCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Poster image
                Box(
                    modifier = Modifier
                        .width(78.dp)
                        .aspectRatio(0.68f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CineSurfaceVariant)
                ) {
                    if (!item.posterUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.posterUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        MoviePosterFallback(title = item.title)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Type pill & Match reason
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (item.contentType) {
                                EntertainmentType.ANIME -> CineCyan.copy(alpha = 0.2f)
                                EntertainmentType.TV_SERIES -> CineGold.copy(alpha = 0.2f)
                                EntertainmentType.DRAMA -> Color(0xFF9C27B0).copy(alpha = 0.2f)
                                else -> CineGreen.copy(alpha = 0.2f)
                            }
                        ) {
                            Text(
                                text = "${EntertainmentType.getIcon(item.contentType)} ${EntertainmentType.getDisplayName(item.contentType)}",
                                color = when (item.contentType) {
                                    EntertainmentType.ANIME -> CineCyan
                                    EntertainmentType.TV_SERIES -> CineGold
                                    EntertainmentType.DRAMA -> Color(0xFFBA68C8)
                                    else -> CineGreen
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (!item.matchReason.isNullOrBlank()) {
                            Text(
                                text = item.matchReason,
                                color = CineGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Title & Year
                    Text(
                        text = item.title,
                        color = CineTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Alternate Titles if available
                    if (item.alternateTitles.isNotEmpty()) {
                        Text(
                            text = "Alt: ${item.alternateTitles.first()}",
                            color = CineTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Metadata row: Year, Runtime, Rating
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item.releaseYear?.let { year ->
                            Text(
                                text = "$year",
                                color = CineGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        if (!item.externalRating.isNullOrBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = CineStarGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = item.externalRating,
                                    color = CineStarGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        if (item.genres.isNotEmpty()) {
                            Text(
                                text = item.genres.take(2).joinToString(" • "),
                                color = CineTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Synopsis preview
                    if (item.synopsis.isNotBlank()) {
                        Text(
                            text = item.synopsis,
                            color = CineTextTertiary,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Similar titles chips
            if (item.similarTitles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Similar: ",
                        color = CineTextTertiary,
                        fontSize = 10.sp
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item.similarTitles.take(3).forEach { sim ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CineSurfaceVariant,
                                modifier = Modifier.clickable { onSearchSimilar(sim) }
                            ) {
                                Text(
                                    text = sim,
                                    color = CineCyan,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons row: Save to Vault + External Links (Google, IMDb, TMDB)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // External links row
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CineSurfaceVariant,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.googleSearchUrl))
                            context.startActivity(intent)
                        }
                    ) {
                        Text(
                            text = "Google",
                            color = CineTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CineSurfaceVariant,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.imdbSearchUrl))
                            context.startActivity(intent)
                        }
                    ) {
                        Text(
                            text = "IMDb",
                            color = CineGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CineSurfaceVariant,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.tmdbSearchUrl))
                            context.startActivity(intent)
                        }
                    ) {
                        Text(
                            text = "TMDB",
                            color = CineCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }

                // Save Button
                Button(
                    onClick = onSave,
                    enabled = !isSaved,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSaved) CineSurfaceVariant else CineGold,
                        contentColor = if (isSaved) CineTextSecondary else CineDarkBg
                    )
                ) {
                    if (isSaved) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = CineGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("In Vault", fontSize = 11.sp, color = CineGreen)
                    } else {
                        Text("+ Save to Vault", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

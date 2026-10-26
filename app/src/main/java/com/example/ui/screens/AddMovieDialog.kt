package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.MovieEntity
import com.example.data.WatchStatus
import com.example.service.DiscoveredMovie
import com.example.ui.components.MoviePosterFallback
import com.example.ui.theme.CineCardBorder
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineDarkBg
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineRed
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceVariant
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.theme.CineTextTertiary

@Composable
fun AddMovieDialog(
    isSearching: Boolean,
    candidates: List<DiscoveredMovie>,
    searchError: String?,
    fallbackUrl: String?,
    onSearch: (String) -> Unit,
    onSelectDiscovered: (DiscoveredMovie) -> Unit,
    onSaveCustom: (MovieEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchInput by remember { mutableStateOf("") }

    // Manual entry fields
    var manualTitle by remember { mutableStateOf("") }
    var manualYear by remember { mutableStateOf("") }
    var manualDirector by remember { mutableStateOf("") }
    var manualGenres by remember { mutableStateOf("") }
    var manualSynopsis by remember { mutableStateOf("") }
    var manualPosterUrl by remember { mutableStateOf("") }
    var manualRuntime by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .testTag("add_movie_dialog")
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, CineCardBorder, RoundedCornerShape(20.dp)),
            color = CineSurface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CineDarkBg)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Add Movie to CineVault",
                            color = CineTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Automated public web metadata & poster discovery",
                            color = CineGold,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CineTextSecondary
                        )
                    }
                }

                // Tabs: Public Web Discovery vs Manual Entry
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CineDarkBg,
                    contentColor = CineGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CineGold
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Public Discovery")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Manual Entry")
                            }
                        }
                    )
                }

                if (selectedTab == 0) {
                    // TAB 0: Public Web Discovery
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Movie Title search input
                        OutlinedTextField(
                            value = searchInput,
                            onValueChange = { searchInput = it },
                            modifier = Modifier
                                .testTag("search_movie_input")
                                .fillMaxWidth(),
                            placeholder = { Text("Enter movie name (e.g., Interstellar)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = CineGold
                                )
                            },
                            trailingIcon = {
                                if (searchInput.isNotBlank()) {
                                    IconButton(onClick = { searchInput = "" }) {
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
                                if (searchInput.isNotBlank()) onSearch(searchInput)
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CineGold,
                                unfocusedBorderColor = CineCardBorder,
                                focusedTextColor = CineTextPrimary,
                                unfocusedTextColor = CineTextPrimary,
                                cursorColor = CineGold
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (searchInput.isNotBlank()) onSearch(searchInput)
                            },
                            enabled = searchInput.isNotBlank() && !isSearching,
                            modifier = Modifier
                                .testTag("discover_button")
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CineGold,
                                contentColor = CineDarkBg
                            )
                        ) {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = CineDarkBg
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Discovering metadata...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Discover & Save", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Results area
                        if (isSearching) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = CineCyan)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Searching Wikipedia & open web databases...",
                                        color = CineTextSecondary,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Zero API keys required",
                                        color = CineGold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        } else if (searchError != null && candidates.isEmpty()) {
                            // Error or no automated matches state
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = searchError,
                                    color = CineTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                if (fallbackUrl != null) {
                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CineCyan)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInBrowser,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Open Public Search in Browser")
                                    }
                                }
                            }
                        } else if (candidates.isNotEmpty()) {
                            // Matching candidate list for user verification
                            Text(
                                text = "Select matching verified movie (${candidates.size} results):",
                                color = CineTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                items(candidates) { candidate ->
                                    DiscoveredCandidateCard(
                                        candidate = candidate,
                                        onSelect = { onSelectDiscovered(candidate) }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        } else {
                            // Initial placeholder info
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = CineTextTertiary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Enter any film title to automatically fetch",
                                        color = CineTextSecondary,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Poster, year, director, genres, cast & synopsis",
                                        color = CineTextTertiary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: Manual Entry Form
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = manualTitle,
                                onValueChange = { manualTitle = it },
                                label = { Text("Movie Title *") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = manualYear,
                                    onValueChange = { manualYear = it },
                                    label = { Text("Release Year") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = manualRuntime,
                                    onValueChange = { manualRuntime = it },
                                    label = { Text("Runtime (mins)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = manualDirector,
                                onValueChange = { manualDirector = it },
                                label = { Text("Director") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = manualGenres,
                                onValueChange = { manualGenres = it },
                                label = { Text("Genres (comma-separated, e.g. Sci-Fi, Drama)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = manualPosterUrl,
                                onValueChange = { manualPosterUrl = it },
                                label = { Text("Poster Image URL") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = manualSynopsis,
                                onValueChange = { manualSynopsis = it },
                                label = { Text("Synopsis / Overview") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 6
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (manualTitle.isNotBlank()) {
                                        onSaveCustom(
                                            MovieEntity(
                                                title = manualTitle.trim(),
                                                releaseYear = manualYear.toIntOrNull(),
                                                runtimeMinutes = manualRuntime.toIntOrNull(),
                                                director = manualDirector.trim(),
                                                genres = manualGenres.trim(),
                                                synopsis = manualSynopsis.trim(),
                                                posterUrl = manualPosterUrl.trim().ifBlank { null },
                                                watchStatus = WatchStatus.PLAN_TO_WATCH
                                            )
                                        )
                                    }
                                },
                                enabled = manualTitle.isNotBlank(),
                                modifier = Modifier
                                    .testTag("save_manual_button")
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CineGold, contentColor = CineDarkBg)
                            ) {
                                Text("Save to CineVault", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiscoveredCandidateCard(
    candidate: DiscoveredMovie,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(10.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = CineSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster thumbnail
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .aspectRatio(0.68f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black)
            ) {
                if (!candidate.posterUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(candidate.posterUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = candidate.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    MoviePosterFallback(title = candidate.title)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = candidate.title,
                        color = CineTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    candidate.releaseYear?.let { year ->
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "($year)",
                            color = CineGold,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                if (candidate.director.isNotBlank()) {
                    Text(
                        text = "Dir: ${candidate.director}",
                        color = CineTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (candidate.genres.isNotEmpty()) {
                    Text(
                        text = candidate.genres.take(3).joinToString(", "),
                        color = CineCyan,
                        fontSize = 11.sp
                    )
                }

                if (candidate.synopsis.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = candidate.synopsis,
                        color = CineTextTertiary,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = candidate.sourceName,
                        color = CineGold.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Button(
                        onClick = onSelect,
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CineGold,
                            contentColor = CineDarkBg
                        )
                    ) {
                        Text("Save", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

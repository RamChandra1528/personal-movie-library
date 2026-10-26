package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.MovieEntity
import com.example.data.WatchStatus
import com.example.ui.components.RatingStars
import com.example.ui.theme.CineCardBorder
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineDarkBg
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary

@Composable
fun EditMovieDialog(
    movie: MovieEntity,
    onSave: (MovieEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(movie.title) }
    var year by remember { mutableStateOf(movie.releaseYear?.toString() ?: "") }
    var director by remember { mutableStateOf(movie.director) }
    var cast by remember { mutableStateOf(movie.cast) }
    var genres by remember { mutableStateOf(movie.genres) }
    var runtime by remember { mutableStateOf(movie.runtimeMinutes?.toString() ?: "") }
    var posterUrl by remember { mutableStateOf(movie.posterUrl ?: "") }
    var synopsis by remember { mutableStateOf(movie.synopsis) }
    var watchStatus by remember { mutableStateOf(movie.watchStatus) }
    var personalRating by remember { mutableFloatStateOf(movie.personalRating) }
    var personalNotes by remember { mutableStateOf(movie.personalNotes ?: "") }
    var watchedDate by remember { mutableStateOf(movie.watchedDate ?: "") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .testTag("edit_movie_dialog")
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
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
                    Text(
                        text = "Edit Movie Details",
                        color = CineTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CineTextSecondary
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    item {
                        // Title
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title *") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Year & Runtime
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = year,
                                onValueChange = { year = it },
                                label = { Text("Release Year") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = runtime,
                                onValueChange = { runtime = it },
                                label = { Text("Runtime (mins)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // Watch Status Chips
                        Text(
                            text = "Watch Status",
                            color = CineTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            WatchStatus.allStatuses.forEach { status ->
                                FilterChip(
                                    selected = watchStatus == status,
                                    onClick = { watchStatus = status },
                                    label = {
                                        Text(
                                            WatchStatus.getDisplayName(status),
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CineGold,
                                        selectedLabelColor = CineDarkBg
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Personal Rating
                        Text(
                            text = "Personal Rating (1-5 Stars)",
                            color = CineTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        RatingStars(
                            rating = personalRating,
                            starSize = 28.dp,
                            interactive = true,
                            onRatingChanged = { personalRating = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Watched Date
                        OutlinedTextField(
                            value = watchedDate,
                            onValueChange = { watchedDate = it },
                            label = { Text("Watched Date (e.g. YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Personal Notes
                        OutlinedTextField(
                            value = personalNotes,
                            onValueChange = { personalNotes = it },
                            label = { Text("Personal Review & Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Director & Cast
                        OutlinedTextField(
                            value = director,
                            onValueChange = { director = it },
                            label = { Text("Director") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = cast,
                            onValueChange = { cast = it },
                            label = { Text("Cast (comma-separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Genres
                        OutlinedTextField(
                            value = genres,
                            onValueChange = { genres = it },
                            label = { Text("Genres (comma-separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Poster URL
                        OutlinedTextField(
                            value = posterUrl,
                            onValueChange = { posterUrl = it },
                            label = { Text("Poster Image URL") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Synopsis
                        OutlinedTextField(
                            value = synopsis,
                            onValueChange = { synopsis = it },
                            label = { Text("Synopsis / Overview") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    val updated = movie.copy(
                                        title = title.trim(),
                                        releaseYear = year.toIntOrNull(),
                                        runtimeMinutes = runtime.toIntOrNull(),
                                        director = director.trim(),
                                        cast = cast.trim(),
                                        genres = genres.trim(),
                                        posterUrl = posterUrl.trim().ifBlank { null },
                                        synopsis = synopsis.trim(),
                                        watchStatus = watchStatus,
                                        personalRating = personalRating,
                                        personalNotes = personalNotes.trim().ifBlank { null },
                                        watchedDate = watchedDate.trim().ifBlank { null }
                                    )
                                    onSave(updated)
                                }
                            },
                            enabled = title.isNotBlank(),
                            modifier = Modifier
                                .testTag("save_edit_button")
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CineGold,
                                contentColor = CineDarkBg
                            )
                        ) {
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

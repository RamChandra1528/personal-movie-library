package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.MovieEntity
import com.example.data.WatchStatus
import com.example.ui.components.ExternalRatingBadge
import com.example.ui.components.MoviePosterFallback
import com.example.ui.components.RatingStars
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CineCardBorder
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineDarkBg
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineRed
import com.example.ui.theme.CineStarGold
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceVariant
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.theme.CineTextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MovieDetailsScreen(
    movie: MovieEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    onUpdateRating: (Float) -> Unit,
    onUpdateStatus: (String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onUpdateWatchedDate: (String?) -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditingNotes by remember { mutableStateOf(false) }
    var noteText by remember(movie.personalNotes) { mutableStateOf(movie.personalNotes ?: "") }

    var isEditingDate by remember { mutableStateOf(false) }
    var dateText by remember(movie.watchedDate) { mutableStateOf(movie.watchedDate ?: "") }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Movie?") },
            text = { Text("Are you sure you want to remove \"${movie.title}\" from your CineVault collection?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CineRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = CineTextSecondary)
                }
            },
            containerColor = CineSurface,
            titleContentColor = CineTextPrimary,
            textContentColor = CineTextSecondary
        )
    }

    Surface(
        modifier = Modifier
            .testTag("movie_details_screen")
            .fillMaxSize(),
        color = CineDarkBg
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Hero Header with Poster Backdrop
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    if (!movie.posterUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(movie.posterUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = movie.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        MoviePosterFallback(
                            title = movie.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Scrim Gradients
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.65f),
                                        Color.Black.copy(alpha = 0.35f),
                                        CineDarkBg
                                    )
                                )
                            )
                    )

                    // Top Action Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .testTag("details_back_button")
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onToggleFavorite,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = if (movie.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (movie.isFavorite) CineRed else Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = onEdit,
                                modifier = Modifier
                                    .testTag("details_edit_button")
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Movie",
                                    tint = CineGold
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier
                                    .testTag("details_delete_button")
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Movie",
                                    tint = CineRed
                                )
                            }
                        }
                    }

                    // Bottom info overlay inside Hero
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(status = movie.watchStatus)

                            movie.releaseYear?.let { year ->
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CineSurfaceVariant
                                ) {
                                    Text(
                                        text = "$year",
                                        color = CineGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (!movie.externalRating.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                ExternalRatingBadge(rating = movie.externalRating)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = movie.title,
                            color = CineTextPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 32.sp
                        )

                        if (!movie.originalTitle.isNullOrBlank() && movie.originalTitle != movie.title) {
                            Text(
                                text = "Original: ${movie.originalTitle}",
                                color = CineTextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        // Metadata Quick Row (Runtime, Language, Director)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (movie.runtimeMinutes != null) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = CineCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = movie.formattedRuntime,
                                    color = CineTextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            if (!movie.language.isNullOrBlank()) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = CineTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = movie.language,
                                    color = CineTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Body content
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Genres flow row
                    if (movie.genresList.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            movie.genresList.forEach { genre ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = CineSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CineCardBorder)
                                ) {
                                    Text(
                                        text = genre,
                                        color = CineCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // Interactive Status & Rating Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CineSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CineCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Your Personal Assessment",
                                color = CineTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Watch Status selector chips
                            Text(
                                text = "Watch Status",
                                color = CineTextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                WatchStatus.allStatuses.forEach { status ->
                                    val isSelected = movie.watchStatus == status
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onUpdateStatus(status) },
                                        label = {
                                            Text(
                                                WatchStatus.getDisplayName(status),
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CineGold,
                                            selectedLabelColor = CineDarkBg
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 5-Star Interactive Rating
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Personal Rating",
                                        color = CineTextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = if (movie.personalRating > 0f) "${movie.personalRating.toInt()} / 5 Stars" else "Not rated yet",
                                        color = if (movie.personalRating > 0f) CineStarGold else CineTextTertiary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                RatingStars(
                                    rating = movie.personalRating,
                                    starSize = 26.dp,
                                    interactive = true,
                                    onRatingChanged = onUpdateRating
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Watched Date
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = CineCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (!movie.watchedDate.isNullOrBlank()) "Watched on: ${movie.watchedDate}" else "No watched date set",
                                        color = CineTextSecondary,
                                        fontSize = 13.sp
                                    )
                                }

                                TextButton(
                                    onClick = { isEditingDate = !isEditingDate }
                                ) {
                                    Text(
                                        text = if (isEditingDate) "Done" else "Set Date",
                                        color = CineCyan,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            if (isEditingDate) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = dateText,
                                        onValueChange = { dateText = it },
                                        placeholder = { Text("YYYY-MM-DD") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            onUpdateWatchedDate(dateText.trim().ifBlank { null })
                                            isEditingDate = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CineGold, contentColor = CineDarkBg)
                                    ) {
                                        Text("Save", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Personal Notes / Review Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CineSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CineCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Personal Review & Notes",
                                    color = CineTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                TextButton(
                                    onClick = {
                                        if (isEditingNotes) {
                                            onUpdateNotes(noteText.trim())
                                        }
                                        isEditingNotes = !isEditingNotes
                                    }
                                ) {
                                    Text(
                                        text = if (isEditingNotes) "Save Notes" else if (movie.personalNotes.isNullOrBlank()) "+ Add Notes" else "Edit",
                                        color = CineGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            if (isEditingNotes) {
                                OutlinedTextField(
                                    value = noteText,
                                    onValueChange = { noteText = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    placeholder = { Text("Write your thoughts, memorable scenes, favorite quotes...") },
                                    minLines = 3,
                                    maxLines = 8
                                )
                            } else {
                                if (!movie.personalNotes.isNullOrBlank()) {
                                    Text(
                                        text = movie.personalNotes,
                                        color = CineTextPrimary,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                } else {
                                    Text(
                                        text = "No personal thoughts recorded yet for this film.",
                                        color = CineTextTertiary,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Synopsis Card
                    if (movie.synopsis.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CineSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CineCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Synopsis & Overview",
                                    color = CineTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = movie.synopsis,
                                    color = CineTextSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 21.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // Cast & Crew Card
                    if (movie.director.isNotBlank() || movie.castList.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CineSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CineCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Cast & Crew",
                                    color = CineTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (movie.director.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Movie,
                                            contentDescription = null,
                                            tint = CineGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Director: ",
                                            color = CineTextSecondary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = movie.director,
                                            color = CineTextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (movie.castList.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Top Cast:",
                                        color = CineTextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        movie.castList.forEach { actor ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = CineSurfaceVariant
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = CineTextTertiary,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = actor,
                                                        color = CineTextPrimary,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // Public Source & Attribution Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CineSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CineCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Public Source & Attribution",
                                color = CineTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Discovered via ${movie.sourceAttribution ?: "Wikipedia & Public Web Registry"}",
                                color = CineTextSecondary,
                                fontSize = 12.sp
                            )

                            if (!movie.sourceUrl.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(movie.sourceUrl))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CineCyan)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInBrowser,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open Verified Public Page", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

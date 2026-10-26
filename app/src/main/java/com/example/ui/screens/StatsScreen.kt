package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CollectionStats
import com.example.ui.theme.CineCardBorder
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineDarkBg
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineGreen
import com.example.ui.theme.CineRed
import com.example.ui.theme.CineStarGold
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineSurfaceVariant
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary
import com.example.ui.theme.CineTextTertiary

@Composable
fun StatsScreen(
    stats: CollectionStats,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    Surface(
        modifier = Modifier
            .testTag("stats_screen")
            .fillMaxSize(),
        color = CineDarkBg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        contentDescription = "Back to Collection",
                        tint = CineTextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Vault Statistics",
                        color = CineTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your personal movie viewing telemetry",
                        color = CineGold,
                        fontSize = 12.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    // 2x2 Metric Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Total Movies",
                            value = "${stats.totalMovies}",
                            subtitle = "Saved in CineVault",
                            icon = Icons.Default.LocalMovies,
                            iconTint = CineGold,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Watch Time",
                            value = formatHours(stats.totalRuntimeMinutes),
                            subtitle = "${stats.totalRuntimeMinutes} minutes",
                            icon = Icons.Default.AccessTime,
                            iconTint = CineCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Average Rating",
                            value = if (stats.averageRating > 0f) String.format("%.1f ★", stats.averageRating) else "N/A",
                            subtitle = "Personal 5-star scale",
                            icon = Icons.Default.Star,
                            iconTint = CineStarGold,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Favorites",
                            value = "${stats.favoritesCount}",
                            subtitle = "Starred classics",
                            icon = Icons.Default.Favorite,
                            iconTint = CineRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Watch Status Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CineSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CineCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Collection Status Breakdown",
                                color = CineTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            val total = stats.totalMovies.coerceAtLeast(1)

                            StatusProgressRow(
                                label = "Completed",
                                count = stats.completedCount,
                                total = total,
                                color = CineGreen,
                                icon = Icons.Default.CheckCircle
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            StatusProgressRow(
                                label = "Currently Watching",
                                count = stats.watchingCount,
                                total = total,
                                color = CineCyan,
                                icon = Icons.Default.PlayArrow
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            StatusProgressRow(
                                label = "Plan to Watch",
                                count = stats.planToWatchCount,
                                total = total,
                                color = CineGold,
                                icon = Icons.Default.Bookmark
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            StatusProgressRow(
                                label = "Dropped",
                                count = stats.droppedCount,
                                total = total,
                                color = CineRed,
                                icon = Icons.Default.LocalMovies
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Top Genres Section
                    if (stats.topGenres.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CineSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CineCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Top Genres in Vault",
                                    color = CineTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                val maxGenre = stats.topGenres.maxOfOrNull { it.second } ?: 1

                                stats.topGenres.forEach { (genre, count) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = genre,
                                            color = CineTextSecondary,
                                            fontSize = 13.sp,
                                            modifier = Modifier.width(90.dp)
                                        )
                                        LinearProgressIndicator(
                                            progress = { count / maxGenre.toFloat() },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = CineGold,
                                            trackColor = CineSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "$count",
                                            color = CineTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CineSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CineCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = CineTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = CineTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = CineTextTertiary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun StatusProgressRow(
    label: String,
    count: Int,
    total: Int,
    color: Color,
    icon: ImageVector
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = CineTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "$count (${(count * 100 / total.toFloat()).toInt()}%)",
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { count / total.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = CineSurfaceVariant
        )
    }
}

private fun formatHours(minutes: Int): String {
    val hours = minutes / 60
    val days = hours / 24
    return if (days > 0) {
        val remH = hours % 24
        "${days}d ${remH}h"
    } else {
        "${hours}h"
    }
}

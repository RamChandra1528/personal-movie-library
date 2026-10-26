package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.ui.theme.CineGreen
import com.example.ui.theme.CineSurfaceVariant
import com.google.firebase.auth.FirebaseUser
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineDarkBg
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineGoldBright
import com.example.ui.theme.CineSurface
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary

/**
 * Custom CineVault Vector Logo:
 * Draws an artistic film frame surrounding a secure vault combination lock dial.
 */
@Composable
fun CineVaultLogo(
    modifier: Modifier = Modifier,
    sizeDp: Int = 40
) {
    Box(
        modifier = modifier
            .testTag("cinevault_logo")
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1E2538), Color(0xFF10141E))
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(listOf(CineGold, CineCyan)),
                RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size((sizeDp * 0.72f).dp)) {
            val w = size.width
            val h = size.height

            // Outer film reel frame
            drawRoundRect(
                color = CineGold,
                size = Size(w, h),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 3.5f)
            )

            // Film perforation sprocket holes on left and right
            val sprocW = 3.5f
            val sprocH = 4.5f
            for (i in 0..3) {
                val yOffset = h * 0.15f + i * (h * 0.22f)
                // Left perforation
                drawRect(
                    color = CineCyan,
                    topLeft = Offset(2f, yOffset),
                    size = Size(sprocW, sprocH)
                )
                // Right perforation
                drawRect(
                    color = CineCyan,
                    topLeft = Offset(w - sprocW - 2f, yOffset),
                    size = Size(sprocW, sprocH)
                )
            }

            // Central Vault Dial Ring
            val center = Offset(w / 2f, h / 2f)
            val dialRadius = w * 0.28f
            drawCircle(
                color = CineGoldBright,
                center = center,
                radius = dialRadius,
                style = Stroke(width = 3f)
            )

            // Vault dial tick notches
            for (angle in 0 until 360 step 45) {
                val rad = Math.toRadians(angle.toDouble())
                val innerR = dialRadius - 3.5f
                val outerR = dialRadius + 2f
                val start = Offset(
                    center.x + (innerR * Math.cos(rad)).toFloat(),
                    center.y + (innerR * Math.sin(rad)).toFloat()
                )
                val end = Offset(
                    center.x + (outerR * Math.cos(rad)).toFloat(),
                    center.y + (outerR * Math.sin(rad)).toFloat()
                )
                drawLine(
                    color = CineCyan,
                    start = start,
                    end = end,
                    strokeWidth = 2f
                )
            }

            // Center Vault Knob
            drawCircle(
                color = CineCyan,
                center = center,
                radius = dialRadius * 0.38f
            )
        }
    }
}

@Composable
fun CineVaultTopBar(
    onOpenGlobalSearch: () -> Unit,
    onOpenStats: () -> Unit,
    onAddNewMovie: () -> Unit,
    modifier: Modifier = Modifier,
    currentUser: FirebaseUser? = null,
    isSyncing: Boolean = false,
    onOpenAccount: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .testTag("cinevault_top_bar")
            .fillMaxWidth(),
        color = CineDarkBg,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenAccount() }
            ) {
                CineVaultLogo(sizeDp = 38)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CINE",
                            color = CineTextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "VAULT",
                            color = CineGold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentUser != null) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isSyncing) CineGold else CineGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSyncing) "Syncing..." else "Cloud Active",
                                color = if (isSyncing) CineGold else CineGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "Global Entertainment",
                                color = CineGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Account / Cloud Sync Button
                IconButton(
                    onClick = onOpenAccount,
                    modifier = Modifier
                        .testTag("account_sync_top_button")
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (currentUser != null) CineSurfaceVariant else CineSurface)
                        .border(
                            1.dp,
                            if (currentUser != null) CineGreen.copy(alpha = 0.5f) else Color.Transparent,
                            CircleShape
                        )
                ) {
                    if (currentUser != null) {
                        Text(
                            text = (currentUser.displayName ?: currentUser.email ?: "U").take(1).uppercase(),
                            color = CineGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Cloud Backup & Sync",
                            tint = CineGold,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onOpenGlobalSearch,
                    modifier = Modifier
                        .testTag("global_search_top_button")
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CineSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Global Entertainment Search",
                        tint = CineGold,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onOpenStats,
                    modifier = Modifier
                        .testTag("stats_icon_button")
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CineSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Collection Statistics",
                        tint = CineCyan,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onAddNewMovie,
                    modifier = Modifier
                        .testTag("add_movie_top_button")
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CineGold)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Movie",
                        tint = CineDarkBg,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

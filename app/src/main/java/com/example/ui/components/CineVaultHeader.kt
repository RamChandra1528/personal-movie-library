package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.CineCardBorder
import com.example.ui.theme.CineGreen
import com.example.ui.theme.CineSurfaceVariant
import com.google.firebase.auth.FirebaseUser
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
 * Official CineVault / Personal Movie Library Badge Logo.
 * Displays the user's custom cinema reel and clapperboard badge.
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
            .clip(CircleShape)
            .background(Color(0xFF10141E))
            .border(
                1.5.dp,
                Brush.linearGradient(listOf(CineGold, CineGoldBright)),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "Personal Movie Library Logo",
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * CineVault Top Bar:
 * Keeps the official Personal Movie Library logo and branding in its current top position.
 */
@Composable
fun CineVaultTopBar(
    modifier: Modifier = Modifier,
    currentUser: FirebaseUser? = null,
    isSyncing: Boolean = false,
    onOpenAccount: () -> Unit = {},
    // Legacy parameters for backward compatibility
    onOpenGlobalSearch: () -> Unit = {},
    onOpenStats: () -> Unit = {},
    onAddNewMovie: () -> Unit = {}
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenAccount() }
            ) {
                CineVaultLogo(sizeDp = 42)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CINE",
                            color = CineTextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "VAULT",
                            color = CineGold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
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
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isSyncing) "Syncing..." else "Cloud Active (${currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "User"})",
                                color = if (isSyncing) CineGold else CineGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "Personal Movie Library",
                                color = CineGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * CineVault Bottom Bar:
 * Houses the other header elements (Account / Cloud Sync, Global Search, Stats, and Add Movie)
 * at the bottom of the screen, preserving their original functionality, styling, and test tags.
 */
@Composable
fun CineVaultBottomBar(
    onOpenGlobalSearch: () -> Unit,
    onOpenStats: () -> Unit,
    onAddNewMovie: () -> Unit,
    onOpenAccount: () -> Unit,
    modifier: Modifier = Modifier,
    currentUser: FirebaseUser? = null,
    isSyncing: Boolean = false
) {
    Surface(
        modifier = modifier
            .testTag("cinevault_bottom_bar")
            .fillMaxWidth(),
        color = CineDarkBg,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, CineCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // 1. Account / Cloud Sync Button
            IconButton(
                onClick = onOpenAccount,
                modifier = Modifier
                    .testTag("account_sync_top_button")
                    .size(42.dp)
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
                        fontSize = 15.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Cloud Backup & Sync",
                        tint = CineGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. Global Search Button
            IconButton(
                onClick = onOpenGlobalSearch,
                modifier = Modifier
                    .testTag("global_search_top_button")
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(CineSurface)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Global Entertainment Search",
                    tint = CineGold,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 3. Stats Button
            IconButton(
                onClick = onOpenStats,
                modifier = Modifier
                    .testTag("stats_icon_button")
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(CineSurface)
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Collection Statistics",
                    tint = CineCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 4. Add Movie Button
            IconButton(
                onClick = onAddNewMovie,
                modifier = Modifier
                    .testTag("add_movie_top_button")
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(CineGold)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Movie",
                    tint = CineDarkBg,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

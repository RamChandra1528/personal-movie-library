package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.ui.theme.CineGreen
import com.example.ui.theme.CineSurfaceVariant
import com.google.firebase.auth.FirebaseUser
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.example.R
import com.example.ui.Screen

/** Compact version of the Personal Movie Library reel, clapper, and play-mark logo. */
@Composable
fun CineVaultLogo(
    modifier: Modifier = Modifier,
    sizeDp: Int = 40
) {
    Image(
        modifier = modifier
            .testTag("cinevault_logo")
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                1.dp,
                CineGold,
                RoundedCornerShape(10.dp)
            ),
        painter = painterResource(R.drawable.ic_personal_movie_library_logo),
        contentDescription = "Personal Movie Library logo"
    )
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
                    Column {
                        Text(
                            text = "PERSONAL",
                            color = CineTextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "MOVIE LIBRARY",
                            color = CineGold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
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

/**
 * Primary app navigation. All actions that were formerly in the dashboard header
 * live here so they stay reachable on real devices while scrolling any screen.
 */
@Composable
fun CineVaultBottomBar(
    currentScreen: Screen,
    onOpenVault: () -> Unit,
    onOpenGlobalSearch: () -> Unit,
    onOpenStats: () -> Unit,
    onAddNewMovie: () -> Unit,
    currentUser: FirebaseUser?,
    isSyncing: Boolean,
    onOpenAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("cinevault_bottom_bar"),
        containerColor = CineSurface,
        contentColor = CineGold,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.DASHBOARD,
            onClick = onOpenVault,
            icon = { CineVaultLogo(sizeDp = 27) },
            label = { Text("Vault", fontSize = 10.sp) },
            colors = bottomBarItemColors()
        )
        NavigationBarItem(
            selected = currentScreen == Screen.GLOBAL_SEARCH,
            onClick = onOpenGlobalSearch,
            icon = { Icon(Icons.Default.Search, contentDescription = "Global search") },
            label = { Text("Discover", fontSize = 10.sp) },
            colors = bottomBarItemColors()
        )
        NavigationBarItem(
            selected = currentScreen == Screen.STATS,
            onClick = onOpenStats,
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Collection statistics") },
            label = { Text("Stats", fontSize = 10.sp) },
            colors = bottomBarItemColors()
        )
        NavigationBarItem(
            selected = false,
            onClick = onAddNewMovie,
            icon = { Icon(Icons.Default.Add, contentDescription = "Add movie") },
            label = { Text("Add", fontSize = 10.sp) },
            colors = bottomBarItemColors()
        )
        NavigationBarItem(
            selected = false,
            onClick = onOpenAccount,
            icon = {
                if (currentUser != null) {
                    Icon(
                        if (isSyncing) Icons.Default.CloudSync else Icons.Default.CloudDone,
                        contentDescription = "Cloud backup and account"
                    )
                } else {
                    Icon(Icons.Default.CloudSync, contentDescription = "Cloud backup and account")
                }
            },
            label = { Text(if (currentUser != null) "Cloud" else "Sign in", fontSize = 10.sp) },
            colors = bottomBarItemColors()
        )
    }
}

@Composable
private fun bottomBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = CineDarkBg,
    selectedTextColor = CineGold,
    indicatorColor = CineGold,
    unselectedIconColor = CineTextSecondary,
    unselectedTextColor = CineTextSecondary
)

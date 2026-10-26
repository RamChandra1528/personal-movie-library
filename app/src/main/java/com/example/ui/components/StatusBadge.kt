package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WatchStatus
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineGreen
import com.example.ui.theme.CineRed

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val (bg, fg, icon, label) = when (status) {
        WatchStatus.COMPLETED -> Quad(
            CineGreen.copy(alpha = 0.18f),
            CineGreen,
            Icons.Default.CheckCircle,
            "Completed"
        )
        WatchStatus.WATCHING -> Quad(
            CineCyan.copy(alpha = 0.18f),
            CineCyan,
            Icons.Default.PlayArrow,
            "Watching"
        )
        WatchStatus.PLAN_TO_WATCH -> Quad(
            CineGold.copy(alpha = 0.18f),
            CineGold,
            Icons.Default.Bookmark,
            "Plan to Watch"
        )
        WatchStatus.DROPPED -> Quad(
            CineRed.copy(alpha = 0.18f),
            CineRed,
            Icons.Default.Close,
            "Dropped"
        )
        else -> Quad(
            Color.White.copy(alpha = 0.12f),
            Color.White,
            Icons.Default.Bookmark,
            status
        )
    }

    Box(
        modifier = modifier
            .testTag("status_badge_${status.lowercase()}")
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = if (compact) 6.dp else 10.dp, vertical = if (compact) 3.dp else 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(if (compact) 12.dp else 14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (compact && status == WatchStatus.PLAN_TO_WATCH) "Plan" else label,
                color = fg,
                fontSize = if (compact) 10.sp else 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

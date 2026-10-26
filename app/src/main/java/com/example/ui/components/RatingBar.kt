package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineStarEmpty
import com.example.ui.theme.CineStarGold
import com.example.ui.theme.CineTextSecondary

@Composable
fun RatingStars(
    rating: Float,
    maxRating: Int = 5,
    starSize: Dp = 16.dp,
    interactive: Boolean = false,
    onRatingChanged: ((Float) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.testTag("rating_stars_row"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxRating) {
            val filled = rating >= i
            val half = !filled && rating >= (i - 0.5f)

            val icon = when {
                filled -> Icons.Default.Star
                half -> Icons.Default.StarHalf
                else -> Icons.Default.StarBorder
            }

            val tint = if (filled || half) CineStarGold else CineStarEmpty

            val clickableModifier = if (interactive && onRatingChanged != null) {
                Modifier
                    .size(starSize + 12.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Toggle to 0 if tapping same star
                        if (rating == i.toFloat()) {
                            onRatingChanged(0f)
                        } else {
                            onRatingChanged(i.toFloat())
                        }
                    }
                    .padding(2.dp)
            } else {
                Modifier.size(starSize)
            }

            Box(
                modifier = clickableModifier,
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Star $i",
                    tint = tint,
                    modifier = Modifier.size(starSize)
                )
            }
        }
    }
}

@Composable
fun ExternalRatingBadge(
    rating: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag("external_rating_badge"),
        shape = RoundedCornerShape(6.dp),
        color = CineStarGold.copy(alpha = 0.16f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = CineStarGold,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = rating,
                color = CineGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

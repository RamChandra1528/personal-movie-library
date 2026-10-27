package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CineCyan
import com.example.ui.theme.CineDarkBg
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineGoldBright
import com.example.ui.theme.CineTextPrimary
import com.example.ui.theme.CineTextSecondary

/**
 * Premium cinematic loading screen for CineVault / Personal Movie Library.
 * Displays the app logo in the center with a rotating circular gradient spinner
 * and a breathing/hover glow animation while authentication and cloud sync initialize.
 */
@Composable
fun AppLoadingScreen(
    statusMessage: String = "Initializing cinema vault..."
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_anims")

    // Rotation angle for the spinner ring around the logo
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinner_rotation"
    )

    // Reverse rotation for a secondary subtle counter-ring
    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_rotation"
    )

    // Breathing / Hover scale animation for the logo
    val hoverScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_hover_scale"
    )

    // Pulsing glow alpha
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(
        modifier = Modifier
            .testTag("app_loading_screen")
            .fillMaxSize()
            .background(CineDarkBg),
        contentAlignment = Alignment.Center
    ) {
        // Background radial glow behind center
        Canvas(
            modifier = Modifier
                .size(320.dp)
                .scale(hoverScale)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CineGold.copy(alpha = glowAlpha * 0.22f),
                        CineCyan.copy(alpha = glowAlpha * 0.08f),
                        Color.Transparent
                    ),
                    radius = size.minDimension / 1.6f
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Central Logo Container with Outer Rotating Circular Spinner
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Rotating Circular Spinner Ring (Forward)
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val strokeWidth = 3.5.dp.toPx()
                    val diameter = size.minDimension - strokeWidth * 2

                    // Subtle background track
                    drawCircle(
                        color = Color(0xFF222938),
                        radius = diameter / 2f,
                        style = Stroke(width = strokeWidth)
                    )

                    // Primary sweeping spinner arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color.Transparent,
                                CineCyan.copy(alpha = 0.4f),
                                CineGoldBright,
                                CineGold
                            )
                        ),
                        startAngle = rotationAngle,
                        sweepAngle = 260f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner Counter-Rotating Accent Dash Ring
                Canvas(
                    modifier = Modifier.size(126.dp)
                ) {
                    val innerStroke = 1.5.dp.toPx()
                    drawArc(
                        color = CineCyan.copy(alpha = glowAlpha * 0.7f),
                        startAngle = counterRotation,
                        sweepAngle = 80f,
                        useCenter = false,
                        style = Stroke(width = innerStroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = CineGold.copy(alpha = glowAlpha * 0.7f),
                        startAngle = counterRotation + 180f,
                        sweepAngle = 80f,
                        useCenter = false,
                        style = Stroke(width = innerStroke, cap = StrokeCap.Round)
                    )
                }

                // Center App Logo with Subtle Breathing / Hover Glow
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .scale(hoverScale)
                        .clip(CircleShape)
                        .background(Color(0xFF0F141F))
                        .border(
                            2.dp,
                            Brush.linearGradient(
                                listOf(CineGoldBright, CineGold.copy(alpha = 0.6f))
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "CineVault Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Brand Title
            Text(
                text = "CINEVAULT",
                color = CineGold,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
                text = "PERSONAL MOVIE LIBRARY",
                color = CineTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Status Text
            Text(
                text = statusMessage,
                color = CineTextPrimary.copy(alpha = 0.8f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

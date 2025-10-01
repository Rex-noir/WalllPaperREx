package com.ace.wallpaperrex.ui.components.wallpaper

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

fun Modifier.shimmerBackground(shape: RoundedCornerShape = RoundedCornerShape(0.dp)): Modifier =
    composed {
        val transition = rememberInfiniteTransition(label = "shimmerTransition")
        val translateAnimation by transition.animateFloat(
            initialValue = 0f,
            targetValue = 400f, // Adjust this value based on the width of your items
            animationSpec = infiniteRepeatable(
                tween(durationMillis = 1500), // Slower shimmer
                RepeatMode.Restart
            ),
            label = "shimmerTranslate"
        )

        val shimmerColors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // Lighter placeholder
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), // Darker part of shimmer
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )

        this.background(
            brush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(translateAnimation - 200f, translateAnimation - 200f),
                end = Offset(translateAnimation, translateAnimation)
            ),
            shape = shape
        )
    }

@Composable
fun SkeletonGridItem(
    modifier: Modifier = Modifier,
    // Provide a list of possible aspect ratios
    possibleAspectRatios: List<Float> = listOf(0.6f, 0.75f, 1f, 1.25f, 1.5f)
) {
    // Remember a random aspect ratio for this specific item instance
    val randomAspectRatio = remember { possibleAspectRatios.randomOrNull() ?: (3f / 4f) }

    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(randomAspectRatio)
            .clip(RoundedCornerShape(size = 0.dp)) // Same shape as your GridImageItem
            .shimmerBackground()
    )
}

@Preview(showBackground = true)
@Composable
fun SkeletonGridItemPreview() {
    MaterialTheme {
        SkeletonGridItem(modifier = Modifier.height(200.dp)) // Fixed height for preview clarity
    }
}

package com.ace.wallpaperrex.ui.components.wallpaper

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import shimmer

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
            .shimmer(visible = true)
    )
}

@Preview(showBackground = true)
@Composable
fun SkeletonGridItemPreview() {
    MaterialTheme {
        SkeletonGridItem(modifier = Modifier.height(200.dp)) // Fixed height for preview clarity
    }
}

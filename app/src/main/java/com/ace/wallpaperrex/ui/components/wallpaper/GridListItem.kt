package com.ace.wallpaperrex.ui.components.wallpaper

import Picture
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.ace.wallpaperrex.ui.models.ImageItem


@Composable
fun GridImageItem(item: ImageItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Picture(
        model = item.thumbnail,
        contentDescription = item.description,
        contentScale = ContentScale.Crop,
        shimmerEnabled = false,
        shape = RectangleShape,
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(item.aspectRatio.takeIf { it > 0 } ?: (3f / 4f))
            .clip(RoundedCornerShape(size = 0.dp))
            .background(item.placeHolderColor ?: Color.Gray)
            .clickable(onClick = onClick)
    )
}


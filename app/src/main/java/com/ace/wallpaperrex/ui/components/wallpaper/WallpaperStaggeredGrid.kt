package com.ace.wallpaperrex.ui.components.wallpaper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ace.wallpaperrex.ui.models.ImageItem

@Composable
fun WallpaperStaggeredGrid(
    items: List<ImageItem>,
    isLoadingMore: Boolean,
    isEndOfList: Boolean,
    error: String?,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    onWallpaperClick: (image: ImageItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty() && !isLoadingMore && error == null) {
        EmptyState(
            modifier = modifier.fillMaxSize()
        )
    } else if (isLoadingMore && items.isEmpty()) {
        SkeletonWallpaperGrid(modifier = modifier)
    } else if (!isLoadingMore && error != null) {
        ErrorState(
            message = error,
            onRetry = onRetryLoadMore,
            modifier = modifier.fillMaxWidth()
        )
    } else LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 150.dp),
        modifier = modifier // Apply modifier here
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> item.id }
        ) { index, item ->
            GridImageItem(item = item, onClick = { onWallpaperClick(item) })

            val loadMoreThreshold = 5
            if (!isLoadingMore && error == null && !isEndOfList && index >= items.size - loadMoreThreshold) {
                LaunchedEffect(key1 = items.size) {
                    onLoadMore()
                }
            }

        }

        if (isLoadingMore && error == null && items.isNotEmpty()) {
            items(4) {
                SkeletonGridItem()
            }
        }
        if (error != null) {
            item(
                span = StaggeredGridItemSpan.Companion.FullLine
            ) {
                Column(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Companion.CenterHorizontally
                ) {
                    Text(
                        "Error loading more : $error",
                        color = MaterialTheme.colorScheme.error
                    )

                    Button(onClick = onRetryLoadMore) {
                        Text("Retry")
                    }
                }
            }
        }

    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "Wow. So Empty!",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(16.dp), // Add padding for better spacing from edges
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            // Use Arrangement.spacedBy for consistent vertical spacing
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Add a relevant icon for a network or general error
            Icon(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = null, // The text describes the state, so icon is decorative
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            // Use a more prominent headline style for the message
            Text(
                text = message,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            // The retry button remains a clear call to action
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}

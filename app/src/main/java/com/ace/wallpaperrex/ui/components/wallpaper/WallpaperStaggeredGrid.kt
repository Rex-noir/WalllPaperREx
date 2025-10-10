package com.ace.wallpaperrex.ui.components.wallpaper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ace.wallpaperrex.ui.models.ImageItem

@Composable
fun WallpaperStaggeredGrid(
    items: List<ImageItem>,
    isLoadingMore: Boolean,
    isEndOfList: Boolean,
    paginationError: String?,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    onWallpaperClick: (image: ImageItem) -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 150.dp),
        modifier = modifier // Apply modifier here
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> item.id }
        ) { index, item ->
            GridImageItem(item = item, onClick = { onWallpaperClick(item) })

            val loadMoreThreshold = 5
            if (!isLoadingMore && paginationError == null && !isEndOfList && index >= items.size - loadMoreThreshold) {
                LaunchedEffect(key1 = items.size) {
                    onLoadMore()
                }
            }

        }

        if (isLoadingMore && paginationError == null) {
            items(4) {
                SkeletonGridItem()
            }
        }

        if (paginationError != null) {
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
                        "Error loading more : $paginationError",
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
package com.ace.wallpaperrex.ui.screens.wallpapers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ace.wallpaperrex.ui.components.wallpaper.GridImageItem
import com.ace.wallpaperrex.ui.components.wallpaper.SkeletonGridItem
import com.ace.wallpaperrex.ui.models.ImageItem

@Composable
fun WallpaperListScreen(
    modifier: Modifier = Modifier,
    viewModel: WallPaperListViewModel,
    onWallpaperClick: (image: ImageItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading && uiState.items.isEmpty()) {
            SkeletonWallpaperGrid(modifier = Modifier.fillMaxSize())
        } else if (uiState.error != null && uiState.items.isEmpty()) {
            ErrorState(
                message = uiState.error!!,
                onRetry = { viewModel.retryInitialLoad() },
                modifier = Modifier.fillMaxSize(),
            )
        } else if (uiState.items.isEmpty()) {
            EmptyState(
                message = "No wallpapers found for '${uiState.currentQuery}'.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            WallpaperStaggeredGrid(
                items = uiState.items,
                isLoadingMore = uiState.isLoading,
                isEndOfList = uiState.isEndOfList,
                paginationError = uiState.error,
                onLoadMore = { viewModel.loadNextPage() },
                onRetryLoadMore = { viewModel.loadNextPage() },
                onWallpaperClick = onWallpaperClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
fun SkeletonWallpaperGrid(
    modifier: Modifier = Modifier,
    itemCount: Int = 12 // Number of skeleton items to show
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 150.dp),
    ) {
        items(itemCount) {
            SkeletonGridItem() // Uses its internal random aspect ratio
        }
    }
}


@Composable
fun WallpaperStaggeredGrid(
    items: List<ImageItem>,
    isLoadingMore: Boolean,
    isEndOfList: Boolean,
    paginationError: String?,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    onWallpaperClick: (image: ImageItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 150.dp),
    ) {
        itemsIndexed(
            items = items,
            key = { index: Int, item: ImageItem -> item.id }) { index, item ->
            GridImageItem(item = item, onClick = { onWallpaperClick(item) })

            val loadMoreThreshold = 5
            if (!isLoadingMore && !isEndOfList && index >= items.size - loadMoreThreshold) {
                LaunchedEffect(key1 = items.size) {
                    onLoadMore()
                }
            }

        }

        if (isLoadingMore) {
            // Show a few skeleton items at the end for pagination loading
            // You can make these span the full width if you wrap them in a Box
            // and use item(span = StaggeredGridItemSpan.FullLine)
            // For now, let's add a few individual skeleton items.
            items(4) { // Show 3 skeleton items while loading more
                SkeletonGridItem()
            }
            // Alternatively, for a full-width single shimmer bar:
            // item(span = StaggeredGridItemSpan.FullLine) {
            //     Spacer(
            //         modifier = Modifier
            //             .fillMaxWidth()
            //             .height(80.dp) // Or some other appropriate height
            //             .padding(vertical = 8.dp)
            //             .shimmerBackground(RoundedCornerShape(4.dp))
            //     )
            // }
        }

        if (paginationError != null) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
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


@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}


@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}
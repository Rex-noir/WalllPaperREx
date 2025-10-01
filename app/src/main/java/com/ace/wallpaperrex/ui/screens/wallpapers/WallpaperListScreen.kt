package com.ace.wallpaperrex.ui.screens.wallpapers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ace.wallpaperrex.data.ImageItem
import com.ace.wallpaperrex.ui.components.wallpaper.GridImageItem

@Composable
fun WallpaperListScreen(
    modifier: Modifier = Modifier,
    viewModel: WallPaperListViewModel,
    onWallpaperClick: (wallpaperId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading && uiState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
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
fun WallpaperStaggeredGrid(
    items: List<ImageItem>,
    isLoadingMore: Boolean,
    isEndOfList: Boolean,
    paginationError: String?,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    onWallpaperClick: (wallpaperId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 150.dp),
    ) {
        itemsIndexed(
            items = items,
            key = { index: Int, item: ImageItem -> item.id }) { index, item ->
            GridImageItem(item = item, onClick = { onWallpaperClick(item.id) })

            val loadMoreThreshold = 5
            if (!isLoadingMore && !isEndOfList && index >= items.size - loadMoreThreshold) {
                LaunchedEffect(key1 = items.size) {
                    onLoadMore()
                }
            }

        }


        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
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
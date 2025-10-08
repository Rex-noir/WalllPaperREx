package com.ace.wallpaperrex.ui.screens.wallpapers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ace.wallpaperrex.data.daos.getLastWallpaperSource
import com.ace.wallpaperrex.data.daos.getWallpaperSourcesFlow
import com.ace.wallpaperrex.data.daos.setLastWallpaperSourceId
import com.ace.wallpaperrex.ui.components.wallpaper.GridImageItem
import com.ace.wallpaperrex.ui.components.wallpaper.SkeletonGridItem
import com.ace.wallpaperrex.ui.models.ImageItem
import kotlinx.coroutines.launch

@Composable
fun WallpaperListScreen(
    modifier: Modifier = Modifier,
    viewModel: WallPaperListViewModel,
    onWallpaperClick: (image: ImageItem) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedSource by context.getLastWallpaperSource()
        .collectAsStateWithLifecycle(initialValue = null)
    val allWallpaperSources by context.getWallpaperSourcesFlow()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val wallpaperSources by remember(allWallpaperSources) {
        derivedStateOf {
            allWallpaperSources.filter { it.isConfigured }
        }
    }

    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {


        if (wallpaperSources.isNotEmpty() && wallpaperSources.size > 1) {
            val selectedIndex = wallpaperSources.indexOf(selectedSource).coerceAtLeast(0)

            SecondaryTabRow(
                selectedTabIndex = selectedIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                wallpaperSources.forEach { source ->
                    Tab(
                        selected = source.id == selectedSource?.id,
                        onClick = {
                            if (source.id != selectedSource?.id) {
                                scope.launch {
                                    context.setLastWallpaperSourceId(source.id)
                                }
                            }
                        },
                        text = {
                            Text(text = source.name)
                        }
                    )
                }
            }
        }

        // --- FIX 3: Content area with its own logic ---
        // The Box takes up the remaining available space.
        Box(modifier = Modifier.weight(1f)) {
            // This logic now only controls the content area, not the whole screen.
            if (uiState.isLoading && uiState.items.isEmpty()) {
                SkeletonWallpaperGrid(modifier = Modifier.fillMaxSize())
            } else if (uiState.error != null && uiState.items.isEmpty()) {
                ErrorState(
                    message = uiState.error!!, // No need for '!!'
                    onRetry = { viewModel.retryInitialLoad() },
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (uiState.items.isEmpty()) {
                EmptyState(
                    message = "No wallpapers found for '${uiState.currentQuery}'.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // The grid is only shown when there are items.
                WallpaperStaggeredGrid(
                    items = uiState.items,
                    isLoadingMore = uiState.isLoading,
                    isEndOfList = uiState.isEndOfList,
                    paginationError = uiState.error,
                    onLoadMore = { viewModel.loadNextPage() },
                    onRetryLoadMore = { viewModel.loadNextPage() },
                    onWallpaperClick = onWallpaperClick,
                    // This modifier is important for the grid to fill the Box
                    modifier = Modifier.fillMaxSize()
                )
            }
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
        modifier = modifier // Apply modifier here
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
        modifier = modifier // Apply modifier here
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> item.id }
        ) { index, item ->
            GridImageItem(item = item, onClick = { onWallpaperClick(item) })

            val loadMoreThreshold = 5
            if (!isLoadingMore && !isEndOfList && index >= items.size - loadMoreThreshold) {
                LaunchedEffect(key1 = items.size) {
                    onLoadMore()
                }
            }

        }

        if (isLoadingMore) {
            items(4) {
                SkeletonGridItem()
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

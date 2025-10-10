package com.ace.wallpaperrex.ui.components.wallpaper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ace.wallpaperrex.ui.models.ImageItem

@Composable
fun WallpaperSourceList(
    modifier: Modifier = Modifier,
    viewModel: WallpaperSourceListViewModel,
    onWallpaperClick: (image: ImageItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
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
                    message = "Looks like there are no wallpapers from this source.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                WallpaperStaggeredGrid(
                    items = uiState.items,
                    isLoadingMore = uiState.isLoading,
                    isEndOfList = uiState.isEndOfList,
                    error = uiState.error,
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


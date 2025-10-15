package com.ace.wallpaperrex.ui.screens.wallpapers

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import com.ace.wallpaperrex.data.repositories.WallpaperSourceRepository
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperStaggeredGrid
import com.ace.wallpaperrex.ui.models.ImageItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WallpaperListScreen(
    onWallpaperClick: (ImageItem, WallpaperSourceConfigItem) -> Unit,
    wallpaperSourceRepository: WallpaperSourceRepository
) {
    val scope = rememberCoroutineScope()

    val allWallpaperSources by wallpaperSourceRepository.wallpaperSources
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val wallpaperSources by remember(allWallpaperSources) {
        derivedStateOf {
            allWallpaperSources.filter { it.isConfigured }
        }
    }
    var initialSource by remember { mutableStateOf<WallpaperSourceConfigItem?>(null) }

    LaunchedEffect(wallpaperSources) {
        // We only proceed if the list of sources is actually loaded.
        if (wallpaperSources.isNotEmpty()) {
            // Asynchronously get the first emitted value from the Flow.
            val lastSource = wallpaperSourceRepository.lastWallpaperSource.first()

            initialSource = // Case 1: A "last used" source was found, so we use it.
                lastSource
                    ?: // Case 2: No "last used" source, so we fall back to the first one in the list.
                            wallpaperSources.first()
        }
    }

    if (wallpaperSources.isNotEmpty() && initialSource != null) {
        val initialPage = remember(initialSource, wallpaperSources) {
            wallpaperSources.indexOf(initialSource).coerceAtLeast(0)
        }

        val pagerState = rememberPagerState(
            initialPage = initialPage,
            pageCount = { wallpaperSources.size }
        )

        Column(modifier = Modifier.fillMaxSize()) {
            if (wallpaperSources.size > 1) {
                PrimaryScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier.fillMaxWidth(),
                    edgePadding = 0.dp
                ) {
                    wallpaperSources.forEachIndexed { index, source ->
                        Tab(
                            selected = index == pagerState.currentPage,
                            onClick = {
                                // 3. Clicking a tab smoothly animates the pager to that page
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(text = source.label) }
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                key = { pageIndex -> wallpaperSources[pageIndex].uniqueKey }
            ) { pageIndex ->
                val source = wallpaperSources[pageIndex]
                val viewModel: WallpaperListViewModel = viewModel(
                    factory = WallpaperListViewModel.createFactory(
                        source.uniqueKey,
                        wallpaperSourceRepository
                    ),
                    key = "source-${source.uniqueKey}"
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                WallpaperStaggeredGrid(
                    items = uiState.items,
                    isLoadingMore = uiState.isLoading,
                    isEndOfList = uiState.isEndOfList,
                    error = uiState.error,
                    onLoadMore = { viewModel.loadNextPage() },
                    onRetryLoadMore = { viewModel.retry() },
                    onWallpaperClick = { image ->
                        onWallpaperClick(image, source)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 5. Persist the last viewed page's source ID whenever the page changes
            LaunchedEffect(pagerState.currentPage) {
                if (wallpaperSources.isNotEmpty()) {
                    val currentSourceKey = wallpaperSources[pagerState.currentPage].uniqueKey
                    wallpaperSourceRepository.setLastWallpaperSource(currentSourceKey)
                }
            }
        }
    }
}

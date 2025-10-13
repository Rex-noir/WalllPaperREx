package com.ace.wallpaperrex.ui.screens.wallpapers

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ace.wallpaperrex.data.daos.getLastWallpaperSource
import com.ace.wallpaperrex.data.daos.getWallpaperSourcesFlow
import com.ace.wallpaperrex.data.daos.setLastWallpaperSourceId
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperStaggeredGrid
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WallpaperListScreen(
    onWallpaperClick: (ImageItem) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val allWallpaperSources by context.getWallpaperSourcesFlow()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val wallpaperSources by remember(allWallpaperSources) {
        derivedStateOf {
            allWallpaperSources.filter { it.isConfigured }
        }
    }

    // Determine the initial page based on the last saved source
    val initialSource: WallpaperSourceConfigItem? = remember(wallpaperSources) {
        if (wallpaperSources.isNotEmpty()) {
            runBlocking { context.getLastWallpaperSource().first() }
                ?: wallpaperSources.first()
        } else {
            null
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
                SecondaryTabRow(
                    // 2. The selected tab is now driven by the pager's current page
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier.fillMaxWidth()
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
                            text = { Text(text = source.name) }
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                key = { pageIndex -> wallpaperSources[pageIndex].id }
            ) { pageIndex ->
                val source = wallpaperSources[pageIndex]
                val viewModel: WallpaperListViewModel = viewModel(
                    factory = WallpaperListViewModel.createFactory(source.id),
                    key = "source-${source.id}"
                )
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                WallpaperStaggeredGrid(
                    items = uiState.items,
                    isLoadingMore = uiState.isLoading,
                    isEndOfList = uiState.isEndOfList,
                    error = uiState.error,
                    onLoadMore = { viewModel.loadNextPage() },
                    onRetryLoadMore = { viewModel.loadNextPage() },
                    onWallpaperClick = onWallpaperClick,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 5. Persist the last viewed page's source ID whenever the page changes
            LaunchedEffect(pagerState.currentPage) {
                if (wallpaperSources.isNotEmpty()) {
                    val currentSourceId = wallpaperSources[pagerState.currentPage].id
                    context.setLastWallpaperSourceId(currentSourceId)
                }
            }
        }
    }
}

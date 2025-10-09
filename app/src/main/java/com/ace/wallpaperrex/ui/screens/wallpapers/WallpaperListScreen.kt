package com.ace.wallpaperrex.ui.screens.wallpapers

// Import createGraph
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigation.toRoute
import com.ace.wallpaperrex.data.daos.getLastWallpaperSource
import com.ace.wallpaperrex.data.daos.getWallpaperSourcesFlow
import com.ace.wallpaperrex.data.daos.setLastWallpaperSourceId
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperSourceList
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperSourceListViewModel
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.WallpaperSourceItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
data class WallpaperSingleListRoute(val sourceId: Int)

@Composable
fun WallpaperListScreen(
    onWallpaperClick: (ImageItem) -> Unit
) {
    val context = LocalContext.current
    val tabNavController = rememberNavController()

    val allWallpaperSources by context.getWallpaperSourcesFlow()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val wallpaperSources by remember(allWallpaperSources) {
        derivedStateOf {
            allWallpaperSources.filter { it.isConfigured }
        }
    }
    val scope = rememberCoroutineScope()

    val initialSource: WallpaperSourceItem? = remember(wallpaperSources) {
        if (wallpaperSources.isNotEmpty()) {
            runBlocking { context.getLastWallpaperSource().first() }
                ?: wallpaperSources.first()
        } else {
            null
        }
    }

    if (wallpaperSources.isNotEmpty() && initialSource != null) {
        val navGraph = remember(tabNavController, initialSource, wallpaperSources) {
            tabNavController.createGraph(
                startDestination = WallpaperSingleListRoute(initialSource.id),
            ) {
                wallpaperSources.forEach { source ->
                    composable<WallpaperSingleListRoute> { backStackEntry ->
                        val currentId = backStackEntry.toRoute<WallpaperSingleListRoute>().sourceId
                        key(currentId) {
                            WallpaperSourceList(
                                viewModel = viewModel(
                                    factory = WallpaperSourceListViewModel.createFactory(
                                        currentId,
                                    ),
                                    key = "source-$currentId",
                                    viewModelStoreOwner = backStackEntry
                                ),
                                onWallpaperClick = onWallpaperClick,
                            )
                        }
                    }
                }
            }
        }


        Column(modifier = Modifier.fillMaxSize()) {
            val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
            val currentDestinationId =
                navBackStackEntry?.toRoute<WallpaperSingleListRoute>()?.sourceId

            val selectedIndex = remember(currentDestinationId, wallpaperSources) {
                wallpaperSources.indexOfFirst { it.id == currentDestinationId }.coerceAtLeast(0)
            }

            if (wallpaperSources.size > 1) {
                SecondaryTabRow(
                    selectedTabIndex = selectedIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    wallpaperSources.forEachIndexed { index, source ->
                        Tab(
                            selected = index == selectedIndex,
                            onClick = {
                                if (selectedIndex != index) {
                                    tabNavController.navigate(WallpaperSingleListRoute(source.id)) {
                                        popUpTo(WallpaperSingleListRoute(source.id)) {
                                            saveState = true
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    scope.launch {
                                        context.setLastWallpaperSourceId(source.id)
                                    }
                                }
                            },
                            text = { Text(text = source.name) }
                        )
                    }
                }
            }

            NavHost(
                navController = tabNavController,
                graph = navGraph,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

package com.ace.wallpaperrex.ui.screens.wallpapers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ace.wallpaperrex.data.daos.getLastWallpaperSource
import com.ace.wallpaperrex.data.daos.getWallpaperSourcesFlow
import com.ace.wallpaperrex.data.daos.setLastWallpaperSourceId
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperSourceList
import com.ace.wallpaperrex.ui.models.ImageItem
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class WallpaperSingleListRoute(val sourceId: Int)

@Composable
fun WallpaperListScreen(
    onWallpaperClick: (ImageItem) -> Unit
) {
    val context = LocalContext.current
    val tabNavController = rememberNavController()
    val selectedSource by context.getLastWallpaperSource()
        .collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    val allWallpaperSources by context.getWallpaperSourcesFlow()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val wallpaperSources by remember(allWallpaperSources) {
        derivedStateOf {
            allWallpaperSources.filter { it.isConfigured }
        }
    }

    Column {
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
                                tabNavController.navigate(WallpaperSingleListRoute(source.id))
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
        NavHost(
            navController = tabNavController,
            startDestination = WallpaperSingleListRoute(sourceId = 1),
            modifier = Modifier.fillMaxSize()
        ) {
            composable<WallpaperSingleListRoute> { backStackEntry ->
                WallpaperSourceList(
                    onWallpaperClick = onWallpaperClick,
                )
            }
        }
    }


}
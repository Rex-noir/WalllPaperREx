package com.ace.wallpaperrex.ui.layouts

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ace.wallpaperrex.AppRoute
import com.ace.wallpaperrex.R
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperListTopAppBar
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.screens.setting.SettingsScreen
import com.ace.wallpaperrex.ui.screens.wallpapers.FavoriteListScreen
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperSourceListViewModel
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperSourceList
import com.ace.wallpaperrex.ui.screens.wallpapers.WallpaperListScreen
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

// 1. Define concrete serializable types for your destinations
@Serializable
data object WallpaperListRoute

@Serializable
data object SettingsRoute


@Serializable
data object FavoriteListRoute

// 2. Data structure to hold UI metadata (icon, title) for each route KClass
data class BottomNavigationItemInfo(
    val routeKClass: KClass<*>, // KClass of the @Serializable route data object/class
    val routeInstance: Any,     // The actual @Serializable route data object/class instance
    val icon: ImageVector,
    val titleResId: Int
)

// 3. List of navigation items with their metadata
val homeBottomNavItems: List<BottomNavigationItemInfo> = listOf(
    BottomNavigationItemInfo(
        routeKClass = WallpaperListRoute::class,
        routeInstance = WallpaperListRoute,
        icon = Icons.Filled.Wallpaper,
        titleResId = R.string.bottom_nav_home
    ),
    BottomNavigationItemInfo(
        routeKClass = FavoriteListRoute::class,
        routeInstance = FavoriteListRoute,
        icon = Icons.Filled.Favorite,
        titleResId = R.string.bottom_nav_favorites
    ),
    BottomNavigationItemInfo(
        routeKClass = SettingsRoute::class,
        routeInstance = SettingsRoute,
        icon = Icons.Filled.Settings,
        titleResId = R.string.bottom_nav_settings
    )

)

@Composable
fun AppBottomNavigationBar(
    homeNavController: NavHostController,
    currentNavDestination: NavDestination?
) {
    NavigationBar {
        homeBottomNavItems.forEach { navItemInfo ->
            val selected = currentNavDestination?.hasRoute(navItemInfo.routeKClass) == true
            NavigationBarItem(
                icon = { Icon(navItemInfo.icon, contentDescription = null) },
                label = { Text(stringResource(navItemInfo.titleResId)) },
                selected = selected,
                onClick = {
                    homeNavController.navigate(navItemInfo.routeInstance) { // Navigate to the serializable object instance
                        popUpTo(homeNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeLayout(
    modifier: Modifier = Modifier,
    appNavController: NavHostController, // For navigating outside HomeLayout's scope
    favoriteImageList: List<ImageItem>
) {
    val homeNavController = rememberNavController() // For navigation within HomeLayout
    val currentHomeBackStackEntry by homeNavController.currentBackStackEntryAsState()
    val currentNavDestination = currentHomeBackStackEntry?.destination

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var searchQuery by remember { mutableStateOf("") }

    var isBottomBarVisible by remember { mutableStateOf(true) }
    val bottomBarHeight = 80.dp

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y


                if (delta < 0) {
                    isBottomBarVisible = false
                } else if (delta > 0) {
                    isBottomBarVisible = true
                }

                return Offset.Zero
            }
        }
    }

    val animatedBottomBarHeight by animateDpAsState(
        targetValue = if (isBottomBarVisible) bottomBarHeight else 0.dp,
        label = "bottomBarHeightAnimation"
    )
    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .nestedScroll(nestedScrollConnection),
        topBar = {
            when {
                currentNavDestination?.hasRoute<WallpaperListRoute>() == true -> {
//                    WallpaperListTopAppBar(
//                        query = searchQuery,
//                        onQueryChange = { searchQuery = it },
//                        onSearchSubmit = {
//                            // TODO
//                        },
//                        scrollBehavior = scrollBehavior,
//                        onClearClicked = { searchQuery = "" }
//                    )
                    null
                }

                else -> {
                    val currentNavItem = homeBottomNavItems.find {
                        currentNavDestination?.hasRoute(it.routeKClass) == true
                    }

                    TopAppBar(
                        title = {
                            Text(
                                stringResource(
                                    // Use the item's title, or fallback to the app name
                                    currentNavItem?.titleResId ?: R.string.app_name
                                )
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ),
                        scrollBehavior = scrollBehavior
                    )
                }
            }
        },
        bottomBar = {
            Box(modifier = Modifier.height(animatedBottomBarHeight)) {
                AppBottomNavigationBar(
                    homeNavController = homeNavController,
                    currentNavDestination = currentNavDestination
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = homeNavController,
            startDestination = WallpaperListRoute, // Use the concrete @Serializable data object
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            composable<WallpaperListRoute> { // Use the concrete @Serializable data object
                WallpaperListScreen(
                    onWallpaperClick = { image ->
                        appNavController.navigate(AppRoute.WallpaperDetailRoute(image.id))
                    }
                )
            }
            composable<SettingsRoute> { // Use the concrete @Serializable data object
                SettingsScreen(modifier = Modifier.fillMaxSize())
            }
            composable<FavoriteListRoute> {
                FavoriteListScreen(
                    favorites = favoriteImageList,
                    onWallpaperClick = { image ->
                        Log.d("HomeLayout", "onWallpaperClick: $image")
                        appNavController.navigate(AppRoute.WallpaperDetailRoute(image.id))
                    }
                )
            }
        }
    }
}

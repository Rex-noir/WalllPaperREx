package com.ace.wallpaperrex.ui.layouts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.ace.wallpaperrex.AppRoute // Assuming this is for app-level navigation
import com.ace.wallpaperrex.R
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperListTopAppBar
import com.ace.wallpaperrex.ui.screens.setting.SettingsScreen
import com.ace.wallpaperrex.ui.screens.wallpapers.WallPaperListViewModel
import com.ace.wallpaperrex.ui.screens.wallpapers.WallpaperListScreen
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

// 1. Define concrete serializable types for your destinations
@Serializable
data object WallpaperListRoute

@Serializable
data object SettingsRoute

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
        icon = Icons.Filled.Home,
        titleResId = R.string.bottom_nav_home
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
    wallPaperListViewModelFromActivity: WallPaperListViewModel,
    appNavController: NavHostController // For navigating outside HomeLayout's scope
) {
    val homeNavController = rememberNavController() // For navigation within HomeLayout
    val currentHomeBackStackEntry by homeNavController.currentBackStackEntryAsState()
    val currentNavDestination = currentHomeBackStackEntry?.destination

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // Determine TopAppBar based on the current route type
            // You can also fetch the titleResId from homeBottomNavItems if needed
            when {
                currentNavDestination?.hasRoute<WallpaperListRoute>() == true -> {
                    WallpaperListTopAppBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearchSubmit = { wallPaperListViewModelFromActivity.searchWallpapers(it) },
                        scrollBehavior = scrollBehavior,
                        onClearClicked = { searchQuery = "" }
                    )
                }

                currentNavDestination?.hasRoute<SettingsRoute>() == true -> {
                    val settingsItemInfo =
                        homeBottomNavItems.find { it.routeKClass == SettingsRoute::class }
                    TopAppBar(
                        title = {
                            Text(
                                stringResource(
                                    settingsItemInfo?.titleResId ?: R.string.bottom_nav_settings
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

                else -> { // Fallback or initial state (e.g., when NavHost is first composed)
                    TopAppBar(
                        title = { Text(stringResource(R.string.app_name)) },
                        scrollBehavior = scrollBehavior
                    )
                }
            }
        },
        bottomBar = {
            AppBottomNavigationBar(
                homeNavController = homeNavController,
                currentNavDestination = currentNavDestination
            )
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
                    viewModel = wallPaperListViewModelFromActivity,
                    onWallpaperClick = { wallpaperId ->
                        // Use appNavController for navigation outside HomeLayout's NavHost
                        appNavController.navigate(AppRoute.WallpaperDetailRoute(wallpaperId))
                    }
                )
            }
            composable<SettingsRoute> { // Use the concrete @Serializable data object
                SettingsScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

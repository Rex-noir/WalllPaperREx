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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import com.ace.wallpaperrex.AppRoute // Assuming this is for app-level navigation
import com.ace.wallpaperrex.R
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperListTopAppBar
import com.ace.wallpaperrex.ui.screens.setting.SettingsScreen
import com.ace.wallpaperrex.ui.screens.wallpapers.WallPaperListViewModel
import com.ace.wallpaperrex.ui.screens.wallpapers.WallpaperListScreen
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

sealed interface HomeNavScreen { // Renamed for clarity, if you prefer
    val icon: ImageVector
    val titleResId: Int
    val route: KClass<*>
}

@Serializable
data object WallpaperListScreenRoute

@Serializable
data object SettingsScreenRoute

object WallpaperListNavItem : HomeNavScreen {
    override val icon: ImageVector
        get() = Icons.Filled.Home

    override val titleResId: Int
        get() = R.string.bottom_nav_home
    override val route = WallpaperListScreenRoute::class

}

object SettingsNavItem : HomeNavScreen {
    override val icon: ImageVector
        get() = Icons.Filled.Settings

    override val titleResId: Int
        get() = R.string.bottom_nav_settings
    override val route = SettingsScreenRoute::class

}

val homeBottomNavItems = listOf(WallpaperListNavItem, SettingsNavItem)

@Composable
fun AppBottomNavigationBar(
    homeNavController: NavHostController, // Specifically for navigating within HomeLayout
    currentScreen: HomeNavScreen? // Nullable in case the route is not yet available
) {
    if (currentScreen == null) return // Don't render if currentScreen is not determined

    NavigationBar {
        homeBottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(stringResource(screen.titleResId)) },
                selected = currentScreen == screen,
                onClick = {
                    homeNavController.navigate(screen) { // Navigate to the HomeNavScreen type
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(homeNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
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
    appNavController: NavHostController // This is for navigating outside of HomeLayout
) {
    val homeNavController = rememberNavController() // NavController for screens within HomeLayout
    val currentHomeBackStackEntry by homeNavController.currentBackStackEntryAsState()

    val currentRouteClass: KClass<*>? = when {
        runCatching { currentHomeBackStackEntry?.toRoute<WallpaperListScreenRoute>() }.isSuccess ->
            WallpaperListScreenRoute::class

        runCatching { currentHomeBackStackEntry?.toRoute<SettingsScreenRoute>() }.isSuccess ->
            SettingsScreenRoute::class

        else -> null
    }

    val currentHomeNavScreen = homeBottomNavItems.find { it.route == currentRouteClass }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // Conditionally display TopAppBar based on the current screen in HomeLayout
            when (currentHomeNavScreen) {
                is WallpaperListNavItem -> {
                    WallpaperListTopAppBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearchSubmit = { wallPaperListViewModelFromActivity.searchWallpapers(it) },
                        scrollBehavior = scrollBehavior,
                        onClearClicked = { searchQuery = "" }
                    )
                }

                is SettingsNavItem -> {
                    TopAppBar(
                        title = {
                            Text(stringResource(currentHomeNavScreen.titleResId))
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ),
                        scrollBehavior = scrollBehavior
                    )
                }

                null -> { // Fallback or initial state
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
                currentScreen = currentHomeNavScreen
            )
        }
    ) { innerPadding ->
        // NavHost specific to the HomeLayout, managing WallpaperList and Settings
        NavHost(
            navController = homeNavController,
            startDestination = WallpaperListScreenRoute, // Use the object directly
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            composable<WallpaperListScreenRoute> { // Use KSP type-safe navigation
                WallpaperListScreen(
                    viewModel = wallPaperListViewModelFromActivity,
                    onWallpaperClick = { wallpaperId ->
                        // Use the appNavController to navigate to a destination
                        // outside of the HomeLayout's internal navigation graph.
                        // Assuming WallpaperDetailRoute is part of your AppRoute
                        appNavController.navigate(AppRoute.WallpaperDetailRoute(wallpaperId))
                    }
                )
            }
            composable<SettingsScreenRoute> { // Use KSP type-safe navigation
                SettingsScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

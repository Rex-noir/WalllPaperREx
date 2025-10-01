package com.ace.wallpaperrex.ui.layouts

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ace.wallpaperrex.Screen
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperListTopAppBar
import com.ace.wallpaperrex.ui.screens.setting.SettingsScreen
import com.ace.wallpaperrex.ui.screens.wallpapers.WallPaperListViewModel
import com.ace.wallpaperrex.ui.screens.wallpapers.WallpaperListScreen


@Composable
fun AppBottomNavigationBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        bottomNavItems.forEach { screen ->
            val currentDestination = navController.currentBackStackEntry?.destination
            NavigationBarItem(
                icon = {
                    screen.icon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = screen.bottomNavTitleResId?.let { stringResource(it) })
                    }
                },
                label = {
                    screen.bottomNavTitleResId?.let { Text(stringResource(it)) }
                },
                selected = currentRoute == screen.route, // Simpler selection check
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
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

val bottomNavItems = listOf(
    Screen.WallpaperList,
    Screen.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeLayout(
    modifier: Modifier = Modifier,
    wallPaperListViewModelFromActivity: WallPaperListViewModel,
    appNavController: NavHostController
) {
    val homeNavController = rememberNavController()
    val currentHomeBackStackEntry by homeNavController.currentBackStackEntryAsState()
    val currentHomeRoute = currentHomeBackStackEntry?.destination?.route

    val currentHomeScreen = remember(currentHomeRoute) {
        bottomNavItems.find { currentHomeRoute?.startsWith(it.route) == true }
            ?: Screen.WallpaperList
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (currentHomeScreen == Screen.WallpaperList) {
                WallpaperListTopAppBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearchSubmit = {
                        wallPaperListViewModelFromActivity.searchWallpapers(it)
                    },
                    scrollBehavior = scrollBehavior,
                    onClearClicked = {
                        searchQuery = ""
                    }
                )
            } else if (currentHomeRoute != null) {
                TopAppBar(
                    title = {
                        currentHomeScreen.titleResId?.let {
                            Text(stringResource(it))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    ),
                    scrollBehavior = scrollBehavior
                )
            }
        },
        bottomBar = {
            AppBottomNavigationBar(
                navController = homeNavController,
                currentRoute = currentHomeRoute
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = homeNavController,
            startDestination = Screen.WallpaperList.route,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            composable(Screen.WallpaperList.route) {
                WallpaperListScreen(
                    viewModel = wallPaperListViewModelFromActivity,
                    onWallpaperClick = { wallpaperId ->
                        appNavController.navigate(Screen.WallpaperDetail.route)
                    })
            }

            composable(Screen.Settings.route) { SettingsScreen(modifier = Modifier.fillMaxSize()) }
        }

    }
}
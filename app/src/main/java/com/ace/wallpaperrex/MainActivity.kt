package com.ace.wallpaperrex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ace.wallpaperrex.data.repositories.WallhavenImageRepository
import com.ace.wallpaperrex.data.repositories.WallhavenImageRepositoryImpl
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperListTopAppBar
import com.ace.wallpaperrex.ui.screens.setting.SettingsScreen
import com.ace.wallpaperrex.ui.screens.wallpapers.WallPaperListViewModel
import com.ace.wallpaperrex.ui.screens.wallpapers.WallpaperListScreen
import com.ace.wallpaperrex.ui.theme.AppTheme

// Define your screen routes, including those for bottom navigation
sealed class Screen(
    val route: String,
    val titleResId: Int? = null,
    val icon: ImageVector? = null, // Icon for bottom navigation
    val bottomNavTitleResId: Int? = null // Title for bottom navigation
) {
    object WallpaperList : Screen(
        route = "wallpaper_list",
        titleResId = R.string.bottom_nav_home,
        icon = Icons.Filled.Home,
        bottomNavTitleResId = R.string.bottom_nav_home // Add to strings.xml
    )

    object Settings : Screen(
        route = "settings",
        titleResId = R.string.settings_title,
        icon = Icons.Filled.Settings,
        bottomNavTitleResId = R.string.bottom_nav_settings // Add to strings.xml
    )

    // This is a detail screen, not directly in bottom nav
    object WallpaperSourceDetail : Screen(
        route = "wallpaper_detail", // Note: The NavHost route will be "wallpaper_detail/{wallpaperId}"
        titleResId = R.string.wallpaper_detail_title
    )
}

// List of items for bottom navigation
val bottomNavItems = listOf(
    Screen.WallpaperList,
    Screen.Settings
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val repository = WallhavenImageRepositoryImpl();
        val viewModel: WallPaperListViewModel = WallPaperListViewModel(repository);


        setContent {
            AppTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                val currentScreen = remember(currentRoute) {
                    // This logic might need to be smarter if routes have arguments
                    // For now, it finds based on the base route string
                    Screen::class.sealedSubclasses.mapNotNull { it.objectInstance }
                        .find { currentRoute?.startsWith(it.route) == true }
                }


                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior();

                Scaffold(
                    topBar = {
                        // Show TopAppBar  current screen is not a detail screen or one with a custom top bar
                        var searchQuery by remember { mutableStateOf("") }

                        if (currentScreen == Screen.WallpaperList) {
                            WallpaperListTopAppBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onSearchSubmit = {
                                    viewModel.searchWallpapers(it)
                                },
                                onCloseClicked = {
                                    searchQuery = ""
                                },
                                scrollBehavior = scrollBehavior
                            )
                        } else {
                            TopAppBar(
                                title = {
                                    currentScreen?.titleResId?.let { Text(stringResource(it)) }
                                        ?: Text(stringResource(R.string.app_name))
                                },
                                navigationIcon = {
                                    // Show back arrow only if not a top-level bottom nav destination
                                    // and there's a previous entry
                                    val isTopLevelDestination =
                                        bottomNavItems.any { it.route == currentRoute }
                                    if (navController.previousBackStackEntry != null && !isTopLevelDestination) {
                                        IconButton(onClick = { navController.navigateUp() }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = stringResource(R.string.back_button_desc)
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors( // Use centerAlignedTopAppBarColors
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    },
                    bottomBar = {
                        // Only show bottom navigation for top-level screens
                        if (bottomNavItems.any { it.route == currentRoute }) {
                            AppBottomNavigationBar(
                                navController = navController,
                                currentRoute = currentRoute
                            )
                        }
                    }
                ) { innerPadding ->
                    AppNavigation(
                        wallpaperListModel = viewModel,
                        navController = navController,
                        modifier = Modifier
                            .padding(innerPadding)
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                    )
                }
            }
        }
    }
}

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

class WallpaperListViewModelFactory(private val wallhavenRepository: WallhavenImageRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WallPaperListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WallPaperListViewModel(wallhavenRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun AppNavigation(
    wallpaperListModel: WallPaperListViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        navController = navController,
        startDestination = Screen.WallpaperList.route, // Start with one of the bottom nav items
        modifier = modifier.fillMaxSize() // Ensure NavHost fills the available space
    ) {
        composable(Screen.WallpaperList.route) {


            WallpaperListScreen(
                viewModel = wallpaperListModel,
                onWallpaperClick = { wallpaperId -> },
                modifier = Modifier.fillMaxSize()
            )
        }
        composable(
            route = "${Screen.WallpaperSourceDetail.route}/{sourceId}", // Route with argument
            arguments = listOf(navArgument("sourceId") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val wallpaperId = backStackEntry.arguments?.getString("sourceId")
            if (wallpaperId != null) {
                Text("Detail for ID: $wallpaperId", modifier = Modifier.fillMaxSize())
            } else {
                Text("Error: Missing Wallpaper ID", modifier = Modifier.fillMaxSize())
            }
        }
        composable(Screen.Settings.route) {
            SettingsScreen(modifier = Modifier.fillMaxSize())
        }
    }
}


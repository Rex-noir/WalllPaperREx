package com.ace.wallpaperrex.ui.layouts

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ace.wallpaperrex.R
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import com.ace.wallpaperrex.data.repositories.DataStoreRepository
import com.ace.wallpaperrex.data.repositories.SourcesRepositoryImpl
import com.ace.wallpaperrex.data.repositories.WallpaperSourceRepository
import com.ace.wallpaperrex.ui.components.sources.SourceSettingTopBar
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.screens.models.SearchWallpaperViewModel
import com.ace.wallpaperrex.ui.screens.setting.GeneralSettingScreen
import com.ace.wallpaperrex.ui.screens.setting.GeneralSettingViewModel
import com.ace.wallpaperrex.ui.screens.setting.SourcesSettingsScreen
import com.ace.wallpaperrex.ui.screens.wallpapers.FavoriteListScreen
import com.ace.wallpaperrex.ui.screens.wallpapers.SearchWallpapersScreen
import com.ace.wallpaperrex.ui.screens.wallpapers.WallpaperListScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

// 1. Define concrete serializable types for your destinations
@Serializable
data object WallpaperListRoute

@Serializable
data object SettingsRoute

@Serializable
data object FavoriteListRoute

@Serializable
data object SourcesSettingsRoute

@Serializable
data object SearchWallpapersRoute

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
        routeKClass = SearchWallpapersRoute::class,
        routeInstance = SearchWallpapersRoute,
        icon = Icons.Filled.Search,
        titleResId = R.string.bottom_nav_search
    ),
    BottomNavigationItemInfo(
        routeKClass = SourcesSettingsRoute::class,
        routeInstance = SourcesSettingsRoute,
        icon = Icons.Filled.Api,
        titleResId = R.string.bottom_nav_sources_setting
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
//                label = { Text(stringResource(navItemInfo.titleResId)) },
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
    onWallpaperClick: (image: ImageItem, source: WallpaperSourceConfigItem?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val homeNavController = rememberNavController() // For navigation within HomeLayout
    val currentHomeBackStackEntry by homeNavController.currentBackStackEntryAsState()
    val currentNavDestination = currentHomeBackStackEntry?.destination

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val dataStoreRepository = DataStoreRepository(context)
    val sourcesRepository = SourcesRepositoryImpl(context)
    val sourceRepository = remember {
        WallpaperSourceRepository(
            sourceRepository = sourcesRepository,
            dataStoreRepository = dataStoreRepository
        )
    }
    val sources by sourceRepository.wallpaperSources.collectAsState(initial = emptyList())
    val sourceError by sourceRepository.sourceError.collectAsState(initial = null)
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        sourceRepository.initialize()
        isLoading = false
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

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

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (sourceError != null) {
        Scaffold { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "${sourceError?.message}")
                    Button(onClick = {
                        scope.launch {
                            sourceRepository.resetSourceConfigToDefault()
                        }
                    }) {
                        Text("Reset to default source config")
                    }
                }
            }
        }
        return
    }

    if (sources.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No sources found.")
        }
        return
    }
    Scaffold(
        modifier = modifier.then(
            if (currentNavDestination?.hasRoute<WallpaperListRoute>() == true || currentNavDestination?.hasRoute<SearchWallpapersRoute>() == true) {
                Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .nestedScroll(nestedScrollConnection)
            } else {
                Modifier
            }
        ),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            when {
                currentNavDestination?.hasRoute<WallpaperListRoute>() == true || currentNavDestination?.hasRoute<SearchWallpapersRoute>() == true -> {
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

                currentNavDestination?.hasRoute<SourcesSettingsRoute>() == true -> {
                    val navItem = homeBottomNavItems.find {
                        currentNavDestination.hasRoute(it.routeKClass)
                    }
                    SourceSettingTopBar(
                        title = navItem?.titleResId ?: R.string.bottom_nav_sources_setting,
                        onResetClick = {
                            scope.launch {
                                val result = sourceRepository.resetSourceConfigToDefault()
                                if (result.isSuccess) {
                                    snackbarHostState.showSnackbar(
                                        context.getString(R.string.sources_reset_success)
                                    )
                                } else {
                                    snackbarHostState.showSnackbar(
                                        result.exceptionOrNull()?.localizedMessage
                                            ?: context.getString(R.string.sources_reset_failed)
                                    )
                                }
                            }
                        }
                    )
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
                    onWallpaperClick = { image, source ->
                        onWallpaperClick(image, source)
                    },
                    wallpaperSourceRepository = sourceRepository
                )
            }
            composable<SourcesSettingsRoute> {
                SourcesSettingsScreen(wallpaperSourceRepository = sourceRepository)
            }
            composable<SettingsRoute> {
                GeneralSettingScreen(
                    viewModel = viewModel(
                        factory = GeneralSettingViewModel.factory(wallpaperSourceRepository = sourceRepository)
                    )
                )
            }
            composable<FavoriteListRoute> {
                FavoriteListScreen(
                    onWallpaperClick = { image, source ->
                        onWallpaperClick(image, source)
                    },
                    wallpaperSourceRepository = sourceRepository
                )
            }
            composable<SearchWallpapersRoute> { currentHomeBackStackEntry ->
                SearchWallpapersScreen(
                    onWallpaperClick = onWallpaperClick,
                    wallpaperSourceRepository = sourceRepository,
                    searchViewModel = viewModel(
                        viewModelStoreOwner = currentHomeBackStackEntry,
                        factory = SearchWallpaperViewModel.createFactory(sourceRepository)
                    )
                )
            }
        }
    }
}

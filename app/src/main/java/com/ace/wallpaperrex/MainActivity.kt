package com.ace.wallpaperrex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ace.wallpaperrex.data.repositories.WallhavenImageRepositoryImpl
import com.ace.wallpaperrex.ui.layouts.HomeLayout
import com.ace.wallpaperrex.ui.screens.wallpapers.WallPaperListViewModel
import com.ace.wallpaperrex.ui.theme.AppTheme

// Define your screen routes, including those for bottom navigation
sealed class Screen(
    val route: String,
    val titleResId: Int? = null,
    val icon: ImageVector? = null, // Icon for bottom navigation
    val bottomNavTitleResId: Int? = null // Title for bottom navigation
) {
    object WallpaperList : Screen(
        route = "home.wallpaper_list",
        titleResId = R.string.bottom_nav_home,
        icon = Icons.Filled.Home,
        bottomNavTitleResId = R.string.bottom_nav_home // Add to strings.xml
    )

    object Settings : Screen(
        route = "home.settings",
        titleResId = R.string.settings_title,
        icon = Icons.Filled.Settings,
        bottomNavTitleResId = R.string.bottom_nav_settings // Add to strings.xml
    )

}

object HomeRoute : Screen("home")

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = WallhavenImageRepositoryImpl()
        val wallpaperListViewModel = WallPaperListViewModel(repository)

        setContent {
            AppTheme {
                val appNavController = rememberNavController()

                NavHost(
                    navController = appNavController,
                    startDestination = HomeRoute.route,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }
                ) {
                    composable(HomeRoute.route) {
                        HomeLayout(
                            modifier = Modifier.fillMaxSize(),
                            wallPaperListViewModelFromActivity = wallpaperListViewModel
                        )
                    }
                }
            }
        }
    }
}



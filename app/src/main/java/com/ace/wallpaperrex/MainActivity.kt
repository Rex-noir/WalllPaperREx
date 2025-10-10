package com.ace.wallpaperrex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ace.wallpaperrex.ui.layouts.HomeLayout
import com.ace.wallpaperrex.ui.screens.models.SharedViewModel
import com.ace.wallpaperrex.ui.screens.wallpapers.WallpaperDetailScreen
import com.ace.wallpaperrex.ui.theme.AppTheme
import kotlinx.serialization.Serializable

interface AppRoute {
    val titleResId: Int?
        get() = null

    @Serializable
    data class HomeRoute(
        override val titleResId: Int? = R.string.bottom_nav_home,
    ) : AppRoute


    @Serializable
    data class WallpaperDetailRoute(
        val image: String,
        override val titleResId: Int = R.string.wallpaper_detail_title,
    ) : AppRoute
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.enableEdgeToEdge(window)
        setContent {
            AppTheme {
                val appNavController = rememberNavController()
                val sharedViewModel: SharedViewModel = viewModel()
                val sharedState by sharedViewModel.uiState.collectAsState()

                NavHost(
                    navController = appNavController,
                    startDestination = AppRoute.HomeRoute(),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable<AppRoute.HomeRoute> {
                        HomeLayout(
                            modifier = Modifier.fillMaxSize(),
                            onWallpaperClick = { imageItem ->
                                sharedViewModel.setSelectedImage(imageItem)
                                appNavController.navigate(AppRoute.WallpaperDetailRoute(imageItem.id))
                            },
                        )
                    }

                    composable<AppRoute.WallpaperDetailRoute> { backStackEntry ->
                        WallpaperDetailScreen(
                            onNavigateBack = { appNavController.popBackStack() },
                            viewModelStoreOwner = backStackEntry,
                            imageItem = sharedState.selectedImage,
                        )
                    }
                }
            }
        }
    }
}



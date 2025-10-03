package com.ace.wallpaperrex.ui.screens.wallpapers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ace.wallpaperrex.ui.components.wallpaper.shimmerBackground

@Composable
fun WallpaperDetailScreen(
    onNavigateBack: () -> Unit,
    wallpaperListViewModel: WallPaperListViewModel,
    viewModel: WallpaperDetailViewModel = viewModel()
) {
    val imageItem by viewModel.imageItem.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var isExpanded by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val imageId = viewModel.getImageId()
        imageId?.let {
            viewModel.setImage(wallpaperListViewModel.getImageById(it))
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
        contentWindowInsets = WindowInsets()
    ) { innerPadding ->
        if (imageItem == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: image not found")
            }
        } else {
            val image = imageItem!!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Fullscreen wallpaper image
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(image.url)
                        .crossfade(true)
                        .build(),
                    contentDescription = image.description ?: "Wallpaper Detail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(image.thumbnail)
                                .crossfade(true)
                                .build(),
                            contentDescription = image.description ?: "Wallpaper Detail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .shimmerBackground()
                                )

                            }
                        )
                    }
                )

                // Expandable FAB menu (bottom right)
                ExpandableFabMenu(
                    isExpanded = isExpanded,
                    isFavorite = isFavorite,
                    onExpandClick = { isExpanded = !isExpanded },
                    onFavoriteClick = {
                        isFavorite = !isFavorite
                        // TODO: Handle favorite action
                    },
                    onApplyClick = {
                        // TODO: Handle set wallpaper action
                        isExpanded = false
                    },
                    onDownloadClick = {
                        // TODO: Handle download action
                        isExpanded = false
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                )
            }
        }
    }
}

@Composable
fun ExpandableFabMenu(
    isExpanded: Boolean,
    isFavorite: Boolean,
    onExpandClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onApplyClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        label = "rotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Expandable menu items
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Favorite button with label
                FabMenuItem(
                    icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    label = "Favorite",
                    onClick = onFavoriteClick,
                    containerColor = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )

                // Apply wallpaper button with label
                FabMenuItem(
                    icon = Icons.Filled.Wallpaper,
                    label = "Apply",
                    onClick = onApplyClick
                )

                // Download button with label
                FabMenuItem(
                    icon = Icons.Filled.Download,
                    label = "Download",
                    onClick = onDownloadClick
                )
            }
        }

        // Main FAB
        FloatingActionButton(
            onClick = onExpandClick,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Filled.Close else Icons.Filled.Menu,
                contentDescription = if (isExpanded) "Close menu" else "Open menu",

                )
        }
    }
}

@Composable
fun FabMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Label
        Surface(
            shape = MaterialTheme.shapes.small,
            color = Color.Black.copy(alpha = 0.7f)
        ) {
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Icon button
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = containerColor
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
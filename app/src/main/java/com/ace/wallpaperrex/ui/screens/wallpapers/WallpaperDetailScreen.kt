package com.ace.wallpaperrex.ui.screens.wallpapers

import Picture
import ZoomParams
import android.app.Application
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.Bitmap
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperApplyDialog
import com.ace.wallpaperrex.utils.ImageFileHelper
import com.ace.wallpaperrex.utils.ImageFileHelper.saveRawBytesToUri
import kotlinx.coroutines.launch

@Composable
fun WallpaperDetailScreen(
    onNavigateBack: () -> Unit,
    wallpaperListViewModel: WallPaperListViewModel,
    viewModelStoreOwner: ViewModelStoreOwner
) {
    val viewModel: WallpaperDetailViewModel = viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = WallpaperDetailViewModel.Factory
    )

    val imageItem by viewModel.imageItem.collectAsStateWithLifecycle()
    val bytes by viewModel.imageByes.collectAsState()


    val bitmap: Bitmap? = bytes?.let { byteArray ->
        remember(byteArray) { BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size) }
    }

    val displayModel = remember(imageItem, bitmap) {
        bitmap ?: imageItem?.thumbnail
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var isExpanded by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }

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

            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            val downloadLauncher =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument(
                        "image/*"
                    )
                ) { uri ->
                    uri?.let { destinationUri ->
                        scope.launch {
                            viewModel.imageByes.value?.let { bytes ->
                                saveRawBytesToUri(context, bytes, uri)
                            }

                        }
                    }
                }

            WallpaperApplyDialog(
                isVisible = showDialog,
                imageBytes = bytes,
                onDismiss = { showDialog = false },
                onSuccess = {
                    showDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Wallpaper applied successfully")
                    }
                },
                onError = { error ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            error.localizedMessage ?: "An unknown error occurred"
                        )
                    }
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Picture(
                    model = displayModel,
                    shape = RectangleShape,
                    modifier = Modifier.fillMaxSize(),
                    shimmerEnabled = false,
                    crossfadeEnabled = false,
                    zoomParams = ZoomParams(zoomEnabled = true, hideBarsOnTap = true)
                )

                if (bytes != null) {
                    ExpandableFabMenu(
                        isExpanded = isExpanded,
                        isFavorite = isFavorite,
                        onExpandClick = { isExpanded = !isExpanded },
                        onFavoriteClick = {
                            isFavorite = !isFavorite
                            if (isFavorite) {
                                scope.launch {
                                    val localPath = ImageFileHelper.saveBytesToCache(
                                        context,
                                        "${image.id}${image.extension}",
                                        bytes!!
                                    )
                                    viewModel.addToFavorite(localPath)
                                }
                            } else {
                             ImageFileHelper.deleteCachedImage(
                                    context,
                                    "${image.id}${image.extension}"
                                )
                                viewModel.removeFromFavorite()
                            }
                        },
                        onApplyClick = {
                            isExpanded = false
                            showDialog = true
                        },
                        onDownloadClick = {
                            isExpanded = false
                            downloadLauncher.launch("${image.id}${image.extension}")
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp)
                    )
                }
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
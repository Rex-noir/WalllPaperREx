package com.ace.wallpaperrex.ui.screens.wallpapers

import Picture
import ZoomParams
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.ace.wallpaperrex.ui.components.wallpaper.CreditBar
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperApplyDialog
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.utils.ImageFileHelper.saveRawBytesToUri
import com.ace.wallpaperrex.utils.convertToWebpBytes
import kotlinx.coroutines.launch


@Composable
fun WallpaperDetailScreen(
    onNavigateBack: () -> Unit,
    viewModelStoreOwner: NavBackStackEntry,
    imageList: List<ImageItem>
) {

    val viewModel: WallpaperDetailViewModel = viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = WallpaperDetailViewModel.Factory,
        extras = MutableCreationExtras(initialExtras = viewModelStoreOwner.defaultViewModelCreationExtras).apply {
            set(WallpaperDetailViewModel.IMAGE_LIST_KEY, imageList)
        }
    )


    val imageItem by viewModel.imageItem.collectAsStateWithLifecycle()
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var isExpanded by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    val isFavorite = viewModel.isFavorite.collectAsStateWithLifecycle()

    var isTogglingFavorite = viewModel.isSavingAsFavorite.collectAsStateWithLifecycle()


    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
        floatingActionButton = {
            if (imageBitmap != null) {
                val image = imageItem!!

                val downloadLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.CreateDocument(
                            "image/*"
                        )
                    ) { uri ->
                        uri?.let { destinationUri ->
                            scope.launch {
                                val bytes = imageBitmap?.convertToWebpBytes()
                                saveRawBytesToUri(context, bytes!!, uri)
                            }
                        }
                    }
                ExpandableFabMenu(
                    isFavoriteLoading = isTogglingFavorite.value,
                    isExpanded = isExpanded,
                    isFavorite = isFavorite.value,
                    onExpandClick = { isExpanded = !isExpanded },
                    onFavoriteClick = {
                        try {
                            viewModel.toggleFavoriteState(
                                context,
                                imageBitmap!!,
                                "${image.id}.${image.extension}"
                            )
                        } finally {
                        }
                    },
                    onApplyClick = {
                        isExpanded = false
                        showDialog = true
                    },
                    onDownloadClick = {
                        isExpanded = false
                        downloadLauncher.launch("${image.id}.${image.extension}")
                    },
                    modifier = Modifier
                        .padding(24.dp)
                )
            }
        },
        contentWindowInsets = WindowInsets()
    ) { innerPadding ->
        if (imageItem == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: image not found")
            }
        } else {
            WallpaperApplyDialog(
                isVisible = showDialog,
                imageBitmap = imageBitmap,
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
                val image = imageItem!!
                Log.d("WallpaperDetailScreen", "image: $image")
                if (image.uploader != null) {
                    AnimatedVisibility(
                        visible = imageBitmap != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        CreditBar(
                            uploaderName = image.uploader,
                            uploaderUrl = image.uploaderUrl,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Picture(
                    model = imageItem!!.url,
                    shape = RectangleShape,
                    modifier = Modifier.fillMaxSize(),
                    shimmerEnabled = false,
                    allowHardware = false,
                    crossfadeEnabled = false,
                    onSuccess = { successState ->
                        val drawable = successState.result.drawable
                        val bitmap = (drawable as BitmapDrawable).bitmap
                        imageBitmap = bitmap
                    },
                    loading = {
                        Picture(
                            model = imageItem!!.thumbnail,
                            shape = RectangleShape,
                            modifier = Modifier.fillMaxSize(),
                            shimmerEnabled = true,
                            crossfadeEnabled = false,
                            zoomParams = ZoomParams(zoomEnabled = true, hideBarsOnTap = true)
                        )
                    },
                    zoomParams = ZoomParams(zoomEnabled = true, hideBarsOnTap = true)
                )


            }
        }
    }
}

@Composable
fun ExpandableFabMenu(
    isExpanded: Boolean,
    isFavoriteLoading: Boolean,
    isFavorite: Boolean,
    onExpandClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onApplyClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    isLoading = isFavoriteLoading,
                    containerColor = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )

                // Apply wallpaper button with label
                FabMenuItem(
                    icon = Icons.Filled.Wallpaper,
                    label = "Apply",
                    onClick = onApplyClick,
                )

                // Download button with label
                FabMenuItem(
                    icon = Icons.Filled.Download,
                    label = "Download",
                    onClick = onDownloadClick,
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
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    isLoading: Boolean = false
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
            onClick = {
                if (!isLoading) {
                    onClick()
                }
            },
            containerColor = containerColor,
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
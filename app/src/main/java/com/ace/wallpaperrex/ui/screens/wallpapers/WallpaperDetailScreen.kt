package com.ace.wallpaperrex.ui.screens.wallpapers

import Picture
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import com.ace.wallpaperrex.ui.components.wallpaper.CreditBar
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperApplyDialog
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.screens.models.WallpaperDetailViewModel
import com.ace.wallpaperrex.utils.ImageFileHelper.saveRawBytesToUri
import com.ace.wallpaperrex.utils.convertToWebpBytes
import kotlinx.coroutines.launch


@Composable
fun WallpaperDetailScreen(
    onNavigateBack: () -> Unit,
    viewModelStoreOwner: NavBackStackEntry,
    imageItem: ImageItem?,
    source: WallpaperSourceConfigItem?,
) {

    val viewModel: WallpaperDetailViewModel = viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = WallpaperDetailViewModel.createFactory(imageItem!!, source)
    )


    val imageItem by viewModel.imageItem.collectAsStateWithLifecycle()
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    var isExpanded by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    val isFavorite = viewModel.isFavorite.collectAsStateWithLifecycle()

    val isTogglingFavorite = viewModel.isSavingAsFavorite.collectAsStateWithLifecycle()


    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    // Pan state
    val panOffset by viewModel.panOffset.collectAsStateWithLifecycle()
    val scale by viewModel.scale.collectAsStateWithLifecycle()
    var maxPanX by remember { mutableFloatStateOf(0f) }
    var maxPanY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(imageBitmap, containerSize) {
        if (imageBitmap != null && containerSize.width > 0 && containerSize.height > 0) {
            val bitmapWidth = imageBitmap!!.width.toFloat()
            val bitmapHeight = imageBitmap!!.height.toFloat()
            val containerWidth = containerSize.width.toFloat()
            val containerHeight = containerSize.height.toFloat()

            val bitmapAspect = bitmapWidth / bitmapHeight
            val containerAspect = containerWidth / containerHeight

            // Calculate scale to fill screen
            val scale = if (bitmapAspect > containerAspect) {
                containerHeight / bitmapHeight
            } else {
                containerWidth / bitmapWidth
            }

            // Calculate how much we can pan
            val scaledWidth = bitmapWidth * scale
            val scaledHeight = bitmapHeight * scale

            maxPanX = ((scaledWidth - containerWidth) / 2f).coerceAtLeast(0f)
            maxPanY = ((scaledHeight - containerHeight) / 2f).coerceAtLeast(0f)

            viewModel.updateScale(scale)
        }
    }

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
                                if (source?.api?.endpoints?.download != null) {
                                    viewModel.hitDownloadEndpoint()
                                }
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
                        viewModel.toggleFavoriteState(
                            context,
                            imageBitmap!!,
                            "${image.id}.${image.extension}"
                        )
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
                        .padding(vertical = 70.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
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
                },
                scale = scale,
                panOffset = panOffset,
                containerSize = containerSize
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                Picture(
                    model = imageItem!!.url,
                    shape = RectangleShape,
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { containerSize = it }
                        .then(
                            if (imageItem != null && imageItem?.placeHolderColor != null) Modifier.background(
                                imageItem!!.placeHolderColor!!
                            ) else Modifier
                        )
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val newOffset = panOffset + dragAmount
                                viewModel.updatePanOffset(
                                    Offset(
                                        x = newOffset.x.coerceIn(-maxPanX, maxPanX),
                                        y = newOffset.y.coerceIn(-maxPanY, maxPanY)
                                    )
                                )
                            }
                        }
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = panOffset.x
                            translationY = panOffset.y
                        },
                    shimmerEnabled = false,
                    contentScale = ContentScale.Fit,
                    allowHardware = false,
                    crossfadeEnabled = false,
                    zoomEnabled = true,
                    snapBackZoom = true,
                    onSuccess = { successState ->
                        val drawable = successState.result.drawable
                        val bitmap = (drawable as BitmapDrawable).bitmap
                        imageBitmap = bitmap
                    },
                    loading = {
                        Picture(
                            model = imageItem!!.thumbnail,
                            shape = RectangleShape,
                            contentScale = ContentScale.FillHeight,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (imageItem != null && imageItem?.placeHolderColor != null) Modifier.background(
                                        imageItem!!.placeHolderColor!!
                                    ) else Modifier
                                )
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        val newOffset = panOffset + dragAmount
                                        viewModel.updatePanOffset(
                                            Offset(
                                                x = newOffset.x.coerceIn(-maxPanX, maxPanX),
                                                y = newOffset.y.coerceIn(-maxPanY, maxPanY)
                                            )
                                        )
                                    }
                                }
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = panOffset.x
                                    translationY = panOffset.y
                                },
                            shimmerEnabled = true,
                            crossfadeEnabled = false,
                        )
                    },
                )

                imageItem?.uploader?.let {
                    AnimatedVisibility(
                        visible = imageBitmap != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        CreditBar(
                            uploaderName = imageItem!!.uploader!!,
                            uploaderUrl = imageItem!!.uploaderUrl,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
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
                    icon = if (isFavorite && !isFavoriteLoading) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    label = "Favorite",
                    onClick = onFavoriteClick,
                    isLoading = isFavoriteLoading,
                    containerColor = if (isFavorite && !isFavoriteLoading) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
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
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = ProgressIndicatorDefaults.linearTrackColor
                )
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
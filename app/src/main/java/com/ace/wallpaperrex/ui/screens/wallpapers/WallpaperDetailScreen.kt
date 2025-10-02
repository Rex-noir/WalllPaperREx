package com.ace.wallpaperrex.ui.screens.wallpapers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ace.wallpaperrex.data.ImageItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperDetailScreen(
    onNavigateBack: () -> Unit,
    wallpaperListViewModel: WallPaperListViewModel,
    viewModel: WallpaperDetailViewModel = viewModel()
) {

    val imageItem by viewModel.imageItem.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val snackbarHostState = remember { SnackbarHostState() }

    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val imageId = viewModel.getImageId();
        imageId?.let {
            viewModel.setImage(wallpaperListViewModel.getImageById(it))
        }
    }

    if (imageItem == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error : image not found")
        }
    } else {
        val image = imageItem!!
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        image.description?.let {
                            Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    navigationIcon = {
                        IconButton(onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            /* TODO : Handle share action  */
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share Wallpaper"
                            )
                        }
                        IconButton(onClick = { /* TODO : Handle set wallpaper action  */ }) {
                            Icon(
                                imageVector = Icons.Filled.Wallpaper,
                                contentDescription = "Set as wallpaper"
                            )
                        }
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More options"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("View photographer") },
                                onClick = {
                                    /* TODO : Navigate to photographer profile or search */
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Download") },
                                onClick = {
                                    /* TODO: Handle download action */
                                    showMenu = false
                                }
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        ) { innerPadding ->
            WallpaperDetailContent(
                imageItem = image,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun WallpaperDetailContent(imageItem: ImageItem, modifier: Modifier) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(imageItem.url)
                .crossfade(true).build(),
            contentDescription = imageItem.description ?: "Wallpaper Detail",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

//        Column {  }
    }
}
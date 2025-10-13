package com.ace.wallpaperrex.ui.screens.wallpapers

import Picture
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ace.wallpaperrex.R
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import com.ace.wallpaperrex.data.repositories.WallpaperSourceRepository
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.screens.models.FavoriteListViewModel
import kotlinx.coroutines.launch

@Composable
fun FavoriteListScreen(
    onWallpaperClick: (ImageItem, WallpaperSourceConfigItem?) -> Unit,
    modifier: Modifier = Modifier,
    wallpaperSourceRepository: WallpaperSourceRepository,
    favoriteListViewModel: FavoriteListViewModel = viewModel(factory = FavoriteListViewModel.Factory),
) {
    val scope = rememberCoroutineScope()

    val favorites by favoriteListViewModel.favorites.collectAsState()
    if (favorites.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Icon(
                    painter = painterResource(R.drawable.vacuum_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                    contentDescription = "No favorites",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(64.dp)
                )
                Text(
                    "Looks like you have no favorites.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

        }

    } else {

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(items = favorites, key = { _, item -> item.id }) { index, item ->
                Picture(
                    model = item.url,
                    contentDescription = item.description,
                    contentScale = ContentScale.Crop,
                    shape = RectangleShape,
                    modifier = modifier
                        .fillMaxSize()
                        .aspectRatio(item.aspectRatio.takeIf { it > 0 } ?: (3f / 4f))
                        .clip(RoundedCornerShape(size = 0.dp))
                        .clickable(onClick = {
                           scope.launch {
                               val source = wallpaperSourceRepository.getWallpaperSource(item.sourceKey)
                               onWallpaperClick(item, source)
                           }
                        })
                )
            }
        }
    }

}
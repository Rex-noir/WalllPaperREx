package com.ace.wallpaperrex.ui.screens.wallpapers

import Picture
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FavoriteListScreen(
    modifier: Modifier = Modifier,
    viewModel: FavoriteListViewModel = viewModel(factory = FavoriteListViewModel.Factory)
) {
    val favorites = viewModel.favorites.collectAsState()

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 150.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(items = favorites.value, key = { _, item -> item.id }) { index, item ->

            Picture(
                model = item.localPath,
                contentDescription = item.description,
                contentScale = ContentScale.Crop,
                shape = RectangleShape,
                modifier = modifier
                    .fillMaxSize()
                    .aspectRatio(item.aspectRatio.takeIf { it > 0 } ?: (3f / 4f))
                    .clip(RoundedCornerShape(size = 0.dp))
                    .clickable(onClick = {

                    })
            )
        }
    }
}
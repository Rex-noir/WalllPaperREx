package com.ace.wallpaperrex.ui.screens.wallpapers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ace.wallpaperrex.data.daos.getWallpaperSourcesFlow
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperStaggeredGrid
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.models.WallpaperSourceItem
import com.ace.wallpaperrex.ui.screens.models.SearchWallpaperViewModel
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchWallpapersScreen(
    searchViewModel: SearchWallpaperViewModel = viewModel(factory = SearchWallpaperViewModel.Factory),
    onWallpaperClick: (ImageItem) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {

    // --- State Management ---
    val textFieldState: TextFieldState = rememberTextFieldState()
    var expanded by rememberSaveable { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    val selectedSource by searchViewModel.selectedSource.collectAsState()

    val searchHistory by searchViewModel.searchHistory.collectAsState()

    val context = LocalContext.current

    val wallpaperSources by context.getWallpaperSourcesFlow()
        .map { sourceItems -> sourceItems.filter { it.isConfigured } }
        .collectAsStateWithLifecycle(initialValue = emptyList())


    val isLoading by searchViewModel.isLoading.collectAsState()
    val images by searchViewModel.images.collectAsState()
    val error by searchViewModel.error.collectAsState()
    val isEndOfList by searchViewModel.isEndOfList.collectAsState()

    if (showFilterDialog) {
        FilterDialog(
            sources = wallpaperSources,
            selectedSource = selectedSource,
            onSourceSelected = { source ->
                searchViewModel.setSelectedSource(source)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .semantics { isTraversalGroup = true }
    ) {
        // --- Search Bar ---
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .semantics { traversalIndex = 0f },
            windowInsets = WindowInsets(0, 0, 0, 0),
            colors = SearchBarDefaults.colors(containerColor = Color.Transparent),
            tonalElevation = 0.dp,
            inputField = {
                SearchBarDefaults.InputField(
                    query = textFieldState.text.toString(),
                    onQueryChange = { textFieldState.edit { replace(0, length, it) } },
                    onSearch = { query ->
                        if (query.isNotBlank()) {
                            searchViewModel.performSearch(query)
                        }
                        expanded = false
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Search Wallpapers") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search Icon")
                    },
                    trailingIcon = {
                        if (expanded && textFieldState.text.isNotEmpty()) {
                            IconButton(onClick = { textFieldState.clearText() }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Search")
                            }
                        }
                    }
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            // --- Search History and Suggestions ---
            val currentQuery = textFieldState.text.toString()
            val filteredHistory = remember(currentQuery, searchHistory) {
                if (currentQuery.isBlank()) searchHistory
                else searchHistory.filter { it.query.contains(currentQuery, ignoreCase = true) }
            }

            if (searchHistory.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { searchViewModel.clearSearchHistory() },
                        modifier = Modifier.align(Alignment.CenterEnd),
                    ) {
                        Text("Clear history")
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filteredHistory, key = { it.id }) { item ->
                    ListItem(
                        headlineContent = { Text(item.query) },
                        leadingContent = { Icon(Icons.Filled.History, null) },
                        trailingContent = {
                            IconButton(onClick = { searchViewModel.deleteSearchItem(item) }) {
                                Icon(Icons.Default.Close, "Delete search entry")
                            }
                        },
                        modifier = Modifier.clickable {
                            textFieldState.edit {
                                replace(0, length, item.query)
                            }
                            searchViewModel.performSearch(item.query)
                            expanded = false
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }

                if (currentQuery.isNotBlank() && filteredHistory.isEmpty()) {
                    item {
                        ListItem(
                            headlineContent = { Text("Search for \"$currentQuery\"") },
                            leadingContent = { Icon(Icons.Filled.Search, null) },
                            modifier = Modifier.clickable {
                                searchViewModel.performSearch(currentQuery)
                                expanded = false
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }

        if (!expanded) {

            WallpaperStaggeredGrid(
                items = images,
                isLoadingMore = isLoading,
                isEndOfList = isEndOfList,
                modifier = Modifier.fillMaxSize(),
                error = error,
                onLoadMore = { searchViewModel.loadMore() },
                onRetryLoadMore = { searchViewModel.loadMore() },
                onWallpaperClick = onWallpaperClick
            )
            FloatingActionButton(
                onClick = { showFilterDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter sources"
                )
            }
        }
    }
}

@Composable
private fun FilterDialog(
    sources: List<WallpaperSourceItem>,
    selectedSource: WallpaperSourceItem?,
    onSourceSelected: (WallpaperSourceItem) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select a Source") },
        text = {
            LazyColumn {
                items(sources) { source ->
                    ListItem(
                        headlineContent = { Text(source.name) },
                        leadingContent = {
                            RadioButton(
                                selected = selectedSource?.id == source.id,
                                onClick = { onSourceSelected(source) }
                            )
                        },
                        colors = ListItemDefaults.colors().copy(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onSourceSelected(source) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

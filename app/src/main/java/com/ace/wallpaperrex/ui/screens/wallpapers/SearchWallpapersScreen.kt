package com.ace.wallpaperrex.ui.screens.wallpapers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.Alignment.Companion.CenterEnd
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
import com.ace.wallpaperrex.ui.screens.models.SearchWallpaperViewModel
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchWallpapersScreen(
    searchViewModel: SearchWallpaperViewModel = viewModel(factory = SearchWallpaperViewModel.Factory),
    onWallpaperClick: (ImageItem) -> Unit
) {

    // --- State Management ---
    val textFieldState: TextFieldState = rememberTextFieldState()
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedSource by searchViewModel.selectedSource.collectAsState()

    // --- Data from ViewModel ---
    val searchHistory by searchViewModel.searchHistory.collectAsState()
    val activeSearchQuery by searchViewModel.searchQuery.collectAsState()

    val context = LocalContext.current

    val wallpaperSources by context.getWallpaperSourcesFlow()
        .map { sourceItems -> sourceItems.filter { it.isConfigured } }
        .collectAsStateWithLifecycle(initialValue = emptyList())


    val isLoading by searchViewModel.isLoading.collectAsState()
    val images by searchViewModel.images.collectAsState()
    val error by searchViewModel.error.collectAsState()
    val isEndOfList by searchViewModel.isEndOfList.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { isTraversalGroup = true }
    ) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .semantics { traversalIndex = 0f },
            windowInsets = WindowInsets(0, 0, 0, 0),
            inputField = {
                SearchBarDefaults.InputField(
                    query = textFieldState.text.toString(),
                    onQueryChange = { textFieldState.edit { replace(0, length, it) } },
                    onSearch = { query ->
                        searchViewModel.performSearch(query)
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
            onExpandedChange = {
                expanded = it
            },
        ) {
            // --- Content for the search suggestions (now history) ---
            val currentQuery = textFieldState.text.toString()

            // Filter the history based on the current text input
            val filteredHistory = remember(currentQuery, searchHistory) {
                if (currentQuery.isBlank()) {
                    searchHistory
                } else {
                    searchHistory.filter {
                        it.query.contains(currentQuery, ignoreCase = true)
                    }
                }
            }

            // Clear all button
            if (searchHistory.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            searchViewModel.clearSearchHistory()
                        },
                        modifier = Modifier.align(alignment = CenterEnd),
                    ) {
                        Text("Clear history")
                    }
                }

            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredHistory, key = { it.id }) { item ->
                    ListItem(
                        headlineContent = { Text(item.query) },
                        leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                        // Add a trailing icon to delete history items
                        trailingContent = {
                            IconButton(onClick = { searchViewModel.deleteSearchItem(item) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Delete search entry"
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            textFieldState.edit { append(item.query) }
                            searchViewModel.performSearch(item.query)
                            expanded = false
                        },
                        colors = ListItemDefaults.colors().copy(containerColor = Color.Transparent)
                    )
                }

                // If text is entered but there's no history match, show the "Search for..." option
                if (currentQuery.isNotBlank() && filteredHistory.isEmpty()) {
                    item {
                        ListItem(
                            headlineContent = { Text("Search for \"$currentQuery\"") },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.clickable {
                                searchViewModel.performSearch(currentQuery)
                                expanded = false
                            },
                            colors = ListItemDefaults.colors()
                                .copy(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SearchBarDefaults.InputFieldHeight + 18.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
            ) {
                items(wallpaperSources) { source ->
                    FilterChip(
                        selected = selectedSource?.id == source.id,
                        onClick = {
                            searchViewModel.setSelectedSource(source)
                        },
                        label = { Text(source.name) }
                    )
                }

            }
            WallpaperStaggeredGrid(
                items = images,
                isLoadingMore = isLoading,
                isEndOfList = isEndOfList,
                error = error,
                onLoadMore = { searchViewModel.loadMore() },
                onRetryLoadMore = { searchViewModel.loadMore() },
                onWallpaperClick = onWallpaperClick
            )
        }

    }
}

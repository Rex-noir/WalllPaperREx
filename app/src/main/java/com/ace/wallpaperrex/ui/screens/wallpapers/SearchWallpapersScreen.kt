package com.ace.wallpaperrex.ui.screens.wallpapers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ace.wallpaperrex.data.entities.SearchHistoryItem
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import com.ace.wallpaperrex.data.repositories.WallpaperSourceRepository
import com.ace.wallpaperrex.ui.components.wallpaper.WallpaperStaggeredGrid
import com.ace.wallpaperrex.ui.models.ImageItem
import com.ace.wallpaperrex.ui.screens.models.SearchWallpaperViewModel
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchWallpapersScreen(
    searchViewModel: SearchWallpaperViewModel,
    onWallpaperClick: (ImageItem, WallpaperSourceConfigItem) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    wallpaperSourceRepository: WallpaperSourceRepository
) {

    // --- State Management ---
    val textFieldState: TextFieldState = rememberTextFieldState()
    var expanded by rememberSaveable { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    val selectedSource by searchViewModel.selectedSource.collectAsState()

    val searchHistory by searchViewModel.searchHistory.collectAsState()

    val wallpaperSources by wallpaperSourceRepository.wallpaperSources
        .map { sourceItems -> sourceItems.filter { it.isConfigured } }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val isLoading by searchViewModel.isLoading.collectAsState()
    val images by searchViewModel.images.collectAsState()
    val error by searchViewModel.error.collectAsState()
    val isEndOfList by searchViewModel.isEndOfList.collectAsState()

    val searchQuery by searchViewModel.searchQuery.collectAsState()

    var isSearchBarVisible by rememberSaveable { mutableStateOf(true) }

    // Always show search bar when it's expanded
    LaunchedEffect(expanded) {
        if (expanded) {
            isSearchBarVisible = true
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Hide search bar when scrolling down
                if (available.y > -1) {
                    isSearchBarVisible = false
                }
                // Show search bar when scrolling up
                if (available.y < 1) {
                    isSearchBarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    if (showFilterDialog) {
        EnhancedFilterDialog(
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
            .nestedScroll(nestedScrollConnection)
    ) {
        if (searchQuery.isBlank()) {
            EmptySearchState(
                modifier = Modifier.align(Alignment.Center),
                selectedSource = selectedSource
            )
        } else {
            WallpaperStaggeredGrid(
                items = images,
                isLoadingMore = isLoading,
                isEndOfList = isEndOfList,
                modifier = Modifier.fillMaxSize(),
                error = error,
                onLoadMore = { searchViewModel.loadMore() },
                onRetryLoadMore = { searchViewModel.loadMore() },
                onWallpaperClick = { image ->
                    onWallpaperClick(image, selectedSource!!)
                },
            )
        }

        // --- Search Bar ---
        AnimatedVisibility(
            visible = isSearchBarVisible,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            SearchBar(
                modifier = Modifier.semantics { traversalIndex = 0f },
                windowInsets = WindowInsets(0, 0, 0, 0),
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
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (expanded && textFieldState.text.isNotEmpty()) {
                                IconButton(onClick = { textFieldState.clearText() }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear Search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    )
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                SearchContent(
                    textFieldState = textFieldState,
                    searchHistory = searchHistory,
                    searchViewModel = searchViewModel,
                    onSearchPerformed = { expanded = false }
                )
            }
        }

        if (!expanded) {
            AnimatedFloatingActionButton(
                onClick = { showFilterDialog = true },
                selectedSource = selectedSource,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun EmptySearchState(
    modifier: Modifier = Modifier,
    selectedSource: WallpaperSourceConfigItem?
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ImageSearch,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Search Wallpapers",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter a keyword to discover amazing wallpapers from different sources",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (selectedSource != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "Source: ${selectedSource.label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchContent(
    textFieldState: TextFieldState,
    searchHistory: List<SearchHistoryItem>,
    searchViewModel: SearchWallpaperViewModel,
    onSearchPerformed: () -> Unit
) {
    val currentQuery = textFieldState.text.toString()
    val filteredHistory = remember(currentQuery, searchHistory) {
        if (currentQuery.isBlank()) searchHistory
        else searchHistory.filter { it.query.contains(currentQuery, ignoreCase = true) }
    }

    Column {
        if (searchHistory.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent searches",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { searchViewModel.clearSearchHistory() }
                ) {
                    Text(
                        "Clear all",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(filteredHistory, key = { it.id }) { item ->
                ListItem(
                    headlineContent = {
                        Text(
                            item.query,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingContent = {
                        Icon(
                            Icons.Filled.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = { searchViewModel.deleteSearchItem(item) }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Delete search entry",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier.clickable {
                        textFieldState.edit {
                            replace(0, length, item.query)
                        }
                        searchViewModel.performSearch(item.query)
                        onSearchPerformed()
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            if (currentQuery.isNotBlank() && filteredHistory.isEmpty()) {
                item {
                    ListItem(
                        headlineContent = {
                            Text(
                                "Search for \"$currentQuery\"",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable {
                            searchViewModel.performSearch(currentQuery)
                            onSearchPerformed()
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedFloatingActionButton(
    onClick: () -> Unit,
    selectedSource: WallpaperSourceConfigItem?,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (selectedSource != null) 0f else 360f,
        label = "FAB rotation"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        BadgedBox(
            badge = {
                if (selectedSource != null) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter sources",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
private fun EnhancedFilterDialog(
    sources: List<WallpaperSourceConfigItem>,
    selectedSource: WallpaperSourceConfigItem?,
    onSourceSelected: (WallpaperSourceConfigItem) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                "Select Source",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            if (sources.isEmpty()) {
                EmptySourcesState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(sources) { source ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSourceSelected(source) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedSource?.uniqueKey == source.uniqueKey)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = selectedSource?.uniqueKey == source.uniqueKey,
                                    onClick = { onSourceSelected(source) }
                                )
                                Text(
                                    text = source.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (selectedSource?.uniqueKey == source.uniqueKey)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

@Composable
private fun EmptySourcesState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Sources Available",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please configure at least one wallpaper source in settings",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
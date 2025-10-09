package com.ace.wallpaperrex.ui.screens.wallpapers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ace.wallpaperrex.ui.screens.models.SearchWallpaperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchWallpapersScreen(
    searchViewModel: SearchWallpaperViewModel = viewModel(factory = SearchWallpaperViewModel.Factory)
) {

    // --- State Management ---
    val textFieldState: TextFieldState = rememberTextFieldState()
    var expanded by rememberSaveable { mutableStateOf(false) }
    var activeSearchQuery by rememberSaveable { mutableStateOf<String?>(null) }

    // --- Data from ViewModel ---
    val searchHistory by searchViewModel.searchHistory.collectAsState()


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
                        searchViewModel.addSearchQuery(query)
                        activeSearchQuery = query
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
                            // On click, perform the search with the history item's query
                            textFieldState.edit { append(item.query) }
                            searchViewModel.addSearchQuery(item.query)
                            activeSearchQuery = item.query
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
                                searchViewModel.addSearchQuery(currentQuery)
                                activeSearchQuery = currentQuery
                                expanded = false
                            },
                            colors = ListItemDefaults.colors()
                                .copy(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }

        // --- Content for displaying the actual search results after a search is performed ---
        activeSearchQuery?.let { query ->
            // You would replace this with your actual search results UI
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = SearchBarDefaults.InputFieldHeight),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Showing results for: $query")
            }
        }
    }
}

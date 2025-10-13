package com.ace.wallpaperrex.ui.screens.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ace.wallpaperrex.data.daos.getWallpaperSourcesFlow
import com.ace.wallpaperrex.data.daos.setDefaultWallpaperSourceId
import com.ace.wallpaperrex.data.daos.setWallpaperApiKey
import com.ace.wallpaperrex.ui.components.sources.SourceSettingCard
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem
import com.ace.wallpaperrex.ui.models.wallpaperSourcesStatic
import kotlinx.coroutines.launch

@Composable
fun SourcesSettingsScreen(
    modifier: Modifier = Modifier,
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val sources by context.getWallpaperSourcesFlow().collectAsState(
        initial = emptyList(),
    )

    if (sources.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("No sources available", textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(
            modifier = modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items = wallpaperSourcesStatic,
                key = { _, source -> source.id }
            ) { index, sourceItem ->
                val sourceModel = sources.find { it.id == sourceItem.id }
                SourceSettingCard(
                    source = sourceModel!!,
                    modifier = Modifier,
                    onSetAsDefault = {
                        scope.launch {
                            context.setDefaultWallpaperSourceId(sourceItem.id)
                        }
                    }
                ) {
                    when (sourceItem.id) {
                        1, 2 -> {
                            SourceApiKeySettingInput(sourceModel, onApiKeySave = { id, key ->
                                scope.launch {
                                    context.setWallpaperApiKey(sourceItem.apiKeyDataStoreKey, key)
                                }
                            })
                        }

                        else ->
                            Text("No configuration needed/supported currently")
                    }
                }

            }
        }
    }
}

@Composable
fun SourceApiKeySettingInput(
    source: WallpaperSourceConfigItem,
    onApiKeySave: (sourceId: Int, apiKey: String) -> Unit
) {

    val apiKeyTextFieldState = rememberTextFieldState(initialText = source.apiKey ?: "")
    var isInEditMode by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        if (source.needsApiKey) {
            Text("API Key is required for this source", style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            state = apiKeyTextFieldState, // Use the state from rememberTextFieldState
            label = { Text("API Key") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            shape = RoundedCornerShape(12.dp),
            lineLimits = TextFieldLineLimits.SingleLine,
            enabled = isInEditMode,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                IconButton(onClick = {
                    if (isInEditMode) {
                        onApiKeySave(source.id, apiKeyTextFieldState.text.toString())
                        isInEditMode = false
                    } else {
                        isInEditMode = true
                    }
                }) {
                    Icon(
                        imageVector = if (isInEditMode) Icons.Default.Save else Icons.Default.Edit,
                        contentDescription = if (isInEditMode) "Save API Key" else "Edit API Key",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        if (isInEditMode) {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }
}
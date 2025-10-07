package com.ace.wallpaperrex.ui.components.sources

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.ace.wallpaperrex.ui.models.WallpaperSourceItem

@Composable
fun WallhavenSetting(
    source: WallpaperSourceItem, // Pass the specific source item
    onApiKeySave: (sourceId: Int, apiKey: String) -> Unit
) {
    // This logic is specific to Wallhaven's API key field
    val apiKeyTextFieldState = rememberTextFieldState(initialText = source.apiKey ?: "")
    var isInEditMode by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

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
    } else {
        // Optionally clear focus when not in edit mode
        // LaunchedEffect(Unit) { // Or key on isInEditMode if needed
        //     focusRequester.freeFocus()
        // }
    }
}

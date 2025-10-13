package com.ace.wallpaperrex.ui.components.sources

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ace.wallpaperrex.data.models.WallpaperSourceConfigItem

/**
 * A generic card to display a wallpaper source item.
 * It handles the common display of name, status, and toggle,
 * and accepts a lambda for custom content specific to the source type.
 *
 * This version uses ListItem for better structure and alignment.
 */
@Composable
fun SourceSettingCard(
    source: WallpaperSourceConfigItem,
    onSetAsDefault: () -> Unit, // Add a callback for the action
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit // Slot for custom body content
) {
    Card(modifier = modifier) {
        Column {
            // Use ListItem for the main settings row for proper Material Design alignment
            ListItem(
                // Set colors to transparent to let the Card's background show through
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                // Main text for the setting
                headlineContent = {
                    Text(
                        text = source.label,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                // Trailing content holds the action button or default indicator
                trailingContent = {
                    if (source.isDefault) {
                        // Use a disabled button for a "chip"-like appearance. It's more prominent.
                        TextButton(
                            onClick = { },
                            enabled = false,
                            colors = ButtonDefaults.textButtonColors(
                                disabledContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(text = "Default")
                        }
                    } else {
                        TextButton(onClick = onSetAsDefault, enabled = source.isConfigured) {
                            Text(text = "Set as default")
                        }
                    }
                }
            )

            // Custom content passed via the content lambda
            // We add a divider and padding for clean visual separation.
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp
                )
            ) {
                // ColumnScope is provided so the content can directly add Column children
                content()
            }
        }
    }
}

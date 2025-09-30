package com.ace.wallpaperrex.ui.components.sources // Or your preferred components package

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ace.wallpaperrex.data.WallpaperSourceItem

/**
 * A generic card to display a wallpaper source item.
 * It handles the common display of name, status, and toggle,
 * and accepts a lambda for custom content specific to the source type.
 */
@Composable
fun SourceSettingCard(
    source: WallpaperSourceItem,
    onToggleEnabled: (id: Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit // Slot for custom body content
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Common header: Name, Status, Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = source.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (source.isEnabled) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Switch(
                    checked = source.isEnabled,
                    onCheckedChange = { onToggleEnabled(source.id) }
                )
            }

            // Custom content passed via the content lambda
            // ColumnScope is provided so the content can directly add Column children
            content()
        }
    }
}

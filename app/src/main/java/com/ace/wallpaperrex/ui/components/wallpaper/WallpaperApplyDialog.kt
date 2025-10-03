package com.ace.wallpaperrex.ui.components.wallpaper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.ace.wallpaperrex.utils.WallpaperHelper

@Composable()
fun WallpaperApplyDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onApply: (WallpaperHelper.ScreenTarget) -> Unit
) {
    if (isVisible) {
        var selectedTarget by remember { mutableStateOf(WallpaperHelper.ScreenTarget.HOME) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Set Wallpaper") },
            text = {
                Column {
                    Text("Choose where to apply the wallpaper:")
                    Spacer(modifier = Modifier.height(8.dp))

                    WallpaperHelper.ScreenTarget.entries.forEach { it ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTarget == it,
                                onClick = { selectedTarget = it }
                            )

                            Spacer(modifier = Modifier.width(8.dp))
                            Text(it.name.lowercase().replaceFirstChar { char -> char.uppercase() })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onApply(selectedTarget) }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
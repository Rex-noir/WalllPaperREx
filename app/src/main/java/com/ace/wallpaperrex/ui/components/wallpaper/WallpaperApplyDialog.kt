package com.ace.wallpaperrex.ui.components.wallpaper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ace.wallpaperrex.utils.WallpaperHelper
import kotlinx.coroutines.launch

@Composable
fun WallpaperApplyDialog(
    isVisible: Boolean,
    imageUrl: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit = {}
) {
    if (isVisible) {
        var selectedTarget by remember { mutableStateOf(WallpaperHelper.ScreenTarget.HOME) }
        var isApplying by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = { if (!isApplying) onDismiss() },
            title = { Text("Set Wallpaper") },
            text = {
                Column {
                    Text("Choose where to apply the wallpaper:")
                    Spacer(modifier = Modifier.height(8.dp))
                    WallpaperHelper.ScreenTarget.entries.forEach { target ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTarget == target,
                                onClick = { selectedTarget = target },
                                enabled = !isApplying
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = target.name.lowercase()
                                    .replaceFirstChar { char -> char.uppercase() },
                                color = if (isApplying)
                                    androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.38f
                                    )
                                else
                                    androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isApplying = true
                        scope.launch {
                            try {
                                WallpaperHelper.applyWallpaperFromUrl(
                                    context,
                                    imageUrl,
                                    selectedTarget
                                )
                                onSuccess()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                onError(e)
                            } finally {
                                isApplying = false
                            }
                        }
                    },
                    enabled = !isApplying
                ) {
                    if (isApplying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isApplying) "Applying..." else "Apply")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isApplying
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
package com.ace.wallpaperrex.ui.components.wallpaper

import android.graphics.Bitmap
import android.util.Log
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ace.wallpaperrex.utils.WallpaperHelper
import com.ace.wallpaperrex.utils.convertToWebpBytes
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.ZoomState

@Composable
fun WallpaperApplyDialog(
    isVisible: Boolean,
    imageBitmap: Bitmap?,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    zoomState: ZoomState? = null,
    onError: (Exception) -> Unit = {},
    scale: Float,
    panOffset: Offset,
    containerSize: IntSize
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
                            if (containerSize.width == 0 || containerSize.height == 0) return@launch
                            var bitmapClone = imageBitmap!!

                            if (scale > 0f) {
                                val bitmapWidth = bitmapClone.width.toFloat()
                                val bitmapHeight = bitmapClone.height.toFloat()
                                val containerWidth = containerSize.width.toFloat()
                                val containerHeight = containerSize.height.toFloat()

                                // Calculate the scaled dimensions
                                val scaledWidth = bitmapWidth * scale
                                val scaledHeight = bitmapHeight * scale

                                // The image is centered in the container and then panned
                                // Calculate where the top-left of the scaled image is in screen space
                                val imageLeftInScreen =
                                    (containerWidth - scaledWidth) / 2f + panOffset.x
                                val imageTopInScreen =
                                    (containerHeight - scaledHeight) / 2f + panOffset.y

                                // The visible area in screen coordinates is (0, 0) to (containerWidth, containerHeight)
                                // Calculate which part of the image is visible
                                val visibleLeftInScreen = 0f - imageLeftInScreen
                                val visibleTopInScreen = 0f - imageTopInScreen
                                val visibleRightInScreen = containerWidth - imageLeftInScreen
                                val visibleBottomInScreen = containerHeight - imageTopInScreen

                                // Convert to bitmap coordinates (unscale)
                                var left = visibleLeftInScreen / scale
                                var top = visibleTopInScreen / scale
                                var right = visibleRightInScreen / scale
                                var bottom = visibleBottomInScreen / scale

                                // Clamp to bitmap bounds
                                left = left.coerceIn(0f, bitmapWidth)
                                top = top.coerceIn(0f, bitmapHeight)
                                right = right.coerceIn(0f, bitmapWidth)
                                bottom = bottom.coerceIn(0f, bitmapHeight)

                                // Ensure valid dimensions
                                if (right <= left) right = (left + 1).coerceAtMost(bitmapWidth)
                                if (bottom <= top) bottom = (top + 1).coerceAtMost(bitmapHeight)

                                // Convert to integers with validation
                                val finalLeft = left.toInt().coerceIn(0, bitmapClone.width - 1)
                                val finalTop = top.toInt().coerceIn(0, bitmapClone.height - 1)
                                val finalWidth = (right - left).toInt()
                                    .coerceIn(1, bitmapClone.width - finalLeft)
                                val finalHeight = (bottom - top).toInt()
                                    .coerceIn(1, bitmapClone.height - finalTop)

                                Log.d(
                                    "Crop", """
                                    Scale: $scale, PanOffset: $panOffset
                                    Bitmap: ${bitmapClone.width}x${bitmapClone.height}
                                    Container: ${containerWidth}x${containerHeight}
                                    Scaled: ${scaledWidth}x${scaledHeight}
                                    Image position in screen: ($imageLeftInScreen, $imageTopInScreen)
                                    Visible in screen: ($visibleLeftInScreen, $visibleTopInScreen) to ($visibleRightInScreen, $visibleBottomInScreen)
                                    Visible in bitmap: ($left, $top) to ($right, $bottom)
                                    Crop: ($finalLeft, $finalTop, $finalWidth, $finalHeight)
                                """.trimIndent()
                                )

                                bitmapClone = Bitmap.createBitmap(
                                    bitmapClone,
                                    finalLeft,
                                    finalTop,
                                    finalWidth,
                                    finalHeight
                                )
                            }

                            val bytes = bitmapClone.convertToWebpBytes()
                            try {
                                WallpaperHelper.applyWallpaper(
                                    context,
                                    rawBytes = bytes,
                                    target = selectedTarget
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
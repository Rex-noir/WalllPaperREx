package com.ace.wallpaperrex.ui.components.wallpaper

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun CreditBar(
    uploaderName: String,
    uploaderUrl: String?,
    modifier: Modifier = Modifier,
) {
    Row {
        Text(
            "By : $uploaderName",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight((1f))
        )

        if (uploaderUrl != null) {
            IconButton(onClick = {
                // Open browser
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                    contentDescription = "Open in browser",
                    tint = Color.White
                )
            }
        }
    }
}
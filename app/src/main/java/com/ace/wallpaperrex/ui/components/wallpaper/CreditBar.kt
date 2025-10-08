package com.ace.wallpaperrex.ui.components.wallpaper

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import coil3.Uri
import androidx.core.net.toUri

@Composable
fun CreditBar(
    uploaderName: String,
    uploaderUrl: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .background(
                Color.Black.copy(alpha = 0.5f)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "By : $uploaderName",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight((1f))
        )

        if (uploaderUrl != null) {
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, uploaderUrl.toUri())
                context.startActivity(intent)
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
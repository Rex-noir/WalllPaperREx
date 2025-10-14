package com.ace.wallpaperrex.ui.components.sources

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceSettingTopBar(
    title: Int,
    onResetClick: () -> Unit
) {
    var expandDropdown by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            Text(text = stringResource(id = title))
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = "Import sources json file"
                )
            }

            IconButton(onClick = { expandDropdown = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Open Source Menu dropdown"
                )

                DropdownMenu(
                    expanded = expandDropdown,
                    onDismissRequest = { expandDropdown = false }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            onResetClick()
                            expandDropdown = false
                        },
                        text = {
                            Text(text = "Reset to default")
                        }, leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Restore,
                                contentDescription = "Reset to default"
                            )
                        }
                    )
                }

            }
        }
    )
}
package com.qalab.launcher.presentation.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var shizukuEnabled by remember { mutableStateOf(false) }
    var firebaseEnabled by remember { mutableStateOf(false) }
    var accessibilityEnabled by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    "Integrations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Shizuku Service") },
                    supportingContent = { Text("Enable elevated operations via Shizuku API") },
                    leadingContent = { Icon(Icons.Default.Code, null) },
                    trailingContent = {
                        Switch(checked = shizukuEnabled, onCheckedChange = { shizukuEnabled = it })
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Firebase Test Lab") },
                    supportingContent = { Text("Connect to Firebase for cloud testing") },
                    leadingContent = { Icon(Icons.Default.Cloud, null) },
                    trailingContent = {
                        Switch(checked = firebaseEnabled, onCheckedChange = { firebaseEnabled = it })
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Accessibility Service") },
                    supportingContent = { Text("Enable for automated UI testing (user-consented)") },
                    leadingContent = { Icon(Icons.Default.Security, null) },
                    trailingContent = {
                        Switch(
                            checked = accessibilityEnabled,
                            onCheckedChange = { accessibilityEnabled = it }
                        )
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Database") },
                    supportingContent = { Text("Room database for monitoring logs & sessions") },
                    leadingContent = { Icon(Icons.Default.Storage, null) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "About",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("QA Lab Launcher") },
                    supportingContent = { Text("Version 1.0.0\nInternal QA Testing Suite") },
                    leadingContent = { Icon(Icons.Default.Info, null) }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

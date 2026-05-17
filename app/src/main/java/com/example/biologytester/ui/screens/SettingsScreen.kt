package com.example.biologytester.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var darkMode by remember { mutableStateOf(true) }
            var haptics by remember { mutableStateOf(true) }
            var sounds by remember { mutableStateOf(true) }
            var autoAdvance by remember { mutableStateOf(false) }

            Text("Preferences", style = MaterialTheme.typography.titleLarge)

            ListItem(
                headlineContent = { Text("Dark Mode") },
                supportingContent = { Text("Toggle system dark mode (Demo)") },
                trailingContent = { Switch(checked = darkMode, onCheckedChange = { darkMode = it }) }
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Haptic Feedback") },
                supportingContent = { Text("Vibrate on correct/wrong answers") },
                trailingContent = { Switch(checked = haptics, onCheckedChange = { haptics = it }) }
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Sound Effects") },
                trailingContent = { Switch(checked = sounds, onCheckedChange = { sounds = it }) }
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Auto-Advance") },
                supportingContent = { Text("Automatically go to next question after answering") },
                trailingContent = { Switch(checked = autoAdvance, onCheckedChange = { autoAdvance = it }) }
            )
        }
    }
}

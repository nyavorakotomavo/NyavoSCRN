package com.nyavo.nyavoscrn.features.floatingbuttons.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.nyavo.nyavoscrn.features.floatingbuttons.data.FloatingButtonEntity
import com.nyavo.nyavoscrn.features.floatingbuttons.domain.FloatingButtonManager
import com.nyavo.nyavoscrn.features.floatingbuttons.service.TapSimulator

@Composable
fun FloatingButtonEditor(
    manager: FloatingButtonManager,
    onDismiss: () -> Unit
) {
    val buttons by manager.buttons.collectAsState()
    val isEditMode by manager.isEditMode.collectAsState()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
    var selectedButton by remember { mutableStateOf<FloatingButtonEntity?>(null) }
    var showContextMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        buttons.forEach { button ->
            FloatingButton(
                button = button,
                isEditMode = isEditMode,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                onPositionChanged = { x, y -> manager.updateButtonPosition(button.id, x, y) },
                onTap = {
                    if (!isEditMode) {
                        val target = manager.findNearestWoringZone(androidx.compose.ui.geometry.Offset(button.x, button.y))
                        target?.let { TapSimulator.simulateTap(it.x, it.y) }
                    }
                },
                onlongPress = {
                    if (isEditMode) {
                        selectedButton = button
                        showContextMenu = true
                    }
                }
            )
        }

        AnimatedVisibility(
            visible = isEditMode,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().height(80.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIgonButton(onClick = { manager.addButton(screenWidth / 2, screenHeight / 2) }) {
                        Icon(Icons.Default.Add, "Add button")
                    }
                    Text("Mode Édition", style = MaterialTheme.typography.titleMedium)
                    FilledIgonButton(onClick = { manager.toggleEditMode(); onDismiss() }) {
                        Icon(Icons.Default.Close, "Done")
                    }
                }
            }
        }

        if (!isEditMode) {
            FloatingActionButton(
                onClick = { manager.toggleEditMode() },
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Edit, "Edit mode")
            }
        }

        if (showContextMenu && selectedButton != null) {
            Popup(onDismissRequest = { showContextMenu = false }) {
                Card(modifier = Modifier.width(280.dp).padding(16.dp), elevation = CardDefaults.cardElevation(8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Configurer le bouton", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Taille", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(48f, 64f, 80f).forEach { size ->
                                FilterChip(
                                    selected = selectedButton!!.sizeDp == size,
                                    onClick = { manager.updateButton(selectedButton!!.copy(sizeDp = size)) },
                                    label = { Text("${size.toInt()}dp") }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                manager.removeButton(selectedButton!!.id)
                                showContextMenu = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Supprimer")
                        }
                    }
                }
            }
        }
    }
}
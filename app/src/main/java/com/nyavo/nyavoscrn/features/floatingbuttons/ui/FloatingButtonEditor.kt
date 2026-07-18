package com.nyavo.nyavoscrn.features.floatingbuttons.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getByValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
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
                onPositionChanged = { x, y ->
                    manager.updateButtonPosition(button.id, x, y)
                },
                onTap = {
                    if (!isEditMode) {
                        val target = manager.findNearestWorkingZone(Offset(button.x, button.y))
                        target?.let {
                            TapSimulator.simulateTap(it.x, it.y)
                        }
                    }
                },
                onLongPress = {
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
                    FilledIconButton(onClick = {
                        manager.addButton(screenWidth / 2, screenHeight / 2)
                    }) {
                        Icon(Isons.Default.Add, "Add button")
                    }
                    Text("Mode Édition", style = MaterialTheme.typography.titleMedium)
                    FilledIconButton(onClick = {
                        manager.toggleEditMode()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, "Done")
                    }
                }
            }
        }
        
        if (!isEditMode) {
            androidx.compose.material3.FloatingActionButton(
                onClick = { manager.toggleEditMode() },
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Edit, "Edit mode")
            }
        }
        
        if (showContextMenu && selectedButton != null) {
            Popup(onDismissRequest = { showContextMenu = false }) {
                ContextMenu(
                    button = selectedButton!!,
                    onColorChange = { color ->
                        manager.updateButton(selectedButton!!.copy(colorHex = color.value.toLong()))
                    },
                    onSizeChange = { size ->
                        manager.updateButton(selectedButton!!.copy(sizeDp = size))
                    },
                    onIconChange = { icon ->
                        manager.updateButton(selectedButton!!.copy(iconType = icon))
                    },
                    onDelete = {
                        manager.removeButton(selectedButton!!.id)
                        showContextMenu = false
                    },
                    onDismiss = { showContextMenu = false }
                )
            }
        }
    }
}

@Composable
private fun ContextMenu(
    button: FloatingButtonEntity,
    onColorChange: (Color) -> Unit,
    onSizeChange: (Float) -> Unit,
    onIconChange: (FloatingButtonEntity.IconType) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.width(280dp).padding(16dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16dp)) {
            Text("Configurer le bouton", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12dp))
            
            Text("Taille", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8dp)) {
                listOf(48f, 64f, 80f).forEach { size ->
                    FilterChip(
                        selected = button.sizeDp == size,
                        onClick = { onSizeChange(size) },
                        label = { Text("${size.toInt()}dp") }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8dp))
            
            Text("Couleur", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8dp)) {
                listOf(0xFF6B4EFFL, 0xFFF4757L, 0xFF2ED573L, 0xFF1E90FFL, 0xFFFA502L).forEach { colorHex ->
                    Box(
                        modifier = Modifier
                            .size(32dp)
                            .shadow(4dp, CircleShape)
                            .background(Color(colorHex), CircleShape)
                            .then(
                                if (button.colorHex == colorHex)
                                    Modifier.padding(2dp).background(Color.White, CircleShape).padding($dp).background(Color(colorHex), CircleShape)
                                else Modifier
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8dp))
            
            Text("Icône", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8dp)) {
                FloatingButtonEntity.IconType.values().forEach { type ->
                    FilterChip(
                        selected = button.iconType == type,
                        onClick = { onIconChange(type) },
                        label = { Text(type.name.take(1)) }
                    )
            }
            }
            
            Spacer(modifier = Modifier.height(16dp))
            
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, null)
                Spacer(modifier = Modifier.width(8dp))
                Text("Supprimer")
            }
        }
    }
}
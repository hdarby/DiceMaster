package com.hdarby.dicemaster.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.viewmodel.CharacterViewModel
import com.hdarby.dicemaster.viewmodel.WeaponViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeaponScreen(
    weaponViewModel: WeaponViewModel = koinViewModel(),
    characterViewModel: CharacterViewModel = koinViewModel()
) {
    val uiState by weaponViewModel.uiState.collectAsState()
    val characterState by characterViewModel.uiState.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingWeapon by remember { mutableStateOf<Weapon?>(null) }
    var assigningWeapon by remember { mutableStateOf<Weapon?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weapon Repository", modifier = Modifier.testTag("screen_title_weapons")) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Weapon")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.weapons) { weapon ->
                        WeaponCard(
                            weapon = weapon,
                            onEdit = { editingWeapon = it },
                            onDelete = { weaponViewModel.deleteWeapon(it) },
                            onAssign = { assigningWeapon = it }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddEditWeaponDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = {
                    weaponViewModel.addWeapon(it)
                    showAddDialog = false
                }
            )
        }

        editingWeapon?.let { weapon ->
            AddEditWeaponDialog(
                weapon = weapon,
                onDismiss = { editingWeapon = null },
                onConfirm = {
                    weaponViewModel.updateWeapon(it)
                    editingWeapon = null
                }
            )
        }

        assigningWeapon?.let { weapon ->
            AssignWeaponDialog(
                weapon = weapon,
                characters = characterState.characters.map { it.character },
                onDismiss = { assigningWeapon = null },
                onConfirm = { characterId ->
                    weaponViewModel.assignWeaponToCharacter(characterId, weapon.id)
                    assigningWeapon = null
                }
            )
        }
    }
}

@Composable
fun WeaponCard(
    weapon: Weapon,
    onEdit: (Weapon) -> Unit,
    onDelete: (Weapon) -> Unit,
    onAssign: (Weapon) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = weapon.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${weapon.type})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Text(
                        text = "${weapon.damageDice} ${weapon.damageType} (Modifier: ${weapon.modifier})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row {
                    IconButton(onClick = { onAssign(weapon) }) {
                        Icon(Icons.Default.Link, contentDescription = "Assign")
                    }
                    IconButton(onClick = { onEdit(weapon) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { onDelete(weapon) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWeaponDialog(
    weapon: Weapon? = null,
    onDismiss: () -> Unit,
    onConfirm: (Weapon) -> Unit
) {
    var name by remember { mutableStateOf(weapon?.name ?: "") }
    var type by remember { mutableStateOf(weapon?.type ?: "") }
    var dice by remember { mutableStateOf(weapon?.damageDice ?: "") }
    var damageType by remember { mutableStateOf(weapon?.damageType ?: "") }
    var mod by remember { mutableStateOf(weapon?.modifier?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (weapon == null) "Add Weapon" else "Edit Weapon") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Weapon Type (e.g. Sword)") })
                OutlinedTextField(value = dice, onValueChange = { dice = it }, label = { Text("Damage Dice (e.g. 1d8)") })
                OutlinedTextField(value = damageType, onValueChange = { damageType = it }, label = { Text("Damage Type") })
                OutlinedTextField(
                    value = mod,
                    onValueChange = { input ->
                        if (input.isEmpty() || input == "-" || input.toIntOrNull() != null) {
                            mod = input
                        }
                    },
                    label = { Text("Modifier") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(Weapon(
                    id = weapon?.id ?: 0, 
                    name = name, 
                    type = type,
                    damageDice = dice, 
                    damageType = damageType, 
                    modifier = mod.toIntOrNull() ?: 0
                ))
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AssignWeaponDialog(
    weapon: Weapon,
    characters: List<com.hdarby.dicemaster.domain.model.Character>,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign ${weapon.name}") },
        text = {
            LazyColumn {
                items(characters) { character ->
                    ListItem(
                        headlineContent = { Text(character.name) },
                        supportingContent = { Text(character.race) },
                        modifier = Modifier.fillMaxWidth().clickable {
                            onConfirm(character.id)
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

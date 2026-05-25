package com.hdarby.dicemaster.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hdarby.dicemaster.R
import com.hdarby.dicemaster.domain.model.DamageDice
import com.hdarby.dicemaster.domain.model.DamageType
import com.hdarby.dicemaster.domain.model.UserRole
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.domain.model.WeaponType
import com.hdarby.dicemaster.viewmodel.WeaponViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeaponScreen(
    weaponViewModel: WeaponViewModel = koinViewModel(),
    editWeaponId: Long? = null,
    onLeaveSession: () -> Unit
) {
    val uiState by weaponViewModel.uiState.collectAsState()
    val isDungeonMaster = uiState.userRole !is UserRole.Player

    var showAddDialog by remember { mutableStateOf(false) }
    var editingWeapon by remember { mutableStateOf<Weapon?>(null) }
    var editConsumed by remember(editWeaponId) { mutableStateOf(false) }

    LaunchedEffect(editWeaponId, uiState.weapons) {
        if (editWeaponId != null && !editConsumed) {
            uiState.weapons.find { it.id == editWeaponId }?.let { weapon ->
                editingWeapon = weapon
                editConsumed = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_weapons), modifier = Modifier.testTag("screen_title_weapons")) },
                actions = {
                    IconButton(onClick = onLeaveSession) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(R.string.content_desc_leave_session)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (isDungeonMaster) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.content_desc_add_weapon))
                }
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
                            isDungeonMaster = isDungeonMaster,
                            onEdit = { editingWeapon = it },
                            onDelete = { weaponViewModel.deleteWeapon(it) }
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
    }
}

@Composable
fun WeaponCard(
    weapon: Weapon,
    isDungeonMaster: Boolean = true,
    onEdit: (Weapon) -> Unit,
    onDelete: (Weapon) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
                            text = stringResource(R.string.format_weapon_type_parenthesized, weapon.weaponType.displayName),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    val hitSign = if (weapon.toHitBonus >= 0) "+" else ""
                    val dmgSign = if (weapon.damageModifier >= 0) "+" else ""
                    Text(
                        text = "${weapon.damageDice.displayName} ${weapon.damageType.displayName} · ${hitSign}${weapon.toHitBonus} to hit · ${dmgSign}${weapon.damageModifier} dmg",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (weapon.isAtomic) {
                        Text(
                            text = stringResource(R.string.label_weapon_atomic),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                if (isDungeonMaster) {
                    Row {
                        IconButton(onClick = { onEdit(weapon) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.content_desc_edit))
                        }
                        IconButton(onClick = { onDelete(weapon) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.content_desc_delete))
                        }
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
    var selectedType by remember { mutableStateOf(weapon?.weaponType ?: WeaponType.SIMPLE_MELEE) }
    var selectedDice by remember { mutableStateOf(weapon?.damageDice ?: DamageDice.D6) }
    var selectedDamageType by remember { mutableStateOf(weapon?.damageType ?: DamageType.SLASHING) }
    var toHitBonus by remember { mutableStateOf(weapon?.toHitBonus?.toString() ?: "0") }
    var damageModifier by remember { mutableStateOf(weapon?.damageModifier?.toString() ?: "0") }
    var isAtomic by remember { mutableStateOf(weapon?.isAtomic ?: true) }

    var typeExpanded by remember { mutableStateOf(false) }
    var diceExpanded by remember { mutableStateOf(false) }
    var damageTypeExpanded by remember { mutableStateOf(false) }

    val isSignedInt = { it: String -> it.isEmpty() || it == "-" || it.toIntOrNull() != null }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (weapon == null) stringResource(R.string.title_add_weapon) else stringResource(R.string.title_edit_weapon)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.label_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                // ── Weapon Type dropdown ─────────────────────────────────────
                item {
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.label_weapon_type)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            WeaponType.entries.forEach { wt ->
                                DropdownMenuItem(
                                    text = { Text(wt.displayName) },
                                    onClick = { selectedType = wt; typeExpanded = false }
                                )
                            }
                        }
                    }
                }
                // ── Damage Dice dropdown ─────────────────────────────────────
                item {
                    ExposedDropdownMenuBox(
                        expanded = diceExpanded,
                        onExpandedChange = { diceExpanded = !diceExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedDice.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.label_damage_dice)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diceExpanded) },
                            modifier = Modifier
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = diceExpanded, onDismissRequest = { diceExpanded = false }) {
                            DamageDice.entries.forEach { dd ->
                                DropdownMenuItem(
                                    text = { Text(dd.displayName) },
                                    onClick = { selectedDice = dd; diceExpanded = false }
                                )
                            }
                        }
                    }
                }
                // ── Damage Type dropdown ─────────────────────────────────────
                item {
                    ExposedDropdownMenuBox(
                        expanded = damageTypeExpanded,
                        onExpandedChange = { damageTypeExpanded = !damageTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedDamageType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.label_damage_type)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = damageTypeExpanded) },
                            modifier = Modifier
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = damageTypeExpanded, onDismissRequest = { damageTypeExpanded = false }) {
                            DamageType.entries.forEach { dt ->
                                DropdownMenuItem(
                                    text = { Text(dt.displayName) },
                                    onClick = { selectedDamageType = dt; damageTypeExpanded = false }
                                )
                            }
                        }
                    }
                }
                // ── Numeric fields ───────────────────────────────────────────
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = toHitBonus,
                            onValueChange = { if (isSignedInt(it)) toHitBonus = it },
                            label = { Text(stringResource(R.string.label_to_hit_bonus)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = damageModifier,
                            onValueChange = { if (isSignedInt(it)) damageModifier = it },
                            label = { Text(stringResource(R.string.label_damage_modifier)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                // ── Atomic checkbox ──────────────────────────────────────────
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(checked = isAtomic, onCheckedChange = { isAtomic = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.label_weapon_atomic_checkbox), style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.label_weapon_atomic_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    Weapon(
                        id = weapon?.id ?: 0,
                        name = name,
                        weaponType = selectedType,
                        damageDice = selectedDice,
                        damageType = selectedDamageType,
                        toHitBonus = toHitBonus.toIntOrNull() ?: 0,
                        damageModifier = damageModifier.toIntOrNull() ?: 0,
                        isAtomic = isAtomic
                    )
                )
            }) {
                Text(stringResource(R.string.button_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}

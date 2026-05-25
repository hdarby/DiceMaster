package com.hdarby.dicemaster.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hdarby.dicemaster.R
import com.hdarby.dicemaster.ui.theme.PrimaryGreen
import com.hdarby.dicemaster.ui.theme.PrimaryGreenDark
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.CharacterItemEntry
import com.hdarby.dicemaster.domain.model.CharacterWeaponEntry
import com.hdarby.dicemaster.domain.model.CharacterWithWeapons
import com.hdarby.dicemaster.domain.model.ConsumableItem
import com.hdarby.dicemaster.domain.model.Stats
import com.hdarby.dicemaster.domain.model.UserRole
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.viewmodel.CharacterViewModel
import com.hdarby.dicemaster.viewmodel.ItemViewModel
import com.hdarby.dicemaster.viewmodel.WeaponViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterScreen(
    viewModel: CharacterViewModel = koinViewModel(),
    itemViewModel: ItemViewModel = koinViewModel(),
    weaponViewModel: WeaponViewModel = koinViewModel(),
    onNavigateToEditWeapon: (Weapon) -> Unit,
    onLeaveSession: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val itemState by itemViewModel.uiState.collectAsState()
    val weaponState by weaponViewModel.uiState.collectAsState()
    val isDungeonMaster = uiState.userRole !is UserRole.Player
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCharacter by remember { mutableStateOf<Character?>(null) }
    var assignItemToCharacter by remember { mutableStateOf<Character?>(null) }
    var assignWeaponToCharacter by remember { mutableStateOf<Character?>(null) }
    var confirmDeathCharacter by remember { mutableStateOf<Character?>(null) }

    // Atomic weapon IDs already assigned somewhere — used to prevent double-assigning atomic weapons
    val assignedAtomicWeaponIds = remember(uiState.characters) {
        uiState.characters
            .flatMap { it.weapons }
            .filter { it.weapon.isAtomic }
            .map { it.weapon.id }
            .toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_characters), modifier = Modifier.testTag("screen_title_characters")) },
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
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.content_desc_add_character))
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
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.characters) { characterWithWeapons ->
                        val characterItems = itemState.itemsByCharacterId[characterWithWeapons.character.id]
                            ?: emptyList()
                        CharacterCard(
                            characterWithWeapons = characterWithWeapons,
                            items = characterItems,
                            isDungeonMaster = isDungeonMaster,
                            onEdit = { editingCharacter = it },
                            onDelete = { viewModel.deleteCharacter(it) },
                            onWeaponClick = if (isDungeonMaster) onNavigateToEditWeapon else { _ -> },
                            onUnassignWeapon = { assignmentId -> viewModel.unassignWeapon(assignmentId) },
                            onAssignWeapon = { assignWeaponToCharacter = it },
                            onAssignItem = { assignItemToCharacter = it },
                            onIncrementItem = { entry ->
                                itemViewModel.incrementQuantity(entry.assignmentId, entry.quantity)
                            },
                            onDecrementItem = { entry ->
                                itemViewModel.decrementQuantity(entry.assignmentId, entry.quantity)
                            },
                            onHeal = { viewModel.heal(it) },
                            onDamage = { viewModel.damage(it) },
                            onDeathSaveChanged = { character, failures ->
                                if (failures == MAX_DEATH_SAVE_FAILURES) {
                                    // Stage death save update and await confirmation
                                    viewModel.setDeathSaveFailures(character, failures)
                                    confirmDeathCharacter = character.copy(deathSaveFailures = failures)
                                } else {
                                    viewModel.setDeathSaveFailures(character, failures)
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddEditCharacterDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = {
                    viewModel.addCharacter(it)
                    showAddDialog = false
                }
            )
        }

        editingCharacter?.let { character ->
            AddEditCharacterDialog(
                character = character,
                onDismiss = { editingCharacter = null },
                onConfirm = {
                    viewModel.updateCharacter(it)
                    editingCharacter = null
                }
            )
        }

        assignItemToCharacter?.let { character ->
            AssignItemDialog(
                characterName = character.name,
                availableItems = itemState.items,
                onDismiss = { assignItemToCharacter = null },
                onAssign = { item ->
                    itemViewModel.assignItem(character.id, item.id)
                    assignItemToCharacter = null
                }
            )
        }

        assignWeaponToCharacter?.let { character ->
            AssignWeaponToCharacterDialog(
                characterName = character.name,
                availableWeapons = weaponState.weapons,
                disabledAtomicWeaponIds = assignedAtomicWeaponIds,
                onDismiss = { assignWeaponToCharacter = null },
                onAssign = { weapon ->
                    viewModel.assignWeapon(character.id, weapon.id)
                    assignWeaponToCharacter = null
                }
            )
        }

        confirmDeathCharacter?.let { character ->
            ConfirmDeathDialog(
                characterName = character.name,
                onConfirm = {
                    viewModel.markDead(character)
                    confirmDeathCharacter = null
                },
                onDismiss = {
                    // Revert to 2 failed saves — the user cancelled the death confirmation
                    viewModel.setDeathSaveFailures(character, MAX_DEATH_SAVE_FAILURES - 1)
                    confirmDeathCharacter = null
                }
            )
        }
    }
}

private const val MAX_DEATH_SAVE_FAILURES = 3

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CharacterCard(
    characterWithWeapons: CharacterWithWeapons,
    items: List<CharacterItemEntry> = emptyList(),
    isDungeonMaster: Boolean = true,
    onEdit: (Character) -> Unit,
    onDelete: (Character) -> Unit,
    onWeaponClick: (Weapon) -> Unit = {},
    onUnassignWeapon: (Long) -> Unit = {},
    onAssignWeapon: (Character) -> Unit = {},
    onAssignItem: (Character) -> Unit = {},
    onIncrementItem: (CharacterItemEntry) -> Unit = {},
    onDecrementItem: (CharacterItemEntry) -> Unit = {},
    onHeal: (Character) -> Unit = {},
    onDamage: (Character) -> Unit = {},
    onDeathSaveChanged: (Character, Int) -> Unit = { _, _ -> }
) {
    val character = characterWithWeapons.character
    val weaponEntries = characterWithWeapons.weapons

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Header row: name / race / edit / delete ──────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = character.race, style = MaterialTheme.typography.bodyMedium)
                }
                if (isDungeonMaster) {
                    Row {
                        IconButton(onClick = { onEdit(character) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.content_desc_edit))
                        }
                        IconButton(onClick = { onDelete(character) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.content_desc_delete))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Stats row ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(stringResource(R.string.label_stat_str), character.stats.strength, character.stats.strengthModifier)
                StatItem(stringResource(R.string.label_stat_dex), character.stats.dexterity, character.stats.dexterityModifier)
                StatItem(stringResource(R.string.label_stat_con), character.stats.constitution, character.stats.constitutionModifier)
                StatItem(stringResource(R.string.label_stat_int), character.stats.intelligence, character.stats.intelligenceModifier)
                StatItem(stringResource(R.string.label_stat_wis), character.stats.wisdom, character.stats.wisdomModifier)
                StatItem(stringResource(R.string.label_stat_cha), character.stats.charisma, character.stats.charismaModifier)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Hit Points section ───────────────────────────────────────────
            HitPointsSection(
                character = character,
                onHeal = onHeal,
                onDamage = onDamage,
                onDeathSaveChanged = onDeathSaveChanged
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Weapons section ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.label_weapons_section),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (isDungeonMaster) {
                    IconButton(
                        onClick = { onAssignWeapon(character) },
                        modifier = Modifier.testTag("assign_weapon_${character.id}")
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.content_desc_add_weapon),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
            if (weaponEntries.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    weaponEntries.forEach { entry ->
                        AssistChip(
                            onClick = { onWeaponClick(entry.weapon) },
                            label = { Text(stringResource(R.string.format_weapon_chip_label, entry.weapon.name, entry.weapon.type)) },
                            trailingIcon = if (isDungeonMaster) {
                                {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.content_desc_unassign_weapon),
                                        modifier = Modifier.clickable { onUnassignWeapon(entry.assignmentId) }
                                    )
                                }
                            } else null,
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Items section ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.label_items_section),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                IconButton(
                    onClick = { onAssignItem(character) },
                    modifier = Modifier.testTag("assign_item_${character.id}")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.content_desc_add_item),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (items.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items.forEach { entry ->
                        ItemQuantityRow(
                            entry = entry,
                            onIncrement = { onIncrementItem(entry) },
                            onDecrement = { onDecrementItem(entry) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HitPointsSection(
    character: Character,
    onHeal: (Character) -> Unit,
    onDamage: (Character) -> Unit,
    onDeathSaveChanged: (Character, Int) -> Unit
) {
    when {
        character.isDead -> {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(R.string.label_status_dead),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
        character.currentHitPoints == 0 -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = stringResource(R.string.label_status_down),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.label_hp_display, 0, character.maxHitPoints),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { onHeal(character) }) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = stringResource(R.string.content_desc_heal),
                            tint = if (isSystemInDarkTheme()) PrimaryGreenDark else PrimaryGreen
                        )
                    }
                }
                DeathSaveTracker(
                    failures = character.deathSaveFailures,
                    onFailuresChanged = { newCount -> onDeathSaveChanged(character, newCount) }
                )
            }
        }
        else -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.label_hp_display, character.currentHitPoints, character.maxHitPoints),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onDamage(character) },
                        enabled = character.currentHitPoints > 0
                    ) {
                        Icon(
                            Icons.Default.HeartBroken,
                            contentDescription = stringResource(R.string.content_desc_damage),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(
                        onClick = { onHeal(character) },
                        enabled = character.currentHitPoints < character.maxHitPoints
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = stringResource(R.string.content_desc_heal),
                            tint = if (isSystemInDarkTheme()) PrimaryGreenDark else PrimaryGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeathSaveTracker(
    failures: Int,
    onFailuresChanged: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.label_death_saves_failed),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        for (index in 1..MAX_DEATH_SAVE_FAILURES) {
            Checkbox(
                checked = failures >= index,
                onCheckedChange = { checked ->
                    onFailuresChanged(if (checked) index else index - 1)
                }
            )
        }
    }
}

@Composable
fun ConfirmDeathDialog(
    characterName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_confirm_death)) },
        text = { Text(stringResource(R.string.message_confirm_death, characterName)) },
        confirmButton = {
            Button(onClick = onConfirm) { Text(stringResource(R.string.button_mark_dead)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.button_cancel)) }
        }
    )
}

@Composable
fun StatItem(label: String, value: Int, modifier: Int) {
    val modifierText = if (modifier >= 0) "+$modifier" else modifier.toString()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.format_stat_modifier, modifierText),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCharacterDialog(
    character: Character? = null,
    onDismiss: () -> Unit,
    onConfirm: (Character) -> Unit
) {
    var name by remember { mutableStateOf(character?.name ?: "") }
    var race by remember { mutableStateOf(character?.race ?: "") }
    var str by remember { mutableStateOf(character?.stats?.strength?.toString() ?: "10") }
    var strMod by remember { mutableStateOf(character?.stats?.strengthModifier?.toString() ?: "0") }
    var dex by remember { mutableStateOf(character?.stats?.dexterity?.toString() ?: "10") }
    var dexMod by remember { mutableStateOf(character?.stats?.dexterityModifier?.toString() ?: "0") }
    var con by remember { mutableStateOf(character?.stats?.constitution?.toString() ?: "10") }
    var conMod by remember { mutableStateOf(character?.stats?.constitutionModifier?.toString() ?: "0") }
    var intel by remember { mutableStateOf(character?.stats?.intelligence?.toString() ?: "10") }
    var intMod by remember { mutableStateOf(character?.stats?.intelligenceModifier?.toString() ?: "0") }
    var wis by remember { mutableStateOf(character?.stats?.wisdom?.toString() ?: "10") }
    var wisMod by remember { mutableStateOf(character?.stats?.wisdomModifier?.toString() ?: "0") }
    var cha by remember { mutableStateOf(character?.stats?.charisma?.toString() ?: "10") }
    var chaMod by remember { mutableStateOf(character?.stats?.charismaModifier?.toString() ?: "0") }
    var maxHp by remember { mutableStateOf(character?.maxHitPoints?.toString() ?: "10") }

    val isValidInput = { it: String -> it.isEmpty() || it == "-" || it.toIntOrNull() != null }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (character == null) stringResource(R.string.title_add_character) else stringResource(R.string.title_edit_character)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.label_name)) })
                }
                item {
                    OutlinedTextField(value = race, onValueChange = { race = it }, label = { Text(stringResource(R.string.label_race)) })
                }
                item {
                    OutlinedTextField(
                        value = maxHp,
                        onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) maxHp = it },
                        label = { Text(stringResource(R.string.label_max_hp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = str, onValueChange = { if (isValidInput(it)) str = it }, label = { Text(stringResource(R.string.label_stat_str)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = strMod, onValueChange = { if (isValidInput(it)) strMod = it }, label = { Text(stringResource(R.string.label_stat_mod)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = dex, onValueChange = { if (isValidInput(it)) dex = it }, label = { Text(stringResource(R.string.label_stat_dex)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = dexMod, onValueChange = { if (isValidInput(it)) dexMod = it }, label = { Text(stringResource(R.string.label_stat_mod)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = con, onValueChange = { if (isValidInput(it)) con = it }, label = { Text(stringResource(R.string.label_stat_con)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = conMod, onValueChange = { if (isValidInput(it)) conMod = it }, label = { Text(stringResource(R.string.label_stat_mod)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = intel, onValueChange = { if (isValidInput(it)) intel = it }, label = { Text(stringResource(R.string.label_stat_int)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = intMod, onValueChange = { if (isValidInput(it)) intMod = it }, label = { Text(stringResource(R.string.label_stat_mod)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = wis, onValueChange = { if (isValidInput(it)) wis = it }, label = { Text(stringResource(R.string.label_stat_wis)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = wisMod, onValueChange = { if (isValidInput(it)) wisMod = it }, label = { Text(stringResource(R.string.label_stat_mod)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = cha, onValueChange = { if (isValidInput(it)) cha = it }, label = { Text(stringResource(R.string.label_stat_cha)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = chaMod, onValueChange = { if (isValidInput(it)) chaMod = it }, label = { Text(stringResource(R.string.label_stat_mod)) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val stats = Stats(
                    strength = str.toIntOrNull() ?: 10, strengthModifier = strMod.toIntOrNull() ?: 0,
                    dexterity = dex.toIntOrNull() ?: 10, dexterityModifier = dexMod.toIntOrNull() ?: 0,
                    constitution = con.toIntOrNull() ?: 10, constitutionModifier = conMod.toIntOrNull() ?: 0,
                    intelligence = intel.toIntOrNull() ?: 10, intelligenceModifier = intMod.toIntOrNull() ?: 0,
                    wisdom = wis.toIntOrNull() ?: 10, wisdomModifier = wisMod.toIntOrNull() ?: 0,
                    charisma = cha.toIntOrNull() ?: 10, charismaModifier = chaMod.toIntOrNull() ?: 0
                )
                val newMaxHp = maxHp.toIntOrNull()?.coerceAtLeast(1) ?: 10
                val newCurrentHp = when {
                    character == null -> newMaxHp
                    newMaxHp > character.maxHitPoints -> {
                        val hpIncrease = newMaxHp - character.maxHitPoints
                        (character.currentHitPoints + hpIncrease).coerceAtMost(newMaxHp)
                    }
                    else -> character.currentHitPoints.coerceAtMost(newMaxHp)
                }
                onConfirm(
                    Character(
                        id = character?.id ?: 0,
                        name = name,
                        race = race,
                        stats = stats,
                        maxHitPoints = newMaxHp,
                        currentHitPoints = newCurrentHp,
                        deathSaveFailures = character?.deathSaveFailures ?: 0,
                        isDead = character?.isDead ?: false
                    )
                )
            }) {
                Text(stringResource(R.string.button_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.button_cancel)) }
        }
    )
}

@Composable
fun ItemQuantityRow(
    entry: CharacterItemEntry,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = entry.item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            if (entry.item.description.isNotBlank()) {
                Text(text = entry.item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement) {
                Icon(Icons.Default.RemoveCircleOutline, contentDescription = stringResource(R.string.content_desc_decrement), tint = MaterialTheme.colorScheme.error)
            }
            Text(
                text = stringResource(R.string.label_item_quantity, entry.quantity),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(onClick = onIncrement) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = stringResource(R.string.content_desc_increment), tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun AssignItemDialog(
    characterName: String,
    availableItems: List<ConsumableItem>,
    onDismiss: () -> Unit,
    onAssign: (ConsumableItem) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_assign_item, characterName)) },
        text = {
            if (availableItems.isEmpty()) {
                Text(stringResource(R.string.label_no_items_to_assign))
            } else {
                LazyColumn {
                    items(availableItems) { item ->
                        TextButton(
                            onClick = { onAssign(item) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(item.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                if (item.description.isNotBlank()) {
                                    Text(item.description, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.button_cancel)) }
        }
    )
}

@Composable
fun AssignWeaponToCharacterDialog(
    characterName: String,
    availableWeapons: List<Weapon>,
    disabledAtomicWeaponIds: Set<Long>,
    onDismiss: () -> Unit,
    onAssign: (Weapon) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_assign_weapon, characterName)) },
        text = {
            if (availableWeapons.isEmpty()) {
                Text(stringResource(R.string.label_no_weapons_to_assign))
            } else {
                LazyColumn {
                    items(availableWeapons) { weapon ->
                        val isDisabled = weapon.isAtomic && weapon.id in disabledAtomicWeaponIds
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.format_weapon_chip_label, weapon.name, weapon.type),
                                    color = if (isDisabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                            else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.format_weapon_damage_details, weapon.damageDice, weapon.damageType, weapon.modifier),
                                    color = if (isDisabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingContent = if (weapon.isAtomic) {
                                { Text(stringResource(R.string.label_weapon_atomic), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary) }
                            } else null,
                            modifier = Modifier.fillMaxWidth().let {
                                if (isDisabled) it else it.clickable { onAssign(weapon) }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.button_cancel)) }
        }
    )
}


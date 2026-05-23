package com.hdarby.dicemaster.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hdarby.dicemaster.R
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.CharacterItemEntry
import com.hdarby.dicemaster.domain.model.CharacterWithWeapons
import com.hdarby.dicemaster.domain.model.ConsumableItem
import com.hdarby.dicemaster.domain.model.Stats
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.viewmodel.CharacterViewModel
import com.hdarby.dicemaster.viewmodel.ItemViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterScreen(
    viewModel: CharacterViewModel = koinViewModel(),
    itemViewModel: ItemViewModel = koinViewModel(),
    onNavigateToEditWeapon: (Weapon) -> Unit,
    onLeaveSession: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val itemState by itemViewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCharacter by remember { mutableStateOf<Character?>(null) }
    var assignItemToCharacter by remember { mutableStateOf<Character?>(null) }

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
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.content_desc_add_character))
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
                            onEdit = { editingCharacter = it },
                            onDelete = { viewModel.deleteCharacter(it) },
                            onWeaponClick = onNavigateToEditWeapon,
                            onAssignItem = { assignItemToCharacter = it },
                            onIncrementItem = { entry ->
                                itemViewModel.incrementQuantity(
                                    characterWithWeapons.character.id,
                                    entry.item.id,
                                    entry.quantity
                                )
                            },
                            onDecrementItem = { entry ->
                                itemViewModel.decrementQuantity(
                                    characterWithWeapons.character.id,
                                    entry.item.id,
                                    entry.quantity
                                )
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
                assignedItemIds = (itemState.itemsByCharacterId[character.id] ?: emptyList())
                    .map { it.item.id }.toSet(),
                onDismiss = { assignItemToCharacter = null },
                onAssign = { item ->
                    itemViewModel.assignItem(character.id, item.id)
                    assignItemToCharacter = null
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CharacterCard(
    characterWithWeapons: CharacterWithWeapons,
    items: List<CharacterItemEntry> = emptyList(),
    onEdit: (Character) -> Unit,
    onDelete: (Character) -> Unit,
    onWeaponClick: (Weapon) -> Unit = {},
    onAssignItem: (Character) -> Unit = {},
    onIncrementItem: (CharacterItemEntry) -> Unit = {},
    onDecrementItem: (CharacterItemEntry) -> Unit = {}
) {
    val character = characterWithWeapons.character
    val weapons = characterWithWeapons.weapons

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
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = character.race,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row {
                    IconButton(onClick = { onEdit(character) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.content_desc_edit))
                    }
                    IconButton(onClick = { onDelete(character) }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.content_desc_delete))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
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

            if (weapons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.label_weapons_section),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    weapons.forEach { weapon ->
                        AssistChip(
                            onClick = { onWeaponClick(weapon) },
                            label = { Text(stringResource(R.string.format_weapon_chip_label, weapon.name, weapon.type)) },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = str,
                            onValueChange = { if (isValidInput(it)) str = it },
                            label = { Text(stringResource(R.string.label_stat_str)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = strMod,
                            onValueChange = { if (isValidInput(it)) strMod = it },
                            label = { Text(stringResource(R.string.label_stat_mod)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dex,
                            onValueChange = { if (isValidInput(it)) dex = it },
                            label = { Text(stringResource(R.string.label_stat_dex)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = dexMod,
                            onValueChange = { if (isValidInput(it)) dexMod = it },
                            label = { Text(stringResource(R.string.label_stat_mod)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = con,
                            onValueChange = { if (isValidInput(it)) con = it },
                            label = { Text(stringResource(R.string.label_stat_con)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = conMod,
                            onValueChange = { if (isValidInput(it)) conMod = it },
                            label = { Text(stringResource(R.string.label_stat_mod)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = intel,
                            onValueChange = { if (isValidInput(it)) intel = it },
                            label = { Text(stringResource(R.string.label_stat_int)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = intMod,
                            onValueChange = { if (isValidInput(it)) intMod = it },
                            label = { Text(stringResource(R.string.label_stat_mod)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = wis,
                            onValueChange = { if (isValidInput(it)) wis = it },
                            label = { Text(stringResource(R.string.label_stat_wis)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = wisMod,
                            onValueChange = { if (isValidInput(it)) wisMod = it },
                            label = { Text(stringResource(R.string.label_stat_mod)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cha,
                            onValueChange = { if (isValidInput(it)) cha = it },
                            label = { Text(stringResource(R.string.label_stat_cha)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = chaMod,
                            onValueChange = { if (isValidInput(it)) chaMod = it },
                            label = { Text(stringResource(R.string.label_stat_mod)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val stats = Stats(
                    strength = str.toIntOrNull() ?: 10,
                    strengthModifier = strMod.toIntOrNull() ?: 0,
                    dexterity = dex.toIntOrNull() ?: 10,
                    dexterityModifier = dexMod.toIntOrNull() ?: 0,
                    constitution = con.toIntOrNull() ?: 10,
                    constitutionModifier = conMod.toIntOrNull() ?: 0,
                    intelligence = intel.toIntOrNull() ?: 10,
                    intelligenceModifier = intMod.toIntOrNull() ?: 0,
                    wisdom = wis.toIntOrNull() ?: 10,
                    wisdomModifier = wisMod.toIntOrNull() ?: 0,
                    charisma = cha.toIntOrNull() ?: 10,
                    charismaModifier = chaMod.toIntOrNull() ?: 0
                )
                onConfirm(Character(id = character?.id ?: 0, name = name, race = race, stats = stats))
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
    assignedItemIds: Set<Long>,
    onDismiss: () -> Unit,
    onAssign: (ConsumableItem) -> Unit
) {
    val unassignedItems = availableItems.filterNot { it.id in assignedItemIds }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_assign_item, characterName)) },
        text = {
            if (unassignedItems.isEmpty()) {
                Text(stringResource(R.string.label_no_items_to_assign))
            } else {
                LazyColumn {
                    items(unassignedItems) { item ->
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
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}


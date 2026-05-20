package com.hdarby.dicemaster.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdarby.dicemaster.R
import com.hdarby.dicemaster.ui.components.shapes.KiteShape
import com.hdarby.dicemaster.ui.components.shapes.PentagonShape
import com.hdarby.dicemaster.ui.components.shapes.TriangleShape
import com.hdarby.dicemaster.ui.theme.DiceMasterTheme
import com.hdarby.dicemaster.viewmodel.DiceUiState
import com.hdarby.dicemaster.viewmodel.DiceViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DiceRollerScreen(
    viewModel: DiceViewModel = koinViewModel(),
    onNavigateToDebug: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    DiceMasterScreen(
        uiState = uiState,
        onUpdateFaces = viewModel::updateFaces,
        onUpdateQuantity = viewModel::updateQuantity,
        onUpdateModifier = viewModel::updateModifier,
        onRollDice = viewModel::rollDice,
        onDismissResults = viewModel::dismissResults,
        onNavigateToDebug = onNavigateToDebug
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceMasterScreen(
    uiState: DiceUiState,
    onUpdateFaces: (Int) -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onUpdateModifier: (Int) -> Unit,
    onRollDice: () -> Unit,
    onDismissResults: () -> Unit,
    onNavigateToDebug: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_dice_roller)) },
                actions = {
                    IconButton(onClick = onNavigateToDebug) {
                        Icon(Icons.AutoMirrored.Filled.Help, contentDescription = stringResource(R.string.content_desc_rng_debug))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.heading_dice_master),
                    modifier = Modifier.testTag("screen_title_roller"),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(modifier = Modifier.height(48.dp))

                DiceConfigurationSection(
                    faces = uiState.faces,
                    quantity = uiState.quantity,
                    modifierValue = uiState.modifier,
                    onUpdateFaces = onUpdateFaces,
                    onUpdateQuantity = onUpdateQuantity,
                    onUpdateModifier = onUpdateModifier
                )

                Spacer(modifier = Modifier.height(64.dp))

                Button(
                    onClick = onRollDice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.button_roll_dice),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }

            if (uiState.showResults && uiState.rollResult != null) {
                ModalBottomSheet(
                    onDismissRequest = onDismissResults,
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    ResultsContent(
                        results = uiState.rollResult.rolls,
                        total = uiState.rollResult.total,
                        faces = uiState.faces,
                        modifier = uiState.rollResult.modifier
                    )
                }
            }
        }
    }
}

@Composable
fun DiceConfigurationSection(
    faces: Int,
    quantity: Int,
    modifierValue: Int,
    onUpdateFaces: (Int) -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onUpdateModifier: (Int) -> Unit
) {
    val faceOptions = listOf(3, 4, 6, 8, 10, 12, 20, 100)
    val quantityOptions = (1..10).toList()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DropdownSelector(
            label = stringResource(R.string.label_dice_faces),
            currentValue = faces,
            options = faceOptions,
            onValueChange = onUpdateFaces,
            prefix = "D"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                DropdownSelector(
                    label = stringResource(R.string.label_quantity),
                    currentValue = quantity,
                    options = quantityOptions,
                    onValueChange = onUpdateQuantity
                )
            }
            OutlinedTextField(
                value = if (modifierValue == 0) "" else modifierValue.toString(),
                onValueChange = { onUpdateModifier(it.toIntOrNull() ?: 0) },
                label = { Text(stringResource(R.string.label_modifier)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    currentValue: Int,
    options: List<Int>,
    onValueChange: (Int) -> Unit,
    prefix: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = "$prefix$currentValue",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text("$prefix$option") },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ResultsContent(results: List<Int>, total: Int, faces: Int, modifier: Int = 0) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .height(320.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.title_results),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.tertiary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        val modifierText = if (modifier != 0) {
            val sign = if (modifier > 0) "+" else ""
            " ($sign$modifier)"
        } else ""

        Text(
            text = stringResource(R.string.label_total, total, modifierText),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results) { result ->
                ResultItem(value = result, faces = faces)
            }
        }
    }
}

@Composable
fun ResultItem(value: Int, faces: Int) {
    val shape = when (faces) {
        3 -> RectangleShape
        4 -> TriangleShape()
        6 -> RoundedCornerShape(4.dp)
        8 -> TriangleShape()
        10 -> KiteShape()
        12 -> PentagonShape()
        20 -> TriangleShape()
        100 -> CircleShape
        else -> RoundedCornerShape(12.dp)
    }

    val sizeModifier = if (faces == 3) {
        Modifier.size(width = 96.dp, height = 48.dp)
    } else {
        Modifier.size(64.dp)
    }

    val textTopPadding = if (shape is TriangleShape) 18.dp else 0.dp

    Card(
        modifier = sizeModifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = shape,
        border = BorderStroke(1.dp, Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val isCriticalHit = faces == 20 && value == 20
            val isCriticalMiss = faces == 20 && value == 1

            if (isCriticalHit || isCriticalMiss) {
                val textColor = if (isCriticalHit) Color.Green else Color.Red
                val outlineColor = if (isCriticalHit) Color.White else Color.Black

                // Outline effect using shadows
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = textColor,
                            shadow = Shadow(
                                color = outlineColor,
                                offset = Offset(0f, 0f),
                                blurRadius = 4f
                            )
                        ),
                        modifier = Modifier.padding(top = textTopPadding)
                    )
                    // Secondary shadow for sharper outline
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = textColor,
                            shadow = Shadow(
                                color = outlineColor,
                                offset = Offset(0f, 0f),
                                blurRadius = 1f
                            )
                        ),
                        modifier = Modifier.padding(top = textTopPadding)
                    )
                }
            } else {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.padding(top = textTopPadding)
                )
            }
        }
    }
}


@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DiceMasterPreview() {
    DiceMasterTheme {
        DiceMasterScreen(
            uiState = DiceUiState(faces = 20, quantity = 3, modifier = 5, showResults = false),
            onUpdateFaces = {},
            onUpdateQuantity = {},
            onUpdateModifier = {},
            onRollDice = {},
            onDismissResults = {},
            onNavigateToDebug = {}
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun ResultsPreview() {
    DiceMasterTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
            ResultsContent(results = listOf(20, 18, 15, 12, 10, 8, 6, 4, 2, 1), total = 106, faces = 20)
        }
    }
}

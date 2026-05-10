package com.hdarby.dicemaster.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.RectangleShape
import com.hdarby.dicemaster.ui.components.shapes.KiteShape
import com.hdarby.dicemaster.ui.components.shapes.PentagonShape
import com.hdarby.dicemaster.ui.components.shapes.TriangleShape
import androidx.compose.ui.platform.testTag
import com.hdarby.dicemaster.ui.theme.DiceMasterTheme
import com.hdarby.dicemaster.viewmodel.DiceUiState
import com.hdarby.dicemaster.viewmodel.DiceViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DiceRollerScreen(viewModel: DiceViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    DiceMasterScreen(
        uiState = uiState,
        onUpdateFaces = viewModel::updateFaces,
        onUpdateQuantity = viewModel::updateQuantity,
        onRollDice = viewModel::rollDice,
        onDismissResults = viewModel::dismissResults
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceMasterScreen(
    uiState: DiceUiState,
    onUpdateFaces: (Int) -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onRollDice: () -> Unit,
    onDismissResults: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    Box(modifier = Modifier.fillMaxSize()) {
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
                text = "Dice Master",
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
                onUpdateFaces = onUpdateFaces,
                onUpdateQuantity = onUpdateQuantity
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
                    text = "ROLL DICE",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                )
            }
        }

        if (uiState.showResults) {
            ModalBottomSheet(
                onDismissRequest = onDismissResults,
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                ResultsContent(
                    results = uiState.rollResults,
                    total = uiState.rollResults.sum(),
                    faces = uiState.faces
                )
            }
        }
    }
}

@Composable
fun DiceConfigurationSection(
    faces: Int,
    quantity: Int,
    onUpdateFaces: (Int) -> Unit,
    onUpdateQuantity: (Int) -> Unit
) {
    val faceOptions = listOf(3, 4, 6, 8, 10, 12, 20, 100)
    val quantityOptions = (1..10).toList()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        DropdownSelector(
            label = "Dice Faces",
            currentValue = faces,
            options = faceOptions,
            onValueChange = onUpdateFaces,
            prefix = "D"
        )

        DropdownSelector(
            label = "Number of Dice",
            currentValue = quantity,
            options = quantityOptions,
            onValueChange = onUpdateQuantity
        )
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
fun ResultsContent(results: List<Int>, total: Int, faces: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .height(320.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Results",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.tertiary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Total: $total",
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

    Card(
        modifier = sizeModifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = shape
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.padding(top = if (shape is TriangleShape) 8.dp else 0.dp)
            )
        }
    }
}


@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DiceMasterPreview() {
    DiceMasterTheme {
        DiceMasterScreen(
            uiState = DiceUiState(faces = 20, quantity = 3, rollResults = listOf(18, 12, 5), showResults = false),
            onUpdateFaces = {},
            onUpdateQuantity = {},
            onRollDice = {},
            onDismissResults = {}
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

package com.hdarby.dicemaster.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdarby.dicemaster.R
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.Stats
import com.hdarby.dicemaster.ui.theme.DiceMasterTheme
import com.hdarby.dicemaster.viewmodel.SessionViewModel
import com.hdarby.dicemaster.viewmodel.state.SessionSetupStep
import com.hdarby.dicemaster.viewmodel.state.SessionUiState
import org.koin.androidx.compose.koinViewModel

@Composable
fun SessionSetupScreen(
    viewModel: SessionViewModel = koinViewModel(),
    onContinueSession: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    SessionSetupContent(
        uiState = uiState,
        onCreateSessionClicked = viewModel::onCreateSessionClicked,
        onSessionCodeConfirmed = viewModel::onSessionCodeConfirmed,
        onJoinSessionClicked = viewModel::onJoinSessionClicked,
        onJoinCodeChanged = viewModel::onJoinCodeChanged,
        onJoinCodeSubmitted = viewModel::onJoinCodeSubmitted,
        onCharacterSelected = viewModel::onCharacterSelected,
        onErrorDismissed = viewModel::onErrorDismissed,
        onContinueSessionClicked = onContinueSession
    )
}

@Composable
fun SessionSetupContent(
    uiState: SessionUiState,
    onCreateSessionClicked: () -> Unit,
    onSessionCodeConfirmed: () -> Unit,
    onJoinSessionClicked: () -> Unit,
    onJoinCodeChanged: (String) -> Unit,
    onJoinCodeSubmitted: () -> Unit,
    onCharacterSelected: (Character) -> Unit,
    onErrorDismissed: () -> Unit,
    onContinueSessionClicked: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState.setupStep) {
            SessionSetupStep.Landing -> LandingStep(
                isLoading = uiState.isLoading || uiState.isCheckingSession,
                hasActiveSession = uiState.currentSession != null,
                onCreateSessionClicked = onCreateSessionClicked,
                onJoinSessionClicked = onJoinSessionClicked,
                onContinueSessionClicked = onContinueSessionClicked
            )
            SessionSetupStep.ShowingSessionCode -> ShowingSessionCodeStep(
                code = uiState.generatedCode.orEmpty(),
                isLoading = uiState.isLoading,
                onConfirmed = onSessionCodeConfirmed
            )
            SessionSetupStep.EnteringJoinCode -> EnteringJoinCodeStep(
                code = uiState.joinCodeInput,
                isLoading = uiState.isLoading,
                onCodeChanged = onJoinCodeChanged,
                onSubmit = onJoinCodeSubmitted
            )
            SessionSetupStep.SelectingCharacter -> SelectingCharacterStep(
                characters = uiState.availableCharacters,
                onCharacterSelected = onCharacterSelected
            )
        }

        if (uiState.error != null) {
            AlertDialog(
                onDismissRequest = onErrorDismissed,
                title = { Text(stringResource(R.string.session_error_title)) },
                text = { Text(uiState.error) },
                confirmButton = {
                    TextButton(onClick = onErrorDismissed) {
                        Text(stringResource(R.string.button_confirm))
                    }
                }
            )
        }
    }
}

@Composable
private fun LandingStep(
    isLoading: Boolean,
    hasActiveSession: Boolean,
    onCreateSessionClicked: () -> Unit,
    onJoinSessionClicked: () -> Unit,
    onContinueSessionClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Casino,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.heading_dice_master),
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.session_setup_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onCreateSessionClicked,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Default.Casino, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.session_button_create))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onJoinSessionClicked,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.GroupAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.session_button_join))
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onContinueSessionClicked,
            enabled = hasActiveSession && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.session_button_continue))
        }
    }
}

@Composable
private fun ShowingSessionCodeStep(
    code: String,
    isLoading: Boolean,
    onConfirmed: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.session_code_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.session_code_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = { clipboardManager.setText(AnnotatedString(code)) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.session_code_copy),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onConfirmed,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.session_button_start))
        }
    }
}

@Composable
private fun EnteringJoinCodeStep(
    code: String,
    isLoading: Boolean,
    onCodeChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.GroupAdd,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.session_join_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.session_join_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = code,
            onValueChange = onCodeChanged,
            label = { Text(stringResource(R.string.session_label_code)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSubmit,
            enabled = !isLoading && code.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.session_button_join_confirm))
            }
        }
    }
}

@Composable
private fun SelectingCharacterStep(
    characters: List<Character>,
    onCharacterSelected: (Character) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.session_select_character_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.session_select_character_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (characters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.session_no_characters),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(characters) { character ->
                    Card(
                        onClick = { onCharacterSelected(character) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = character.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = character.race,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
private fun SessionSetupLandingPreview() {
    DiceMasterTheme {
        SessionSetupContent(
            uiState = SessionUiState(isCheckingSession = false, setupStep = SessionSetupStep.Landing),
            onCreateSessionClicked = {},
            onSessionCodeConfirmed = {},
            onJoinSessionClicked = {},
            onJoinCodeChanged = {},
            onJoinCodeSubmitted = {},
            onCharacterSelected = {},
            onErrorDismissed = {}
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
private fun SessionSetupCodePreview() {
    DiceMasterTheme {
        SessionSetupContent(
            uiState = SessionUiState(
                isCheckingSession = false,
                setupStep = SessionSetupStep.ShowingSessionCode,
                generatedCode = "AB12CD34"
            ),
            onCreateSessionClicked = {},
            onSessionCodeConfirmed = {},
            onJoinSessionClicked = {},
            onJoinCodeChanged = {},
            onJoinCodeSubmitted = {},
            onCharacterSelected = {},
            onErrorDismissed = {}
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
private fun SessionSetupJoinPreview() {
    DiceMasterTheme {
        SessionSetupContent(
            uiState = SessionUiState(
                isCheckingSession = false,
                setupStep = SessionSetupStep.EnteringJoinCode,
                joinCodeInput = "AB12CD34"
            ),
            onCreateSessionClicked = {},
            onSessionCodeConfirmed = {},
            onJoinSessionClicked = {},
            onJoinCodeChanged = {},
            onJoinCodeSubmitted = {},
            onCharacterSelected = {},
            onErrorDismissed = {}
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
private fun SessionSetupSelectCharacterPreview() {
    val characters = listOf(
        Character(id = 1, name = "Grog", race = "Goliath", stats = Stats(20, 5, 12, 1, 18, 4, 6, -2, 10, 0, 8, -1)),
        Character(id = 2, name = "Vex", race = "Half-Elf", stats = Stats(12, 1, 18, 4, 14, 2, 14, 2, 14, 2, 16, 3))
    )
    DiceMasterTheme {
        SessionSetupContent(
            uiState = SessionUiState(
                isCheckingSession = false,
                setupStep = SessionSetupStep.SelectingCharacter,
                availableCharacters = characters
            ),
            onCreateSessionClicked = {},
            onSessionCodeConfirmed = {},
            onJoinSessionClicked = {},
            onJoinCodeChanged = {},
            onJoinCodeSubmitted = {},
            onCharacterSelected = {},
            onErrorDismissed = {}
        )
    }
}









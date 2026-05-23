package com.hdarby.dicemaster.viewmodel.state

import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.Session

enum class SessionSetupStep {
    Landing,
    ShowingSessionCode,
    EnteringJoinCode,
    SelectingCharacter
}

data class SessionUiState(
    val isCheckingSession: Boolean = true,
    val currentSession: Session? = null,
    val setupStep: SessionSetupStep = SessionSetupStep.Landing,
    val isLoading: Boolean = false,
    val generatedCode: String? = null,
    val joinCodeInput: String = "",
    val availableCharacters: List<Character> = emptyList(),
    val error: String? = null
)


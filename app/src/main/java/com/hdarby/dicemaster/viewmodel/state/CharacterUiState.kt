package com.hdarby.dicemaster.viewmodel.state

import com.hdarby.dicemaster.domain.model.CharacterWithWeapons
import com.hdarby.dicemaster.domain.model.UserRole

data class CharacterUiState(
    val characters: List<CharacterWithWeapons> = emptyList(),
    val userRole: UserRole? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

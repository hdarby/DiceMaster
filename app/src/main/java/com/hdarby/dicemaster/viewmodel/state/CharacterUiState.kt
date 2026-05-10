package com.hdarby.dicemaster.viewmodel.state

import com.hdarby.dicemaster.domain.model.CharacterWithWeapons

data class CharacterUiState(
    val characters: List<CharacterWithWeapons> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

package com.hdarby.dicemaster.viewmodel.state

import com.hdarby.dicemaster.domain.model.CharacterItemEntry
import com.hdarby.dicemaster.domain.model.ConsumableItem

data class ItemUiState(
    val items: List<ConsumableItem> = emptyList(),
    val itemsByCharacterId: Map<Long, List<CharacterItemEntry>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)


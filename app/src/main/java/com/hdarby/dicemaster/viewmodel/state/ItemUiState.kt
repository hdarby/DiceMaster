package com.hdarby.dicemaster.viewmodel.state

import com.hdarby.dicemaster.domain.model.CharacterItemEntry
import com.hdarby.dicemaster.domain.model.ConsumableItem
import com.hdarby.dicemaster.domain.model.UserRole

data class ItemUiState(
    val items: List<ConsumableItem> = emptyList(),
    val itemsByCharacterId: Map<Long, List<CharacterItemEntry>> = emptyMap(),
    val userRole: UserRole? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

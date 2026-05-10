package com.hdarby.dicemaster.viewmodel.state

import com.hdarby.dicemaster.domain.model.Weapon

data class WeaponUiState(
    val weapons: List<Weapon> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

package com.hdarby.dicemaster.viewmodel.state

import com.hdarby.dicemaster.domain.model.UserRole
import com.hdarby.dicemaster.domain.model.Weapon

data class WeaponUiState(
    val weapons: List<Weapon> = emptyList(),
    val userRole: UserRole? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

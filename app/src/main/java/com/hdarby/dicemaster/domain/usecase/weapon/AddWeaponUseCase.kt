package com.hdarby.dicemaster.domain.usecase.weapon

import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.domain.repository.WeaponRepository

class AddWeaponUseCase(private val repository: WeaponRepository) {
    suspend operator fun invoke(weapon: Weapon): Long = repository.addWeapon(weapon)
}

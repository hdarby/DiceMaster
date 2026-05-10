package com.hdarby.dicemaster.domain.usecase.weapon

import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.domain.repository.WeaponRepository

class DeleteWeaponUseCase(private val repository: WeaponRepository) {
    suspend operator fun invoke(weapon: Weapon) = repository.deleteWeapon(weapon)
}

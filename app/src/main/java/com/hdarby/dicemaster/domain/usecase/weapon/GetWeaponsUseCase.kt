package com.hdarby.dicemaster.domain.usecase.weapon

import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.domain.repository.WeaponRepository
import kotlinx.coroutines.flow.Flow

class GetWeaponsUseCase(private val repository: WeaponRepository) {
    operator fun invoke(): Flow<List<Weapon>> = repository.getAllWeapons()
}

package com.hdarby.dicemaster.domain.usecase.character

import com.hdarby.dicemaster.domain.repository.CharacterRepository

class UnassignWeaponFromCharacterUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(characterId: Long, weaponId: Long) =
        repository.unassignWeaponFromCharacter(characterId, weaponId)
}

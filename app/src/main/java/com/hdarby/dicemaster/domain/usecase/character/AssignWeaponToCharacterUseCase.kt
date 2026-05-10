package com.hdarby.dicemaster.domain.usecase.character

import com.hdarby.dicemaster.domain.repository.CharacterRepository

class AssignWeaponToCharacterUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(characterId: Long, weaponId: Long) =
        repository.assignWeaponToCharacter(characterId, weaponId)
}

package com.hdarby.dicemaster.domain.usecase.character

import com.hdarby.dicemaster.domain.repository.CharacterRepository

class UnassignWeaponFromCharacterUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(assignmentId: Long) =
        repository.unassignWeaponFromCharacter(assignmentId)
}

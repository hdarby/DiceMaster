package com.hdarby.dicemaster.domain.usecase.character

import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.repository.CharacterRepository

class DamageCharacterUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(character: Character) {
        val damaged = character.copy(
            currentHitPoints = (character.currentHitPoints - 1).coerceAtLeast(0)
        )
        repository.updateCharacter(damaged)
    }
}


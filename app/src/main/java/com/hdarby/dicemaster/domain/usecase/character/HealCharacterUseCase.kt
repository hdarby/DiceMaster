package com.hdarby.dicemaster.domain.usecase.character

import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.repository.CharacterRepository

class HealCharacterUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(character: Character) {
        val wasDown = character.currentHitPoints == 0
        val healed = character.copy(
            currentHitPoints = (character.currentHitPoints + 1).coerceAtMost(character.maxHitPoints),
            deathSaveFailures = if (wasDown) 0 else character.deathSaveFailures
        )
        repository.updateCharacter(healed)
    }
}



package com.hdarby.dicemaster.domain.usecase.character

import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.repository.CharacterRepository

class MarkCharacterDeadUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(character: Character) {
        repository.updateCharacter(
            character.copy(isDead = true, deathSaveFailures = 3, currentHitPoints = 0)
        )
    }
}


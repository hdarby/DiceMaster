package com.hdarby.dicemaster.domain.usecase.character

import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.repository.CharacterRepository

class SetDeathSaveFailuresUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(character: Character, failures: Int) {
        repository.updateCharacter(character.copy(deathSaveFailures = failures.coerceIn(0, 3)))
    }
}


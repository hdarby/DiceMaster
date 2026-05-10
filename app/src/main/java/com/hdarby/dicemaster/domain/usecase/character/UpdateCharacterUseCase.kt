package com.hdarby.dicemaster.domain.usecase.character

import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.repository.CharacterRepository

class UpdateCharacterUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(character: Character) {
        repository.updateCharacter(character)
    }
}

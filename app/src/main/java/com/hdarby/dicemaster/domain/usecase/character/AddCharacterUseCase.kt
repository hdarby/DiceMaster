package com.hdarby.dicemaster.domain.usecase.character

import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.repository.CharacterRepository

class AddCharacterUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(character: Character): Long = repository.addCharacter(character)
}

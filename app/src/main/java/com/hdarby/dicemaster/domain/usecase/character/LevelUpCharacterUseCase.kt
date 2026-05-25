package com.hdarby.dicemaster.domain.usecase.character

import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.repository.CharacterRepository

class LevelUpCharacterUseCase(private val repository: CharacterRepository) {
    suspend operator fun invoke(character: Character) {
        val hitDieGain = character.characterClass?.hitDie ?: 0
        val newMaxHp = character.maxHitPoints + hitDieGain
        val newCurrentHp = (character.currentHitPoints + hitDieGain).coerceAtMost(newMaxHp)
        repository.updateCharacter(
            character.copy(
                level = character.level + 1,
                maxHitPoints = newMaxHp,
                currentHitPoints = newCurrentHp
            )
        )
    }
}


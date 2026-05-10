package com.hdarby.dicemaster.domain.usecase.character

import com.hdarby.dicemaster.domain.model.CharacterWithWeapons
import com.hdarby.dicemaster.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow

class GetCharactersWithWeaponsUseCase(private val repository: CharacterRepository) {
    operator fun invoke(): Flow<List<CharacterWithWeapons>> = repository.getCharactersWithWeapons()
}

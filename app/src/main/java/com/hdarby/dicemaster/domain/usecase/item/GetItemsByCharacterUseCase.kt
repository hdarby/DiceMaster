package com.hdarby.dicemaster.domain.usecase.item

import com.hdarby.dicemaster.domain.model.CharacterItemEntry
import com.hdarby.dicemaster.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow

class GetItemsByCharacterUseCase(private val repository: ItemRepository) {
    operator fun invoke(): Flow<Map<Long, List<CharacterItemEntry>>> =
        repository.getItemsByCharacter()
}


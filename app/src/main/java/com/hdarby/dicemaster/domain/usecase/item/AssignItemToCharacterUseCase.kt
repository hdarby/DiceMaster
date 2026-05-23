package com.hdarby.dicemaster.domain.usecase.item

import com.hdarby.dicemaster.domain.repository.ItemRepository

class AssignItemToCharacterUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(characterId: Long, itemId: Long, quantity: Int) =
        repository.assignItemToCharacter(characterId, itemId, quantity)
}


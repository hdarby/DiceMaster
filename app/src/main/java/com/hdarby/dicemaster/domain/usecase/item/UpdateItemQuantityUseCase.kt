package com.hdarby.dicemaster.domain.usecase.item

import com.hdarby.dicemaster.domain.repository.ItemRepository

class UpdateItemQuantityUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(characterId: Long, itemId: Long, quantity: Int) =
        repository.updateItemQuantity(characterId, itemId, quantity)
}


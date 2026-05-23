package com.hdarby.dicemaster.domain.usecase.item

import com.hdarby.dicemaster.domain.model.ConsumableItem
import com.hdarby.dicemaster.domain.repository.ItemRepository

class DeleteItemUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(item: ConsumableItem) = repository.deleteItem(item)
}


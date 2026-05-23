package com.hdarby.dicemaster.domain.usecase.item

import com.hdarby.dicemaster.domain.model.ConsumableItem
import com.hdarby.dicemaster.domain.repository.ItemRepository

class AddItemUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(item: ConsumableItem): Long = repository.addItem(item)
}


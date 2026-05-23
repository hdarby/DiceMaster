package com.hdarby.dicemaster.domain.usecase.item

import com.hdarby.dicemaster.domain.model.ConsumableItem
import com.hdarby.dicemaster.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow

class GetItemsUseCase(private val repository: ItemRepository) {
    operator fun invoke(): Flow<List<ConsumableItem>> = repository.getAllItems()
}


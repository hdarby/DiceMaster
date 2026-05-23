package com.hdarby.dicemaster.domain.usecase.item

import com.hdarby.dicemaster.domain.repository.ItemRepository

class AdjustItemStockUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(itemId: Long, delta: Int) = repository.adjustItemStock(itemId, delta)
}


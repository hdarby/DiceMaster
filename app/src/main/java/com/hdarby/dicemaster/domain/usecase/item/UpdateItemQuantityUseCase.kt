package com.hdarby.dicemaster.domain.usecase.item

import com.hdarby.dicemaster.domain.repository.ItemRepository

class UpdateItemQuantityUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(assignmentId: Long, quantity: Int) =
        repository.updateItemQuantity(assignmentId, quantity)
}



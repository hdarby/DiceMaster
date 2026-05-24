package com.hdarby.dicemaster.domain.usecase.item

import com.hdarby.dicemaster.domain.repository.ItemRepository

class UnassignItemFromCharacterUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(assignmentId: Long) =
        repository.unassignItemFromCharacter(assignmentId)
}



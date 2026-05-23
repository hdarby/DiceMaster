package com.hdarby.dicemaster.domain.repository

import com.hdarby.dicemaster.domain.model.CharacterItemEntry
import com.hdarby.dicemaster.domain.model.ConsumableItem
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getAllItems(): Flow<List<ConsumableItem>>
    fun getItemsByCharacter(): Flow<Map<Long, List<CharacterItemEntry>>>
    suspend fun addItem(item: ConsumableItem): Long
    suspend fun updateItem(item: ConsumableItem)
    suspend fun deleteItem(item: ConsumableItem)
    suspend fun assignItemToCharacter(characterId: Long, itemId: Long, quantity: Int)
    suspend fun unassignItemFromCharacter(characterId: Long, itemId: Long)
    suspend fun updateItemQuantity(characterId: Long, itemId: Long, quantity: Int)
}


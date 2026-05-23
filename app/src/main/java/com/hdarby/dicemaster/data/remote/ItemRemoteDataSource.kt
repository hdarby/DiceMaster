package com.hdarby.dicemaster.data.remote

import com.hdarby.dicemaster.domain.model.ConsumableItem
import kotlinx.coroutines.flow.Flow

interface ItemRemoteDataSource {
    suspend fun upsertItem(sessionId: String, item: ConsumableItem)
    suspend fun deleteItem(sessionId: String, itemId: Long)
    suspend fun upsertCharacterItem(sessionId: String, characterId: Long, itemId: Long, quantity: Int)
    suspend fun deleteCharacterItem(sessionId: String, characterId: Long, itemId: Long)
    suspend fun updateCharacterItemQuantity(sessionId: String, characterId: Long, itemId: Long, quantity: Int)
    fun observeItems(sessionId: String): Flow<List<ConsumableItem>>
    fun observeCharacterItems(sessionId: String): Flow<List<RemoteCharacterItem>>
}


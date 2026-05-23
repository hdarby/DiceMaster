package com.hdarby.dicemaster.data.remote

import com.hdarby.dicemaster.domain.model.ConsumableItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeItemRemoteDataSource : ItemRemoteDataSource {

    private val _items = MutableStateFlow<List<ConsumableItem>>(emptyList())
    private val _characterItems = MutableStateFlow<List<RemoteCharacterItem>>(emptyList())

    val items: List<ConsumableItem> get() = _items.value
    val characterItems: List<RemoteCharacterItem> get() = _characterItems.value

    var upsertItemCallCount = 0
    var deleteItemCallCount = 0
    var upsertCharacterItemCallCount = 0
    var deleteCharacterItemCallCount = 0
    var updateQuantityCallCount = 0
    var lastQuantityUpdate: Triple<Long, Long, Int>? = null  // characterId, itemId, quantity

    override suspend fun upsertItem(sessionId: String, item: ConsumableItem) {
        upsertItemCallCount++
        val current = _items.value.toMutableList()
        current.removeAll { it.id == item.id }
        current.add(item)
        _items.value = current
    }

    override suspend fun deleteItem(sessionId: String, itemId: Long) {
        deleteItemCallCount++
        _items.value = _items.value.filter { it.id != itemId }
    }

    override suspend fun upsertCharacterItem(sessionId: String, characterId: Long, itemId: Long, quantity: Int) {
        upsertCharacterItemCallCount++
        val current = _characterItems.value.toMutableList()
        current.removeAll { it.characterId == characterId && it.itemId == itemId }
        current.add(RemoteCharacterItem(characterId, itemId, quantity))
        _characterItems.value = current
    }

    override suspend fun deleteCharacterItem(sessionId: String, characterId: Long, itemId: Long) {
        deleteCharacterItemCallCount++
        _characterItems.value = _characterItems.value.filter {
            !(it.characterId == characterId && it.itemId == itemId)
        }
    }

    override suspend fun updateCharacterItemQuantity(sessionId: String, characterId: Long, itemId: Long, quantity: Int) {
        updateQuantityCallCount++
        lastQuantityUpdate = Triple(characterId, itemId, quantity)
        val current = _characterItems.value.toMutableList()
        val idx = current.indexOfFirst { it.characterId == characterId && it.itemId == itemId }
        if (idx >= 0) current[idx] = current[idx].copy(quantity = quantity)
        _characterItems.value = current
    }

    override fun observeItems(sessionId: String): Flow<List<ConsumableItem>> = _items.asStateFlow()

    override fun observeCharacterItems(sessionId: String): Flow<List<RemoteCharacterItem>> =
        _characterItems.asStateFlow()

    fun simulateRemoteItemUpdate(vararg remoteItems: ConsumableItem) {
        _items.value = remoteItems.toList()
    }

    fun simulateRemoteCharacterItemUpdate(vararg refs: RemoteCharacterItem) {
        _characterItems.value = refs.toList()
    }
}


package com.hdarby.dicemaster.data.repository

import com.hdarby.dicemaster.data.local.dao.ItemDao
import com.hdarby.dicemaster.data.local.entity.CharacterItemCrossRef
import com.hdarby.dicemaster.data.local.entity.ItemEntity
import com.hdarby.dicemaster.data.remote.ItemRemoteDataSource
import com.hdarby.dicemaster.domain.model.CharacterItemEntry
import com.hdarby.dicemaster.domain.model.ConsumableItem
import com.hdarby.dicemaster.domain.repository.ItemRepository
import com.hdarby.dicemaster.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ItemRepositoryImpl(
    private val itemDao: ItemDao,
    private val sessionRepository: SessionRepository? = null,
    private val itemRemoteDataSource: ItemRemoteDataSource? = null,
    private val externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : ItemRepository {

    init {
        if (sessionRepository != null && itemRemoteDataSource != null) {
            startRemoteSync()
        }
    }

    private fun startRemoteSync() {
        externalScope.launch {
            sessionRepository!!.observeSession().collectLatest { session ->
                if (session == null) return@collectLatest
                itemRemoteDataSource!!.observeItems(session.sessionId).collect { remoteItems ->
                    remoteItems.forEach { item -> itemDao.insertItem(item.toEntity()) }
                }
            }
        }
        externalScope.launch {
            sessionRepository!!.observeSession().collectLatest { session ->
                if (session == null) return@collectLatest
                itemRemoteDataSource!!.observeCharacterItems(session.sessionId).collect { refs ->
                    refs.forEach { ref ->
                        itemDao.upsertCharacterItemCrossRef(
                            CharacterItemCrossRef(ref.characterId, ref.itemId, ref.quantity)
                        )
                    }
                }
            }
        }
    }

    override fun getAllItems(): Flow<List<ConsumableItem>> =
        itemDao.getAllItems().map { entities -> entities.map { it.toDomain() } }

    override fun getItemsByCharacter(): Flow<Map<Long, List<CharacterItemEntry>>> =
        itemDao.getAllCharacterItems().map { assignments ->
            assignments
                .groupBy { it.characterId }
                .mapValues { (_, entries) ->
                    entries.map { CharacterItemEntry(item = it.item.toDomain(), quantity = it.quantity) }
                }
        }

    override suspend fun addItem(item: ConsumableItem): Long {
        val localId = itemDao.insertItem(item.toEntity())
        sessionRepository?.getActiveSession()?.let { session ->
            itemRemoteDataSource?.upsertItem(session.sessionId, item.copy(id = localId))
        }
        return localId
    }

    override suspend fun updateItem(item: ConsumableItem) {
        itemDao.updateItem(item.toEntity())
        sessionRepository?.getActiveSession()?.let { session ->
            itemRemoteDataSource?.upsertItem(session.sessionId, item)
        }
    }

    override suspend fun deleteItem(item: ConsumableItem) {
        itemDao.deleteItem(item.toEntity())
        sessionRepository?.getActiveSession()?.let { session ->
            itemRemoteDataSource?.deleteItem(session.sessionId, item.id)
        }
    }

    override suspend fun assignItemToCharacter(characterId: Long, itemId: Long, quantity: Int) {
        itemDao.insertCharacterItemCrossRef(CharacterItemCrossRef(characterId, itemId, quantity))
        sessionRepository?.getActiveSession()?.let { session ->
            itemRemoteDataSource?.upsertCharacterItem(session.sessionId, characterId, itemId, quantity)
        }
    }

    override suspend fun unassignItemFromCharacter(characterId: Long, itemId: Long) {
        itemDao.deleteCharacterItemCrossRef(characterId, itemId)
        sessionRepository?.getActiveSession()?.let { session ->
            itemRemoteDataSource?.deleteCharacterItem(session.sessionId, characterId, itemId)
        }
    }

    override suspend fun updateItemQuantity(characterId: Long, itemId: Long, quantity: Int) {
        itemDao.updateQuantity(characterId, itemId, quantity)
        sessionRepository?.getActiveSession()?.let { session ->
            itemRemoteDataSource?.updateCharacterItemQuantity(session.sessionId, characterId, itemId, quantity)
        }
    }

    private fun ItemEntity.toDomain() = ConsumableItem(
        id = id, name = name, description = description, totalQuantity = totalQuantity
    )

    private fun ConsumableItem.toEntity() = ItemEntity(
        id = id, name = name, description = description, totalQuantity = totalQuantity
    )
}


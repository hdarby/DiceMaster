package com.hdarby.dicemaster.data.repository

import com.hdarby.dicemaster.data.local.dao.ItemDao
import com.hdarby.dicemaster.data.local.entity.CharacterItemCrossRef
import com.hdarby.dicemaster.data.local.entity.ItemEntity
import com.hdarby.dicemaster.domain.model.CharacterItemEntry
import com.hdarby.dicemaster.domain.model.ConsumableItem
import com.hdarby.dicemaster.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ItemRepositoryImpl(private val itemDao: ItemDao) : ItemRepository {

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

    override suspend fun addItem(item: ConsumableItem): Long =
        itemDao.insertItem(item.toEntity())

    override suspend fun updateItem(item: ConsumableItem) =
        itemDao.updateItem(item.toEntity())

    override suspend fun deleteItem(item: ConsumableItem) =
        itemDao.deleteItem(item.toEntity())

    override suspend fun assignItemToCharacter(characterId: Long, itemId: Long) =
        itemDao.insertCharacterItemCrossRef(CharacterItemCrossRef(characterId, itemId))

    override suspend fun unassignItemFromCharacter(characterId: Long, itemId: Long) =
        itemDao.deleteCharacterItemCrossRef(characterId, itemId)

    override suspend fun updateItemQuantity(characterId: Long, itemId: Long, quantity: Int) =
        itemDao.updateQuantity(characterId, itemId, quantity)

    private fun ItemEntity.toDomain() = ConsumableItem(id = id, name = name, description = description, totalQuantity = totalQuantity)

    private fun ConsumableItem.toEntity() = ItemEntity(id = id, name = name, description = description, totalQuantity = totalQuantity)
}



package com.hdarby.dicemaster.data.repository

import app.cash.turbine.test
import com.hdarby.dicemaster.data.local.dao.ItemDao
import com.hdarby.dicemaster.data.local.entity.CharacterItemAssignment
import com.hdarby.dicemaster.data.local.entity.CharacterItemCrossRef
import com.hdarby.dicemaster.data.local.entity.ItemEntity
import com.hdarby.dicemaster.domain.model.CharacterItemEntry
import com.hdarby.dicemaster.domain.model.ConsumableItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ItemRepositoryImplTest {

    private val itemDao: ItemDao = mockk()
    private val repository = ItemRepositoryImpl(itemDao)

    private val itemEntity = ItemEntity(id = 1L, name = "Healing Potion", description = "Restores 2d4+2 HP")
    private val item = ConsumableItem(id = 1L, name = "Healing Potion", description = "Restores 2d4+2 HP")

    // --- getAllItems ---

    @Test
    fun `getAllItems maps entities to domain models`() = runTest {
        every { itemDao.getAllItems() } returns flowOf(listOf(itemEntity))

        repository.getAllItems().test {
            assertEquals(listOf(item), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getAllItems returns empty list when no items exist`() = runTest {
        every { itemDao.getAllItems() } returns flowOf(emptyList())

        repository.getAllItems().test {
            assertEquals(emptyList<ConsumableItem>(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getAllItems maps multiple entities`() = runTest {
        val entity2 = ItemEntity(id = 2L, name = "Scroll of Fireball", description = "Casts fireball")
        val item2 = ConsumableItem(id = 2L, name = "Scroll of Fireball", description = "Casts fireball")
        every { itemDao.getAllItems() } returns flowOf(listOf(itemEntity, entity2))

        repository.getAllItems().test {
            assertEquals(listOf(item, item2), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getAllItems maps non-default totalQuantity from entity to domain`() = runTest {
        val entityWithQuantity = ItemEntity(id = 3L, name = "Rope", description = "50 ft rope", totalQuantity = 5)
        every { itemDao.getAllItems() } returns flowOf(listOf(entityWithQuantity))

        repository.getAllItems().test {
            val result = awaitItem()
            assertEquals(5, result.first().totalQuantity)
            awaitComplete()
        }
    }

    // --- getItemsByCharacter ---

    @Test
    fun `getItemsByCharacter groups assignments by characterId`() = runTest {
        val assignment = CharacterItemAssignment(characterId = 10L, item = itemEntity, quantity = 2)
        every { itemDao.getAllCharacterItems() } returns flowOf(listOf(assignment))

        repository.getItemsByCharacter().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(
                listOf(CharacterItemEntry(item = item, quantity = 2)),
                result[10L]
            )
            awaitComplete()
        }
    }

    @Test
    fun `getItemsByCharacter preserves totalQuantity on item`() = runTest {
        val entityWithQuantity = ItemEntity(id = 1L, name = "Healing Potion", description = "Restores 2d4+2 HP", totalQuantity = 10)
        val assignment = CharacterItemAssignment(characterId = 10L, item = entityWithQuantity, quantity = 3)
        every { itemDao.getAllCharacterItems() } returns flowOf(listOf(assignment))

        repository.getItemsByCharacter().test {
            val entry = awaitItem()[10L]?.first()
            assertEquals(10, entry?.item?.totalQuantity)
            awaitComplete()
        }
    }

    @Test
    fun `getItemsByCharacter returns empty map when no assignments exist`() = runTest {
        every { itemDao.getAllCharacterItems() } returns flowOf(emptyList())

        repository.getItemsByCharacter().test {
            assertEquals(emptyMap<Long, List<CharacterItemEntry>>(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getItemsByCharacter groups multiple items under the same character`() = runTest {
        val entity2 = ItemEntity(id = 2L, name = "Torch", description = "Provides light")
        val assignment1 = CharacterItemAssignment(characterId = 5L, item = itemEntity, quantity = 1)
        val assignment2 = CharacterItemAssignment(characterId = 5L, item = entity2, quantity = 3)
        every { itemDao.getAllCharacterItems() } returns flowOf(listOf(assignment1, assignment2))

        repository.getItemsByCharacter().test {
            val result = awaitItem()
            assertEquals(2, result[5L]?.size)
            awaitComplete()
        }
    }

    @Test
    fun `getItemsByCharacter separates assignments for different characters`() = runTest {
        val assignment1 = CharacterItemAssignment(characterId = 1L, item = itemEntity, quantity = 1)
        val assignment2 = CharacterItemAssignment(characterId = 2L, item = itemEntity, quantity = 5)
        every { itemDao.getAllCharacterItems() } returns flowOf(listOf(assignment1, assignment2))

        repository.getItemsByCharacter().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals(1, result[1L]?.first()?.quantity)
            assertEquals(5, result[2L]?.first()?.quantity)
            awaitComplete()
        }
    }

    // --- addItem ---

    @Test
    fun `addItem inserts entity and returns generated id`() = runTest {
        coEvery { itemDao.insertItem(any()) } returns 1L

        val result = repository.addItem(item)

        assertEquals(1L, result)
        coVerify { itemDao.insertItem(match { it.name == "Healing Potion" && it.description == "Restores 2d4+2 HP" }) }
    }

    @Test
    fun `addItem passes totalQuantity to dao`() = runTest {
        val itemWithStock = ConsumableItem(id = 0, name = "Arrow", description = "Standard arrow", totalQuantity = 20)
        coEvery { itemDao.insertItem(any()) } returns 5L

        repository.addItem(itemWithStock)

        coVerify { itemDao.insertItem(match { it.totalQuantity == 20 }) }
    }

    // --- updateItem ---

    @Test
    fun `updateItem updates entity`() = runTest {
        coEvery { itemDao.updateItem(any()) } returns Unit

        repository.updateItem(item)

        coVerify { itemDao.updateItem(match { it.name == "Healing Potion" }) }
    }

    // --- deleteItem ---

    @Test
    fun `deleteItem deletes entity`() = runTest {
        coEvery { itemDao.deleteItem(any()) } returns Unit

        repository.deleteItem(item)

        coVerify { itemDao.deleteItem(match { it.name == "Healing Potion" }) }
    }

    // --- assignItemToCharacter ---

    @Test
    fun `assignItemToCharacter inserts cross-ref with provided quantity`() = runTest {
        coEvery { itemDao.insertCharacterItemCrossRef(any()) } returns Unit

        repository.assignItemToCharacter(characterId = 10L, itemId = 1L, quantity = 20)

        coVerify {
            itemDao.insertCharacterItemCrossRef(
                match { it.characterId == 10L && it.itemId == 1L && it.quantity == 20 }
            )
        }
    }

    @Test
    fun `assignItemToCharacter creates cross-ref with correct ids and quantity`() = runTest {
        coEvery { itemDao.insertCharacterItemCrossRef(any()) } returns Unit

        repository.assignItemToCharacter(characterId = 99L, itemId = 42L, quantity = 7)

        coVerify {
            itemDao.insertCharacterItemCrossRef(CharacterItemCrossRef(characterId = 99L, itemId = 42L, quantity = 7))
        }
    }

    // --- unassignItemFromCharacter ---

    @Test
    fun `unassignItemFromCharacter deletes cross-ref`() = runTest {
        coEvery { itemDao.deleteCharacterItemCrossRef(any(), any()) } returns Unit

        repository.unassignItemFromCharacter(characterId = 10L, itemId = 1L)

        coVerify { itemDao.deleteCharacterItemCrossRef(10L, 1L) }
    }

    // --- updateItemQuantity ---

    @Test
    fun `updateItemQuantity calls dao with correct parameters`() = runTest {
        coEvery { itemDao.updateQuantity(any(), any(), any()) } returns Unit

        repository.updateItemQuantity(characterId = 10L, itemId = 1L, quantity = 5)

        coVerify { itemDao.updateQuantity(10L, 1L, 5) }
    }
}








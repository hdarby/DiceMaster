package com.hdarby.dicemaster.domain.usecase.item

import app.cash.turbine.test
import com.hdarby.dicemaster.domain.model.CharacterItemEntry
import com.hdarby.dicemaster.domain.model.ConsumableItem
import com.hdarby.dicemaster.domain.repository.ItemRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ItemUseCasesTest {

    private val repository: ItemRepository = mockk()

    private val item = ConsumableItem(
        id = 1L,
        name = "Healing Potion",
        description = "Restores 2d4+2 hit points.",
        totalQuantity = 3
    )

    // ── GetItemsUseCase ──────────────────────────────────────────────────────

    @Test
    fun `GetItemsUseCase returns all items from repository`() = runTest {
        val useCase = GetItemsUseCase(repository)
        every { repository.getAllItems() } returns flowOf(listOf(item))

        useCase().test {
            assertEquals(listOf(item), awaitItem())
            awaitComplete()
        }
    }

    // ── GetItemsByCharacterUseCase ───────────────────────────────────────────

    @Test
    fun `GetItemsByCharacterUseCase returns character item map from repository`() = runTest {
        val useCase = GetItemsByCharacterUseCase(repository)
        val entry = CharacterItemEntry(assignmentId = 10L, item = item, quantity = 2)
        val expected = mapOf(1L to listOf(entry))
        every { repository.getItemsByCharacter() } returns flowOf(expected)

        useCase().test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    // ── AddItemUseCase ───────────────────────────────────────────────────────

    @Test
    fun `AddItemUseCase returns generated id from repository`() = runTest {
        val useCase = AddItemUseCase(repository)
        coEvery { repository.addItem(item) } returns 42L

        val result = useCase(item)

        assertEquals(42L, result)
        coVerify { repository.addItem(item) }
    }

    // ── UpdateItemUseCase ────────────────────────────────────────────────────

    @Test
    fun `UpdateItemUseCase delegates update to repository`() = runTest {
        val useCase = UpdateItemUseCase(repository)
        coEvery { repository.updateItem(item) } returns Unit

        useCase(item)

        coVerify { repository.updateItem(item) }
    }

    // ── DeleteItemUseCase ────────────────────────────────────────────────────

    @Test
    fun `DeleteItemUseCase delegates delete to repository`() = runTest {
        val useCase = DeleteItemUseCase(repository)
        coEvery { repository.deleteItem(item) } returns Unit

        useCase(item)

        coVerify { repository.deleteItem(item) }
    }

    // ── AssignItemToCharacterUseCase ─────────────────────────────────────────

    @Test
    fun `AssignItemToCharacterUseCase returns assignment id from repository`() = runTest {
        val useCase = AssignItemToCharacterUseCase(repository)
        coEvery { repository.assignItemToCharacter(characterId = 1L, itemId = 1L, quantity = 2) } returns 99L

        val result = useCase(characterId = 1L, itemId = 1L, quantity = 2)

        assertEquals(99L, result)
        coVerify { repository.assignItemToCharacter(1L, 1L, 2) }
    }

    // ── UnassignItemFromCharacterUseCase ─────────────────────────────────────

    @Test
    fun `UnassignItemFromCharacterUseCase delegates unassign to repository`() = runTest {
        val useCase = UnassignItemFromCharacterUseCase(repository)
        coEvery { repository.unassignItemFromCharacter(assignmentId = 10L) } returns Unit

        useCase(10L)

        coVerify { repository.unassignItemFromCharacter(10L) }
    }

    // ── UpdateItemQuantityUseCase ────────────────────────────────────────────

    @Test
    fun `UpdateItemQuantityUseCase delegates quantity update to repository`() = runTest {
        val useCase = UpdateItemQuantityUseCase(repository)
        coEvery { repository.updateItemQuantity(assignmentId = 10L, quantity = 5) } returns Unit

        useCase(assignmentId = 10L, quantity = 5)

        coVerify { repository.updateItemQuantity(10L, 5) }
    }
}


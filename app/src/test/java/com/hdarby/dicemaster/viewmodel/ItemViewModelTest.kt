package com.hdarby.dicemaster.viewmodel

import app.cash.turbine.test
import com.hdarby.dicemaster.domain.model.CharacterItemEntry
import com.hdarby.dicemaster.domain.model.ConsumableItem
import com.hdarby.dicemaster.domain.usecase.item.AddItemUseCase
import com.hdarby.dicemaster.domain.usecase.item.AssignItemToCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.item.DeleteItemUseCase
import com.hdarby.dicemaster.domain.usecase.item.GetItemsByCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.item.GetItemsUseCase
import com.hdarby.dicemaster.domain.usecase.item.UnassignItemFromCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.item.UpdateItemQuantityUseCase
import com.hdarby.dicemaster.domain.usecase.item.UpdateItemUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ItemViewModelTest {

    private val getItemsUseCase: GetItemsUseCase = mockk()
    private val getItemsByCharacterUseCase: GetItemsByCharacterUseCase = mockk()
    private val addItemUseCase: AddItemUseCase = mockk()
    private val updateItemUseCase: UpdateItemUseCase = mockk()
    private val deleteItemUseCase: DeleteItemUseCase = mockk()
    private val assignItemToCharacterUseCase: AssignItemToCharacterUseCase = mockk()
    private val unassignItemFromCharacterUseCase: UnassignItemFromCharacterUseCase = mockk()
    private val updateItemQuantityUseCase: UpdateItemQuantityUseCase = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ItemViewModel

    private val healingPotion = ConsumableItem(1L, "Healing Potion", "Restores 2d4+2 HP")
    private val characterItemEntry = CharacterItemEntry(item = healingPotion, quantity = 3)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getItemsUseCase() } returns flowOf(listOf(healingPotion))
        every { getItemsByCharacterUseCase() } returns flowOf(emptyMap())
        viewModel = buildViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = ItemViewModel(
        getItemsUseCase,
        getItemsByCharacterUseCase,
        addItemUseCase,
        updateItemUseCase,
        deleteItemUseCase,
        assignItemToCharacterUseCase,
        unassignItemFromCharacterUseCase,
        updateItemQuantityUseCase
    )

    // --- Initialisation ---

    @Test
    fun `initialization loads items`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(listOf(healingPotion), state.items)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `initialization loads items by character`() = runTest {
        val itemsMap = mapOf(1L to listOf(characterItemEntry))
        every { getItemsByCharacterUseCase() } returns flowOf(itemsMap)

        val vm = buildViewModel()
        vm.uiState.test {
            assertEquals(itemsMap, awaitItem().itemsByCharacterId)
        }
    }

    @Test
    fun `loading state is updated during item load`() = runTest {
        every { getItemsUseCase() } returns flow {
            kotlinx.coroutines.delay(100)
            emit(listOf(healingPotion))
        }

        val vm = buildViewModel()
        vm.uiState.test {
            assertTrue(awaitItem().isLoading)
            assertFalse(awaitItem().isLoading)
        }
    }

    @Test
    fun `error state is set when item load fails`() = runTest {
        val errorMessage = "Failed to load items"
        every { getItemsUseCase() } returns flow { throw RuntimeException(errorMessage) }

        val vm = buildViewModel()
        vm.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    // --- CRUD ---

    @Test
    fun `addItem calls use case`() = runTest {
        coEvery { addItemUseCase(healingPotion) } returns 1L

        viewModel.addItem(healingPotion)

        coVerify { addItemUseCase(healingPotion) }
    }

    @Test
    fun `updateItem calls use case`() = runTest {
        coEvery { updateItemUseCase(healingPotion) } returns Unit

        viewModel.updateItem(healingPotion)

        coVerify { updateItemUseCase(healingPotion) }
    }

    @Test
    fun `deleteItem calls use case`() = runTest {
        coEvery { deleteItemUseCase(healingPotion) } returns Unit

        viewModel.deleteItem(healingPotion)

        coVerify { deleteItemUseCase(healingPotion) }
    }

    // --- Assignment ---

    @Test
    fun `assignItem calls use case`() = runTest {
        coEvery { assignItemToCharacterUseCase(1L, 1L) } returns Unit

        viewModel.assignItem(characterId = 1L, itemId = 1L)

        coVerify { assignItemToCharacterUseCase(1L, 1L) }
    }

    @Test
    fun `unassignItem calls use case`() = runTest {
        coEvery { unassignItemFromCharacterUseCase(1L, 1L) } returns Unit

        viewModel.unassignItem(characterId = 1L, itemId = 1L)

        coVerify { unassignItemFromCharacterUseCase(1L, 1L) }
    }

    // --- Quantity management ---

    @Test
    fun `incrementQuantity calls updateItemQuantity with currentQuantity plus one`() = runTest {
        coEvery { updateItemQuantityUseCase(1L, 1L, 4) } returns Unit

        viewModel.incrementQuantity(characterId = 1L, itemId = 1L, currentQuantity = 3)

        coVerify { updateItemQuantityUseCase(1L, 1L, 4) }
    }

    @Test
    fun `decrementQuantity calls updateItemQuantity when currentQuantity is greater than one`() = runTest {
        coEvery { updateItemQuantityUseCase(1L, 1L, 2) } returns Unit

        viewModel.decrementQuantity(characterId = 1L, itemId = 1L, currentQuantity = 3)

        coVerify { updateItemQuantityUseCase(1L, 1L, 2) }
    }

    @Test
    fun `decrementQuantity unassigns item when currentQuantity is one`() = runTest {
        coEvery { unassignItemFromCharacterUseCase(1L, 1L) } returns Unit

        viewModel.decrementQuantity(characterId = 1L, itemId = 1L, currentQuantity = 1)

        coVerify { unassignItemFromCharacterUseCase(1L, 1L) }
        coVerify(exactly = 0) { updateItemQuantityUseCase(any(), any(), any()) }
    }

    @Test
    fun `decrementQuantity unassigns item when currentQuantity is zero`() = runTest {
        coEvery { unassignItemFromCharacterUseCase(1L, 1L) } returns Unit

        viewModel.decrementQuantity(characterId = 1L, itemId = 1L, currentQuantity = 0)

        coVerify { unassignItemFromCharacterUseCase(1L, 1L) }
    }

    // --- Error handling ---

    @Test
    fun `addItem sets error on failure`() = runTest {
        val errorMessage = "Failed to add item"
        coEvery { addItemUseCase(any()) } throws Exception(errorMessage)

        viewModel.addItem(healingPotion)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `updateItem sets error on failure`() = runTest {
        val errorMessage = "Failed to update item"
        coEvery { updateItemUseCase(any()) } throws Exception(errorMessage)

        viewModel.updateItem(healingPotion)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `deleteItem sets error on failure`() = runTest {
        val errorMessage = "Failed to delete item"
        coEvery { deleteItemUseCase(any()) } throws Exception(errorMessage)

        viewModel.deleteItem(healingPotion)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `assignItem sets error on failure`() = runTest {
        val errorMessage = "Failed to assign item"
        coEvery { assignItemToCharacterUseCase(any(), any()) } throws Exception(errorMessage)

        viewModel.assignItem(characterId = 1L, itemId = 1L)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `unassignItem sets error on failure`() = runTest {
        val errorMessage = "Failed to unassign item"
        coEvery { unassignItemFromCharacterUseCase(any(), any()) } throws Exception(errorMessage)

        viewModel.unassignItem(characterId = 1L, itemId = 1L)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `incrementQuantity sets error on failure`() = runTest {
        val errorMessage = "Failed to update quantity"
        coEvery { updateItemQuantityUseCase(any(), any(), any()) } throws Exception(errorMessage)

        viewModel.incrementQuantity(characterId = 1L, itemId = 1L, currentQuantity = 2)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `decrementQuantity sets error on failure`() = runTest {
        val errorMessage = "Failed to decrement quantity"
        coEvery { updateItemQuantityUseCase(any(), any(), any()) } throws Exception(errorMessage)

        viewModel.decrementQuantity(characterId = 1L, itemId = 1L, currentQuantity = 3)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }
}


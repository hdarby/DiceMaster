package com.hdarby.dicemaster.viewmodel

import app.cash.turbine.test
import com.hdarby.dicemaster.domain.model.Session
import com.hdarby.dicemaster.domain.model.UserRole
import com.hdarby.dicemaster.domain.repository.SessionRepository
import com.hdarby.dicemaster.domain.usecase.item.AddItemUseCase
import com.hdarby.dicemaster.domain.usecase.item.AssignItemToCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.item.DeleteItemUseCase
import com.hdarby.dicemaster.domain.usecase.item.GetItemsByCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.item.GetItemsUseCase
import com.hdarby.dicemaster.domain.usecase.item.UnassignItemFromCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.item.UpdateItemQuantityUseCase
import com.hdarby.dicemaster.domain.usecase.item.UpdateItemUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ItemViewModelRoleTest {

    private val getItemsUseCase: GetItemsUseCase = mockk()
    private val getItemsByCharacterUseCase: GetItemsByCharacterUseCase = mockk()
    private val addItemUseCase: AddItemUseCase = mockk(relaxed = true)
    private val updateItemUseCase: UpdateItemUseCase = mockk(relaxed = true)
    private val deleteItemUseCase: DeleteItemUseCase = mockk(relaxed = true)
    private val assignItemToCharacterUseCase: AssignItemToCharacterUseCase = mockk(relaxed = true)
    private val unassignItemFromCharacterUseCase: UnassignItemFromCharacterUseCase = mockk(relaxed = true)
    private val updateItemQuantityUseCase: UpdateItemQuantityUseCase = mockk(relaxed = true)
    private val sessionRepository: SessionRepository = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getItemsUseCase() } returns flowOf(emptyList())
        every { getItemsByCharacterUseCase() } returns flowOf(emptyMap())
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
        updateItemQuantityUseCase,
        sessionRepository
    )

    @Test
    fun `no session - userRole is null`() = runTest {
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildViewModel().uiState.test {
            assertNull(awaitItem().userRole)
        }
    }

    @Test
    fun `DM role - userRole is DungeonMaster in state`() = runTest {
        every { sessionRepository.observeSession() } returns flowOf(
            Session("ABC123", UserRole.DungeonMaster)
        )

        buildViewModel().uiState.test {
            assertEquals(UserRole.DungeonMaster, awaitItem().userRole)
        }
    }

    @Test
    fun `Player role - userRole is Player in state`() = runTest {
        val role = UserRole.Player(characterId = 5)
        every { sessionRepository.observeSession() } returns flowOf(Session("ABC123", role))

        buildViewModel().uiState.test {
            assertEquals(role, awaitItem().userRole)
        }
    }

    @Test
    fun `role changes are reflected reactively`() = runTest {
        val sessionFlow = MutableStateFlow<Session?>(null)
        every { sessionRepository.observeSession() } returns sessionFlow

        val vm = buildViewModel()

        vm.uiState.test {
            assertNull(awaitItem().userRole)

            sessionFlow.value = Session("ABC123", UserRole.DungeonMaster)
            assertEquals(UserRole.DungeonMaster, awaitItem().userRole)

            sessionFlow.value = Session("ABC123", UserRole.Player(characterId = 2))
            assertEquals(UserRole.Player(characterId = 2), awaitItem().userRole)
        }
    }
}


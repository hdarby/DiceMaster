package com.hdarby.dicemaster.viewmodel

import app.cash.turbine.test
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.domain.repository.SessionRepository
import com.hdarby.dicemaster.domain.usecase.weapon.AddWeaponUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.DeleteWeaponUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.GetWeaponsUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.UpdateWeaponUseCase
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
class WeaponViewModelTest {

    private val getWeaponsUseCase: GetWeaponsUseCase = mockk()
    private val addWeaponUseCase: AddWeaponUseCase = mockk()
    private val updateWeaponUseCase: UpdateWeaponUseCase = mockk()
    private val deleteWeaponUseCase: DeleteWeaponUseCase = mockk()
    private val sessionRepository: SessionRepository = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: WeaponViewModel

    private val weapon = Weapon(1, "Greataxe", com.hdarby.dicemaster.domain.model.WeaponType.MARTIAL_MELEE, com.hdarby.dicemaster.domain.model.DamageDice.D12, com.hdarby.dicemaster.domain.model.DamageType.SLASHING, 0, 2)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getWeaponsUseCase() } returns flowOf(listOf(weapon))
        every { sessionRepository.observeSession() } returns flowOf(null)
        viewModel = WeaponViewModel(getWeaponsUseCase, addWeaponUseCase, updateWeaponUseCase, deleteWeaponUseCase, sessionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads weapons`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(listOf(weapon), state.weapons)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `addWeapon calls use case`() = runTest {
        coEvery { addWeaponUseCase(weapon) } returns 1L

        viewModel.addWeapon(weapon)

        coVerify { addWeaponUseCase(weapon) }
    }

    @Test
    fun `updateWeapon calls use case`() = runTest {
        coEvery { updateWeaponUseCase(weapon) } returns Unit

        viewModel.updateWeapon(weapon)

        coVerify { updateWeaponUseCase(weapon) }
    }

    @Test
    fun `deleteWeapon calls use case`() = runTest {
        coEvery { deleteWeaponUseCase(weapon) } returns Unit

        viewModel.deleteWeapon(weapon)

        coVerify { deleteWeaponUseCase(weapon) }
    }

    @Test
    fun `addWeapon updates error on failure`() = runTest {
        val errorMessage = "Failed to add weapon"
        coEvery { addWeaponUseCase(any()) } throws Exception(errorMessage)

        viewModel.addWeapon(weapon)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `updateWeapon updates error on failure`() = runTest {
        val errorMessage = "Failed to update weapon"
        coEvery { updateWeaponUseCase(any()) } throws Exception(errorMessage)

        viewModel.updateWeapon(weapon)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `deleteWeapon updates error on failure`() = runTest {
        val errorMessage = "Failed to delete weapon"
        coEvery { deleteWeaponUseCase(any()) } throws Exception(errorMessage)

        viewModel.deleteWeapon(weapon)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `loading state is updated during load`() = runTest {
        every { getWeaponsUseCase() } returns flow {
            kotlinx.coroutines.delay(100)
            emit(listOf(weapon))
        }

        val vm = WeaponViewModel(getWeaponsUseCase, addWeaponUseCase, updateWeaponUseCase, deleteWeaponUseCase, sessionRepository)

        vm.uiState.test {
            assertTrue(awaitItem().isLoading)
            assertFalse(awaitItem().isLoading)
        }
    }

    @Test
    fun `error state is updated on error`() = runTest {
        val errorMessage = "Failed to load weapons"
        every { getWeaponsUseCase() } returns flow { throw RuntimeException(errorMessage) }

        val vm = WeaponViewModel(getWeaponsUseCase, addWeaponUseCase, updateWeaponUseCase, deleteWeaponUseCase, sessionRepository)

        vm.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }
}

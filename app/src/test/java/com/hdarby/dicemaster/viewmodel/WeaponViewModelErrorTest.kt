package com.hdarby.dicemaster.viewmodel

import app.cash.turbine.test
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.domain.repository.SessionRepository
import com.hdarby.dicemaster.domain.usecase.weapon.AddWeaponUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.DeleteWeaponUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.GetWeaponsUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.UpdateWeaponUseCase
import io.mockk.coEvery
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeaponViewModelErrorTest {

    private val getWeaponsUseCase: GetWeaponsUseCase = mockk()
    private val addWeaponUseCase: AddWeaponUseCase = mockk()
    private val updateWeaponUseCase: UpdateWeaponUseCase = mockk()
    private val deleteWeaponUseCase: DeleteWeaponUseCase = mockk()
    private val sessionRepository: SessionRepository = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val weapon = Weapon(
        id = 1,
        name = "Longsword",
        weaponType = com.hdarby.dicemaster.domain.model.WeaponType.MARTIAL_MELEE,
        damageDice = com.hdarby.dicemaster.domain.model.DamageDice.D8,
        damageType = com.hdarby.dicemaster.domain.model.DamageType.SLASHING,
        damageModifier = 2
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { sessionRepository.observeSession() } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = WeaponViewModel(getWeaponsUseCase, addWeaponUseCase, updateWeaponUseCase, deleteWeaponUseCase, sessionRepository)

    @Test
    fun `loadWeapons handles database error`() = runTest {
        val errorMessage = "Failed to load weapons"
        every { getWeaponsUseCase() } returns flow { throw Exception(errorMessage) }

        buildViewModel().uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `addWeapon handles constraint violation on duplicate name`() = runTest {
        every { getWeaponsUseCase() } returns flowOf(emptyList())
        val errorMessage = "Weapon with this name already exists"
        coEvery { addWeaponUseCase(any()) } throws Exception(errorMessage)

        val viewModel = buildViewModel()
        viewModel.addWeapon(weapon)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `updateWeapon with invalid data`() = runTest {
        every { getWeaponsUseCase() } returns flowOf(listOf(weapon))
        val errorMessage = "Invalid weapon data"
        coEvery { updateWeaponUseCase(any()) } throws IllegalArgumentException(errorMessage)

        val viewModel = buildViewModel()
        viewModel.updateWeapon(weapon)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `deleteWeapon handles referenced weapon error`() = runTest {
        every { getWeaponsUseCase() } returns flowOf(listOf(weapon))
        val errorMessage = "Cannot delete weapon that is assigned to a character"
        coEvery { deleteWeaponUseCase(any()) } throws Exception(errorMessage)

        val viewModel = buildViewModel()
        viewModel.deleteWeapon(weapon)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `concurrent operations preserve weapon list on error`() = runTest {
        every { getWeaponsUseCase() } returns flowOf(listOf(weapon))
        coEvery { updateWeaponUseCase(any()) } throws Exception("Update failed")

        val viewModel = buildViewModel()
        viewModel.updateWeapon(weapon)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(listOf(weapon), state.weapons)
            assertEquals("Update failed", state.error)
        }
    }
}


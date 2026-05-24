package com.hdarby.dicemaster.viewmodel

import app.cash.turbine.test
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.domain.repository.SessionRepository
import com.hdarby.dicemaster.domain.usecase.weapon.AddWeaponUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.DeleteWeaponUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.GetWeaponsUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.UpdateWeaponUseCase
import com.hdarby.dicemaster.domain.usecase.character.AssignWeaponToCharacterUseCase
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
    private val assignWeaponToCharacterUseCase: AssignWeaponToCharacterUseCase = mockk()
    private val sessionRepository: SessionRepository = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val weapon = Weapon(
        id = 1,
        name = "Longsword",
        type = "Melee",
        damageDice = "1d8",
        damageType = "Slashing",
        modifier = 2
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

    @Test
    fun `loadWeapons handles database error`() = runTest {
        val errorMessage = "Failed to load weapons"
        every { getWeaponsUseCase() } returns flow {
            throw Exception(errorMessage)
        }

        val viewModel = WeaponViewModel(
            getWeaponsUseCase, addWeaponUseCase, updateWeaponUseCase,
            deleteWeaponUseCase, assignWeaponToCharacterUseCase, sessionRepository
        )

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(errorMessage, state.error)
        }
    }

    @Test
    fun `addWeapon handles constraint violation on duplicate name`() = runTest {
        every { getWeaponsUseCase() } returns flowOf(emptyList())
        val errorMessage = "Weapon with this name already exists"
        coEvery { addWeaponUseCase(any()) } throws Exception(errorMessage)

        val viewModel = WeaponViewModel(
            getWeaponsUseCase, addWeaponUseCase, updateWeaponUseCase,
            deleteWeaponUseCase, assignWeaponToCharacterUseCase, sessionRepository
        )

        viewModel.addWeapon(weapon)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(errorMessage, state.error)
        }
    }

    @Test
    fun `updateWeapon with invalid data`() = runTest {
        every { getWeaponsUseCase() } returns flowOf(listOf(weapon))
        val errorMessage = "Invalid weapon data"
        coEvery { updateWeaponUseCase(any()) } throws IllegalArgumentException(errorMessage)

        val viewModel = WeaponViewModel(
            getWeaponsUseCase, addWeaponUseCase, updateWeaponUseCase,
            deleteWeaponUseCase, assignWeaponToCharacterUseCase, sessionRepository
        )

        viewModel.updateWeapon(weapon)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(errorMessage, state.error)
        }
    }

    @Test
    fun `deleteWeapon handles referenced weapon error`() = runTest {
        every { getWeaponsUseCase() } returns flowOf(listOf(weapon))
        val errorMessage = "Cannot delete weapon that is assigned to a character"
        coEvery { deleteWeaponUseCase(any()) } throws Exception(errorMessage)

        val viewModel = WeaponViewModel(
            getWeaponsUseCase, addWeaponUseCase, updateWeaponUseCase,
            deleteWeaponUseCase, assignWeaponToCharacterUseCase, sessionRepository
        )

        viewModel.deleteWeapon(weapon)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(errorMessage, state.error)
        }
    }

    @Test
    fun `assignWeaponToCharacter handles invalid character ID`() = runTest {
        every { getWeaponsUseCase() } returns flowOf(listOf(weapon))
        val errorMessage = "Character not found"
        coEvery { assignWeaponToCharacterUseCase(any(), any()) } throws Exception(errorMessage)

        val viewModel = WeaponViewModel(
            getWeaponsUseCase, addWeaponUseCase, updateWeaponUseCase,
            deleteWeaponUseCase, assignWeaponToCharacterUseCase, sessionRepository
        )

        viewModel.assignWeaponToCharacter(99L, weapon.id)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(errorMessage, state.error)
        }
    }

    @Test
    fun `concurrent operations preserve weapon list on error`() = runTest {
        every { getWeaponsUseCase() } returns flowOf(listOf(weapon))
        coEvery { updateWeaponUseCase(any()) } throws Exception("Update failed")

        val viewModel = WeaponViewModel(
            getWeaponsUseCase, addWeaponUseCase, updateWeaponUseCase,
            deleteWeaponUseCase, assignWeaponToCharacterUseCase, sessionRepository
        )

        viewModel.updateWeapon(weapon)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(listOf(weapon), state.weapons)
            assertEquals("Update failed", state.error)
        }
    }
}

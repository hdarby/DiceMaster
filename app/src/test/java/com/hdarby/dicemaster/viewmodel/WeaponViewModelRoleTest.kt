package com.hdarby.dicemaster.viewmodel

import app.cash.turbine.test
import com.hdarby.dicemaster.domain.model.Session
import com.hdarby.dicemaster.domain.model.UserRole
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.domain.repository.SessionRepository
import com.hdarby.dicemaster.domain.usecase.weapon.AddWeaponUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.DeleteWeaponUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.GetWeaponsUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.UpdateWeaponUseCase
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
class WeaponViewModelRoleTest {

    private val getWeaponsUseCase: GetWeaponsUseCase = mockk()
    private val addWeaponUseCase: AddWeaponUseCase = mockk(relaxed = true)
    private val updateWeaponUseCase: UpdateWeaponUseCase = mockk(relaxed = true)
    private val deleteWeaponUseCase: DeleteWeaponUseCase = mockk(relaxed = true)
    private val sessionRepository: SessionRepository = mockk()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val weapon = Weapon(1, "Greataxe", com.hdarby.dicemaster.domain.model.WeaponType.MARTIAL_MELEE, com.hdarby.dicemaster.domain.model.DamageDice.D12, com.hdarby.dicemaster.domain.model.DamageType.SLASHING, 0, 2)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getWeaponsUseCase() } returns flowOf(listOf(weapon))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = WeaponViewModel(
        getWeaponsUseCase, addWeaponUseCase, updateWeaponUseCase, deleteWeaponUseCase, sessionRepository
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
        every { sessionRepository.observeSession() } returns flowOf(Session("ABC123", UserRole.DungeonMaster))

        buildViewModel().uiState.test {
            assertEquals(UserRole.DungeonMaster, awaitItem().userRole)
        }
    }

    @Test
    fun `Player role - userRole is Player in state`() = runTest {
        val role = UserRole.Player(characterId = 3)
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

            sessionFlow.value = Session("ABC123", UserRole.Player(characterId = 1))
            assertEquals(UserRole.Player(characterId = 1), awaitItem().userRole)
        }
    }
}


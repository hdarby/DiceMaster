package com.hdarby.dicemaster.viewmodel

import app.cash.turbine.test
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.CharacterWithWeapons
import com.hdarby.dicemaster.domain.model.Session
import com.hdarby.dicemaster.domain.model.Stats
import com.hdarby.dicemaster.domain.model.UserRole
import com.hdarby.dicemaster.domain.repository.SessionRepository
import com.hdarby.dicemaster.domain.usecase.character.AddCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.AssignWeaponToCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.DamageCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.DeleteCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.GetCharactersWithWeaponsUseCase
import com.hdarby.dicemaster.domain.usecase.character.HealCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.LevelUpCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.MarkCharacterDeadUseCase
import com.hdarby.dicemaster.domain.usecase.character.SetDeathSaveFailuresUseCase
import com.hdarby.dicemaster.domain.usecase.character.UnassignWeaponFromCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.UpdateCharacterUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterViewModelRoleFilteringTest {

    private val getCharactersWithWeaponsUseCase: GetCharactersWithWeaponsUseCase = mockk()
    private val addCharacterUseCase: AddCharacterUseCase = mockk(relaxed = true)
    private val updateCharacterUseCase: UpdateCharacterUseCase = mockk(relaxed = true)
    private val deleteCharacterUseCase: DeleteCharacterUseCase = mockk(relaxed = true)
    private val unassignWeaponFromCharacterUseCase: UnassignWeaponFromCharacterUseCase = mockk(relaxed = true)
    private val assignWeaponToCharacterUseCase: AssignWeaponToCharacterUseCase = mockk(relaxed = true)
    private val sessionRepository: SessionRepository = mockk()
    private val healCharacterUseCase: HealCharacterUseCase = mockk(relaxed = true)
    private val damageCharacterUseCase: DamageCharacterUseCase = mockk(relaxed = true)
    private val setDeathSaveFailuresUseCase: SetDeathSaveFailuresUseCase = mockk(relaxed = true)
    private val markCharacterDeadUseCase: MarkCharacterDeadUseCase = mockk(relaxed = true)
    private val levelUpCharacterUseCase: LevelUpCharacterUseCase = mockk(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()

    private val defaultStats = Stats(10, 0, 10, 0, 10, 0, 10, 0, 10, 0, 10, 0)
    private val character1 = Character(id = 1, name = "Grog", race = "Goliath", stats = defaultStats)
    private val character2 = Character(id = 2, name = "Vex", race = "Half-Elf", stats = defaultStats)
    private val cww1 = CharacterWithWeapons(character1, emptyList())
    private val cww2 = CharacterWithWeapons(character2, emptyList())
    private val allCharacters = listOf(cww1, cww2)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getCharactersWithWeaponsUseCase() } returns flowOf(allCharacters)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(): CharacterViewModel = CharacterViewModel(
        getCharactersWithWeaponsUseCase, addCharacterUseCase, updateCharacterUseCase,
        deleteCharacterUseCase, unassignWeaponFromCharacterUseCase,
        assignWeaponToCharacterUseCase, sessionRepository,
        healCharacterUseCase, damageCharacterUseCase,
        setDeathSaveFailuresUseCase, markCharacterDeadUseCase,
        levelUpCharacterUseCase
    )

    // ── No-session (local-only) mode ─────────────────────────────────────────

    @Test
    fun `no session - all characters are visible`() = runTest {
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildViewModel().uiState.test {
            val state = awaitItem()
            assertEquals(allCharacters, state.characters)
        }
    }

    @Test
    fun `no session - userRole is null in state`() = runTest {
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildViewModel().uiState.test {
            assertNull(awaitItem().userRole)
        }
    }

    // ── DungeonMaster role ────────────────────────────────────────────────────

    @Test
    fun `DM role - all characters are visible`() = runTest {
        val dmSession = Session(sessionId = "ABC123", role = UserRole.DungeonMaster)
        every { sessionRepository.observeSession() } returns flowOf(dmSession)

        buildViewModel().uiState.test {
            val state = awaitItem()
            assertEquals(allCharacters, state.characters)
        }
    }

    @Test
    fun `DM role - userRole is DungeonMaster in state`() = runTest {
        val dmSession = Session(sessionId = "ABC123", role = UserRole.DungeonMaster)
        every { sessionRepository.observeSession() } returns flowOf(dmSession)

        buildViewModel().uiState.test {
            assertEquals(UserRole.DungeonMaster, awaitItem().userRole)
        }
    }

    // ── Player role ───────────────────────────────────────────────────────────

    @Test
    fun `Player role - only own character is visible`() = runTest {
        val playerSession = Session(sessionId = "ABC123", role = UserRole.Player(characterId = 2))
        every { sessionRepository.observeSession() } returns flowOf(playerSession)

        buildViewModel().uiState.test {
            val state = awaitItem()
            assertEquals(listOf(cww2), state.characters)
        }
    }

    @Test
    fun `Player role - other characters are not visible`() = runTest {
        val playerSession = Session(sessionId = "ABC123", role = UserRole.Player(characterId = 2))
        every { sessionRepository.observeSession() } returns flowOf(playerSession)

        buildViewModel().uiState.test {
            val characters = awaitItem().characters
            assertTrue(characters.none { it.character.id == 1L })
        }
    }

    @Test
    fun `Player role - userRole is Player(characterId) in state`() = runTest {
        val role = UserRole.Player(characterId = 2)
        every { sessionRepository.observeSession() } returns flowOf(Session("ABC123", role))

        buildViewModel().uiState.test {
            assertEquals(role, awaitItem().userRole)
        }
    }

    @Test
    fun `Player role - characterId has no matching character - empty list`() = runTest {
        val playerSession = Session(sessionId = "ABC123", role = UserRole.Player(characterId = 99))
        every { sessionRepository.observeSession() } returns flowOf(playerSession)

        buildViewModel().uiState.test {
            assertTrue(awaitItem().characters.isEmpty())
        }
    }

    // ── Reactive role changes ─────────────────────────────────────────────────

    @Test
    fun `role change from null to Player - characters list updates reactively`() = runTest {
        val sessionFlow = MutableStateFlow<Session?>(null)
        every { sessionRepository.observeSession() } returns sessionFlow

        val vm = buildViewModel()

        vm.uiState.test {
            // Initial: no session → all characters
            assertEquals(allCharacters, awaitItem().characters)

            // Session is set to Player
            sessionFlow.value = Session("ABC123", UserRole.Player(characterId = 1))

            // Re-emits with filtered list
            val updatedState = awaitItem()
            assertEquals(listOf(cww1), updatedState.characters)
            assertEquals(UserRole.Player(characterId = 1), updatedState.userRole)
        }
    }

    @Test
    fun `role change from Player to DM - all characters become visible`() = runTest {
        val sessionFlow = MutableStateFlow<Session?>(
            Session("ABC123", UserRole.Player(characterId = 1))
        )
        every { sessionRepository.observeSession() } returns sessionFlow

        val vm = buildViewModel()

        vm.uiState.test {
            // Initial: Player → only character 1
            assertEquals(listOf(cww1), awaitItem().characters)

            // Upgrade to DM
            sessionFlow.value = Session("ABC123", UserRole.DungeonMaster)

            // Re-emits with all characters
            val updatedState = awaitItem()
            assertEquals(allCharacters, updatedState.characters)
            assertEquals(UserRole.DungeonMaster, updatedState.userRole)
        }
    }
}










package com.hdarby.dicemaster.viewmodel

import app.cash.turbine.test
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.CharacterWithWeapons
import com.hdarby.dicemaster.domain.model.Stats
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
class CharacterViewModelTest {

    private val getCharactersWithWeaponsUseCase: GetCharactersWithWeaponsUseCase = mockk()
    private val addCharacterUseCase: AddCharacterUseCase = mockk()
    private val updateCharacterUseCase: UpdateCharacterUseCase = mockk()
    private val deleteCharacterUseCase: DeleteCharacterUseCase = mockk()
    private val unassignWeaponFromCharacterUseCase: UnassignWeaponFromCharacterUseCase = mockk()
    private val assignWeaponToCharacterUseCase: AssignWeaponToCharacterUseCase = mockk()
    private val sessionRepository: SessionRepository = mockk()
    private val healCharacterUseCase: HealCharacterUseCase = mockk(relaxed = true)
    private val damageCharacterUseCase: DamageCharacterUseCase = mockk(relaxed = true)
    private val setDeathSaveFailuresUseCase: SetDeathSaveFailuresUseCase = mockk(relaxed = true)
    private val markCharacterDeadUseCase: MarkCharacterDeadUseCase = mockk(relaxed = true)
    private val levelUpCharacterUseCase: LevelUpCharacterUseCase = mockk(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: CharacterViewModel

    private fun buildViewModel() = CharacterViewModel(
        getCharactersWithWeaponsUseCase,
        addCharacterUseCase,
        updateCharacterUseCase,
        deleteCharacterUseCase,
        unassignWeaponFromCharacterUseCase,
        assignWeaponToCharacterUseCase,
        sessionRepository,
        healCharacterUseCase,
        damageCharacterUseCase,
        setDeathSaveFailuresUseCase,
        markCharacterDeadUseCase,
        levelUpCharacterUseCase
    )

    private val character = Character(
        id = 1,
        name = "Grog",
        race = "Goliath",
        stats = Stats(
            strength = 20, strengthModifier = 5,
            dexterity = 12, dexterityModifier = 1,
            constitution = 18, constitutionModifier = 4,
            intelligence = 6, intelligenceModifier = -2,
            wisdom = 10, wisdomModifier = 0,
            charisma = 8, charismaModifier = -1
        )
    )
    private val characterWithWeapons = CharacterWithWeapons(character, emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getCharactersWithWeaponsUseCase() } returns flowOf(listOf(characterWithWeapons))
        every { sessionRepository.observeSession() } returns flowOf(null)
        viewModel = buildViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads characters`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(listOf(characterWithWeapons), state.characters)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `addCharacter calls use case`() = runTest {
        coEvery { addCharacterUseCase(character) } returns 1L
        
        viewModel.addCharacter(character)
        
        coVerify { addCharacterUseCase(character) }
    }

    @Test
    fun `updateCharacter calls use case`() = runTest {
        coEvery { updateCharacterUseCase(character) } returns Unit
        
        viewModel.updateCharacter(character)
        
        coVerify { updateCharacterUseCase(character) }
    }

    @Test
    fun `deleteCharacter calls use case`() = runTest {
        coEvery { deleteCharacterUseCase(character) } returns Unit
        
        viewModel.deleteCharacter(character)
        
        coVerify { deleteCharacterUseCase(character) }
    }

    @Test
    fun `unassignWeapon calls use case`() = runTest {
        coEvery { unassignWeaponFromCharacterUseCase(2L) } returns Unit

        viewModel.unassignWeapon(2L)

        coVerify { unassignWeaponFromCharacterUseCase(2L) }
    }

    @Test
    fun `addCharacter updates error on failure`() = runTest {
        val errorMessage = "Failed to add"
        coEvery { addCharacterUseCase(any()) } throws Exception(errorMessage)

        viewModel.addCharacter(character)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `updateCharacter updates error on failure`() = runTest {
        val errorMessage = "Failed to update"
        coEvery { updateCharacterUseCase(any()) } throws Exception(errorMessage)
        
        viewModel.updateCharacter(character)
        
        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `deleteCharacter updates error on failure`() = runTest {
        val errorMessage = "Failed to delete"
        coEvery { deleteCharacterUseCase(any()) } throws Exception(errorMessage)
        
        viewModel.deleteCharacter(character)
        
        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `unassignWeapon updates error on failure`() = runTest {
        val errorMessage = "Failed to unassign"
        coEvery { unassignWeaponFromCharacterUseCase(any()) } throws Exception(errorMessage)

        viewModel.unassignWeapon(2L)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `loading state is updated during load`() = runTest {
        every { getCharactersWithWeaponsUseCase() } returns flow {
            kotlinx.coroutines.delay(100)
            emit(listOf(characterWithWeapons))
        }
        
        val vm = buildViewModel()

        vm.uiState.test {
            assertTrue(awaitItem().isLoading)
            assertFalse(awaitItem().isLoading)
        }
    }

    @Test
    fun `error state is updated on error`() = runTest {
        val errorMessage = "Failed to load"
        every { getCharactersWithWeaponsUseCase() } returns flow {
            throw RuntimeException(errorMessage)
        }
        
        val vm = buildViewModel()

        vm.uiState.test {
            // Initial state from init {} block load attempt
            val state = awaitItem()
            assertEquals(errorMessage, state.error)
        }
    }

    // ── Hit Points ViewModel actions ─────────────────────────────────────────

    @Test
    fun `heal calls healCharacterUseCase`() = runTest {
        viewModel.heal(character)
        coVerify { healCharacterUseCase(character) }
    }

    @Test
    fun `damage calls damageCharacterUseCase`() = runTest {
        viewModel.damage(character)
        coVerify { damageCharacterUseCase(character) }
    }

    @Test
    fun `setDeathSaveFailures calls setDeathSaveFailuresUseCase`() = runTest {
        viewModel.setDeathSaveFailures(character, 2)
        coVerify { setDeathSaveFailuresUseCase(character, 2) }
    }

    @Test
    fun `markDead calls markCharacterDeadUseCase`() = runTest {
        viewModel.markDead(character)
        coVerify { markCharacterDeadUseCase(character) }
    }

    @Test
    fun `heal updates error on failure`() = runTest {
        val errorMessage = "Heal failed"
        coEvery { healCharacterUseCase(any()) } throws Exception(errorMessage)

        viewModel.heal(character)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `damage updates error on failure`() = runTest {
        val errorMessage = "Damage failed"
        coEvery { damageCharacterUseCase(any()) } throws Exception(errorMessage)

        viewModel.damage(character)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `setDeathSaveFailures updates error on failure`() = runTest {
        val errorMessage = "Save update failed"
        coEvery { setDeathSaveFailuresUseCase(any(), any()) } throws Exception(errorMessage)

        viewModel.setDeathSaveFailures(character, 3)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    @Test
    fun `markDead updates error on failure`() = runTest {
        val errorMessage = "Mark dead failed"
        coEvery { markCharacterDeadUseCase(any()) } throws Exception(errorMessage)

        viewModel.markDead(character)

        viewModel.uiState.test {
            assertEquals(errorMessage, awaitItem().error)
        }
    }

    // ── Armor Class ──────────────────────────────────────────────────────────

    @Test
    fun `character armorClass is exposed correctly in uiState`() = runTest {
        val characterWithCustomAc = Character(
            id = 2, name = "Zariel", race = "Aasimar",
            stats = Stats(16, 3, 14, 2, 12, 1, 10, 0, 12, 1, 18, 4),
            armorClass = 19
        )
        val cwwWithAc = CharacterWithWeapons(characterWithCustomAc, emptyList())
        every { getCharactersWithWeaponsUseCase() } returns flowOf(listOf(cwwWithAc))
        every { sessionRepository.observeSession() } returns flowOf(null)
        val vm = buildViewModel()

        vm.uiState.test {
            assertEquals(19, awaitItem().characters[0].character.armorClass)
        }
    }
}

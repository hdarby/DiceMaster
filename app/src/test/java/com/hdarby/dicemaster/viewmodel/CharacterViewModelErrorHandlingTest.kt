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
import com.hdarby.dicemaster.domain.usecase.character.MarkCharacterDeadUseCase
import com.hdarby.dicemaster.domain.usecase.character.SetDeathSaveFailuresUseCase
import com.hdarby.dicemaster.domain.usecase.character.UnassignWeaponFromCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.UpdateCharacterUseCase
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
class CharacterViewModelErrorHandlingTest {

    private val getCharactersWithWeaponsUseCase: GetCharactersWithWeaponsUseCase = mockk()
    private val addCharacterUseCase: AddCharacterUseCase = mockk()
    private val updateCharacterUseCase: UpdateCharacterUseCase = mockk()
    private val deleteCharacterUseCase: DeleteCharacterUseCase = mockk()
    private val unassignWeaponFromCharacterUseCase: UnassignWeaponFromCharacterUseCase = mockk()
    private val assignWeaponToCharacterUseCase: AssignWeaponToCharacterUseCase = mockk(relaxed = true)
    private val sessionRepository: SessionRepository = mockk()
    private val healCharacterUseCase: HealCharacterUseCase = mockk(relaxed = true)
    private val damageCharacterUseCase: DamageCharacterUseCase = mockk(relaxed = true)
    private val setDeathSaveFailuresUseCase: SetDeathSaveFailuresUseCase = mockk(relaxed = true)
    private val markCharacterDeadUseCase: MarkCharacterDeadUseCase = mockk(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()

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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { sessionRepository.observeSession() } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = CharacterViewModel(
        getCharactersWithWeaponsUseCase, addCharacterUseCase, updateCharacterUseCase,
        deleteCharacterUseCase, unassignWeaponFromCharacterUseCase,
        assignWeaponToCharacterUseCase, sessionRepository,
        healCharacterUseCase, damageCharacterUseCase,
        setDeathSaveFailuresUseCase, markCharacterDeadUseCase
    )

    @Test
    fun `loadCharacters handles database connectivity error`() = runTest {
        val errorMessage = "Database connection failed"
        every { getCharactersWithWeaponsUseCase() } returns flow {
            throw Exception(errorMessage)
        }

        val viewModel = buildViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(errorMessage, state.error)
        }
    }

    @Test
    fun `addCharacter with null error message`() = runTest {
        every { getCharactersWithWeaponsUseCase() } returns flow { emit(emptyList()) }
        coEvery { addCharacterUseCase(any()) } throws NullPointerException()

        val viewModel = buildViewModel()

        viewModel.addCharacter(character)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(null, state.error)
        }
    }

    @Test
    fun `multiple error states preserve last error`() = runTest {
        every { getCharactersWithWeaponsUseCase() } returns flow { emit(emptyList()) }
        coEvery { addCharacterUseCase(any()) } throws Exception("Add error")

        val viewModel = buildViewModel()

        viewModel.addCharacter(character)
        coEvery { deleteCharacterUseCase(any()) } throws Exception("Delete error")
        viewModel.deleteCharacter(character)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Delete error", state.error)
        }
    }

    @Test
    fun `unassignWeapon error preserves character state`() = runTest {
        val characterWithWeapons = CharacterWithWeapons(character, emptyList())
        every { getCharactersWithWeaponsUseCase() } returns flow { emit(listOf(characterWithWeapons)) }
        coEvery { unassignWeaponFromCharacterUseCase(any()) } throws Exception("Unassign failed")

        val viewModel = buildViewModel()

        viewModel.unassignWeapon(2L)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(listOf(characterWithWeapons), state.characters)
            assertEquals("Unassign failed", state.error)
        }
    }
}

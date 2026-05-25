package com.hdarby.dicemaster.domain.usecase.character

import app.cash.turbine.test
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.CharacterWithWeapons
import com.hdarby.dicemaster.domain.model.Stats
import com.hdarby.dicemaster.domain.repository.CharacterRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterUseCasesTest {

    private val repository: CharacterRepository = mockk()
    
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

    @Test
    fun `GetCharactersUseCase returns characters from repository`() = runTest {
        val useCase = GetCharactersUseCase(repository)
        every { repository.getAllCharacters() } returns flowOf(listOf(character))

        useCase().test {
            assertEquals(listOf(character), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `GetCharactersWithWeaponsUseCase returns characters with weapons from repository`() = runTest {
        val useCase = GetCharactersWithWeaponsUseCase(repository)
        every { repository.getCharactersWithWeapons() } returns flowOf(listOf(characterWithWeapons))

        useCase().test {
            assertEquals(listOf(characterWithWeapons), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `AddCharacterUseCase calls repository`() = runTest {
        val useCase = AddCharacterUseCase(repository)
        coEvery { repository.addCharacter(character) } returns 1L

        val result = useCase(character)

        assertEquals(1L, result)
        coVerify { repository.addCharacter(character) }
    }

    @Test
    fun `UpdateCharacterUseCase calls repository`() = runTest {
        val useCase = UpdateCharacterUseCase(repository)
        coEvery { repository.updateCharacter(character) } returns Unit

        useCase(character)

        coVerify { repository.updateCharacter(character) }
    }

    @Test
    fun `DeleteCharacterUseCase calls repository`() = runTest {
        val useCase = DeleteCharacterUseCase(repository)
        coEvery { repository.deleteCharacter(character) } returns Unit

        useCase(character)

        coVerify { repository.deleteCharacter(character) }
    }

    @Test
    fun `AssignWeaponToCharacterUseCase calls repository`() = runTest {
        val useCase = AssignWeaponToCharacterUseCase(repository)
        coEvery { repository.assignWeaponToCharacter(1L, 2L) } returns Unit

        useCase(1L, 2L)

        coVerify { repository.assignWeaponToCharacter(1L, 2L) }
    }

    @Test
    fun `UnassignWeaponFromCharacterUseCase calls repository`() = runTest {
        val useCase = UnassignWeaponFromCharacterUseCase(repository)
        coEvery { repository.unassignWeaponFromCharacter(assignmentId = 5L) } returns Unit

        useCase(5L)

        coVerify { repository.unassignWeaponFromCharacter(5L) }
    }

    // ── Hit Points use cases ─────────────────────────────────────────────────

    @Test
    fun `HealCharacterUseCase increments currentHitPoints by one`() = runTest {
        val useCase = HealCharacterUseCase(repository)
        val char = character.copy(maxHitPoints = 20, currentHitPoints = 10)
        coEvery { repository.updateCharacter(any()) } returns Unit

        useCase(char)

        coVerify { repository.updateCharacter(match { it.currentHitPoints == 11 }) }
    }

    @Test
    fun `HealCharacterUseCase caps at maxHitPoints`() = runTest {
        val useCase = HealCharacterUseCase(repository)
        val char = character.copy(maxHitPoints = 20, currentHitPoints = 20)
        coEvery { repository.updateCharacter(any()) } returns Unit

        useCase(char)

        coVerify { repository.updateCharacter(match { it.currentHitPoints == 20 }) }
    }

    @Test
    fun `HealCharacterUseCase resets deathSaveFailures when healing from zero HP`() = runTest {
        val useCase = HealCharacterUseCase(repository)
        val char = character.copy(maxHitPoints = 20, currentHitPoints = 0, deathSaveFailures = 2)
        coEvery { repository.updateCharacter(any()) } returns Unit

        useCase(char)

        coVerify { repository.updateCharacter(match { it.currentHitPoints == 1 && it.deathSaveFailures == 0 }) }
    }

    @Test
    fun `HealCharacterUseCase preserves deathSaveFailures when healing above zero HP`() = runTest {
        val useCase = HealCharacterUseCase(repository)
        val char = character.copy(maxHitPoints = 20, currentHitPoints = 5, deathSaveFailures = 1)
        coEvery { repository.updateCharacter(any()) } returns Unit

        useCase(char)

        coVerify { repository.updateCharacter(match { it.currentHitPoints == 6 && it.deathSaveFailures == 1 }) }
    }

    @Test
    fun `DamageCharacterUseCase decrements currentHitPoints by one`() = runTest {
        val useCase = DamageCharacterUseCase(repository)
        val char = character.copy(maxHitPoints = 20, currentHitPoints = 10)
        coEvery { repository.updateCharacter(any()) } returns Unit

        useCase(char)

        coVerify { repository.updateCharacter(match { it.currentHitPoints == 9 }) }
    }

    @Test
    fun `DamageCharacterUseCase floors at zero`() = runTest {
        val useCase = DamageCharacterUseCase(repository)
        val char = character.copy(maxHitPoints = 20, currentHitPoints = 0)
        coEvery { repository.updateCharacter(any()) } returns Unit

        useCase(char)

        coVerify { repository.updateCharacter(match { it.currentHitPoints == 0 }) }
    }

    @Test
    fun `SetDeathSaveFailuresUseCase updates failure count`() = runTest {
        val useCase = SetDeathSaveFailuresUseCase(repository)
        val char = character.copy(currentHitPoints = 0, deathSaveFailures = 1)
        coEvery { repository.updateCharacter(any()) } returns Unit

        useCase(char, 2)

        coVerify { repository.updateCharacter(match { it.deathSaveFailures == 2 }) }
    }

    @Test
    fun `SetDeathSaveFailuresUseCase clamps value to 0-3 range`() = runTest {
        val useCase = SetDeathSaveFailuresUseCase(repository)
        val char = character.copy(currentHitPoints = 0, deathSaveFailures = 0)
        coEvery { repository.updateCharacter(any()) } returns Unit

        useCase(char, 99)

        coVerify { repository.updateCharacter(match { it.deathSaveFailures == 3 }) }
    }

    @Test
    fun `MarkCharacterDeadUseCase sets isDead, maxes failures, zeroes HP`() = runTest {
        val useCase = MarkCharacterDeadUseCase(repository)
        val char = character.copy(maxHitPoints = 20, currentHitPoints = 0, deathSaveFailures = 2)
        coEvery { repository.updateCharacter(any()) } returns Unit

        useCase(char)

        coVerify {
            repository.updateCharacter(match {
                it.isDead && it.deathSaveFailures == 3 && it.currentHitPoints == 0
            })
        }
    }
}

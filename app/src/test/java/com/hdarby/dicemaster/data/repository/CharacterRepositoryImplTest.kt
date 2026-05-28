package com.hdarby.dicemaster.data.repository

import app.cash.turbine.test
import com.hdarby.dicemaster.data.local.dao.CharacterDao
import com.hdarby.dicemaster.data.local.dao.WeaponDao
import com.hdarby.dicemaster.data.local.entity.CharacterEntity
import com.hdarby.dicemaster.data.local.entity.CharacterWeaponAssignment
import com.hdarby.dicemaster.data.local.entity.WeaponEntity
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.Proficiency
import com.hdarby.dicemaster.domain.model.Stats
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterRepositoryImplTest {

    private val characterDao: CharacterDao = mockk()
    private val weaponDao: WeaponDao = mockk()
    private val repository = CharacterRepositoryImpl(characterDao, weaponDao)

    private val characterEntity = CharacterEntity(
        id = 1,
        name = "Grog",
        race = "Goliath",
        strength = 20,
        strengthModifier = 5,
        dexterity = 12,
        dexterityModifier = 1,
        constitution = 18,
        constitutionModifier = 4,
        intelligence = 6,
        intelligenceModifier = -2,
        wisdom = 10,
        wisdomModifier = 0,
        charisma = 8,
        charismaModifier = -1
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

    @Test
    fun `getAllCharacters returns domain models`() = runTest {
        every { characterDao.getAllCharacters() } returns flowOf(listOf(characterEntity))

        repository.getAllCharacters().test {
            assertEquals(listOf(character), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getCharactersWithWeapons combines characters and weapon assignments`() = runTest {
        val weaponEntity = WeaponEntity(1, "Axe", "Axe", "1d12", "Slashing", 2)
        val assignment = CharacterWeaponAssignment(assignmentId = 10L, characterId = 1L, weapon = weaponEntity)

        every { characterDao.getAllCharacters() } returns flowOf(listOf(characterEntity))
        every { weaponDao.getAllCharacterWeapons() } returns flowOf(listOf(assignment))

        repository.getCharactersWithWeapons().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Grog", result[0].character.name)
            assertEquals(1, result[0].weapons.size)
            assertEquals("Axe", result[0].weapons[0].weapon.name)
            assertEquals(10L, result[0].weapons[0].assignmentId)
            awaitComplete()
        }
    }

    @Test
    fun `addCharacter inserts entity`() = runTest {
        coEvery { characterDao.insertCharacter(any()) } returns 1L

        val result = repository.addCharacter(character)

        assertEquals(1L, result)
        coVerify { characterDao.insertCharacter(match { it.name == "Grog" }) }
    }

    @Test
    fun `updateCharacter updates entity`() = runTest {
        coEvery { characterDao.updateCharacter(any()) } returns Unit

        repository.updateCharacter(character)

        coVerify { characterDao.updateCharacter(match { it.name == "Grog" }) }
    }

    @Test
    fun `deleteCharacter deletes entity`() = runTest {
        coEvery { characterDao.deleteCharacter(any()) } returns Unit

        repository.deleteCharacter(character)

        coVerify { characterDao.deleteCharacter(match { it.name == "Grog" }) }
    }

    @Test
    fun `assignWeaponToCharacter inserts cross-ref for nonatomic weapon`() = runTest {
        coEvery { weaponDao.isAtomicWeapon(2L) } returns false
        coEvery { weaponDao.getWeaponAssignmentCount(2L) } returns 0
        coEvery { weaponDao.insertCharacterWeaponCrossRef(any()) } returns 1L

        repository.assignWeaponToCharacter(characterId = 1L, weaponId = 2L)

        coVerify { weaponDao.insertCharacterWeaponCrossRef(match { it.characterId == 1L && it.weaponId == 2L }) }
    }

    @Test
    fun `assignWeaponToCharacter throws for atomic weapon already assigned`() = runTest {
        coEvery { weaponDao.isAtomicWeapon(2L) } returns true
        coEvery { weaponDao.getWeaponAssignmentCount(2L) } returns 1

        try {
            repository.assignWeaponToCharacter(characterId = 1L, weaponId = 2L)
            assert(false) { "Expected IllegalStateException" }
        } catch (e: IllegalStateException) {
            assert(e.message!!.isNotBlank())
        }
    }

    @Test
    fun `unassignWeaponFromCharacter deletes cross-ref by assignmentId`() = runTest {
        coEvery { weaponDao.deleteCharacterWeaponCrossRef(5L) } returns Unit

        repository.unassignWeaponFromCharacter(assignmentId = 5L)

        coVerify { weaponDao.deleteCharacterWeaponCrossRef(5L) }
    }

    // ── Proficiency mapping ───────────────────────────────────────────────────

    @Test
    fun `getAllCharacters maps proficiencies from comma-separated string to set`() = runTest {
        val entityWithProficiencies = characterEntity.copy(
            proficiencies = "ACROBATICS,STEALTH,LIGHT_ARMOR"
        )
        every { characterDao.getAllCharacters() } returns flowOf(listOf(entityWithProficiencies))

        repository.getAllCharacters().test {
            val result = awaitItem()
            val expected = setOf(Proficiency.ACROBATICS, Proficiency.STEALTH, Proficiency.LIGHT_ARMOR)
            assertEquals(expected, result[0].proficiencies)
            awaitComplete()
        }
    }

    @Test
    fun `getAllCharacters with empty proficiencies string maps to empty set`() = runTest {
        val entityNoProficiencies = characterEntity.copy(proficiencies = "")
        every { characterDao.getAllCharacters() } returns flowOf(listOf(entityNoProficiencies))

        repository.getAllCharacters().test {
            val result = awaitItem()
            assertTrue(result[0].proficiencies.isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `getAllCharacters silently ignores unknown proficiency values`() = runTest {
        val entityBadData = characterEntity.copy(proficiencies = "ACROBATICS,TOTALLY_MADE_UP,STEALTH")
        every { characterDao.getAllCharacters() } returns flowOf(listOf(entityBadData))

        repository.getAllCharacters().test {
            val result = awaitItem()
            assertEquals(setOf(Proficiency.ACROBATICS, Proficiency.STEALTH), result[0].proficiencies)
            awaitComplete()
        }
    }

    @Test
    fun `addCharacter serialises proficiencies to comma-separated string in entity`() = runTest {
        val characterWithProficiencies = character.copy(
            proficiencies = setOf(Proficiency.ARCANA, Proficiency.HISTORY)
        )
        coEvery { characterDao.insertCharacter(any()) } returns 1L

        repository.addCharacter(characterWithProficiencies)

        coVerify {
            characterDao.insertCharacter(match { entity ->
                val parts = entity.proficiencies.split(",").toSet()
                parts == setOf("ARCANA", "HISTORY")
            })
        }
    }

    @Test
    fun `updateCharacter preserves full proficiency set through round-trip`() = runTest {
        val allProficiencies = Proficiency.entries.toSet()
        val characterAllProficiencies = character.copy(proficiencies = allProficiencies)
        coEvery { characterDao.updateCharacter(any()) } returns Unit

        repository.updateCharacter(characterAllProficiencies)

        coVerify {
            characterDao.updateCharacter(match { entity ->
                val deserialised = entity.proficiencies.split(",")
                    .map { Proficiency.valueOf(it.trim()) }
                    .toSet()
                deserialised == allProficiencies
            })
        }
    }
}

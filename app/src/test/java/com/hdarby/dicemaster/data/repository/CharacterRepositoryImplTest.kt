package com.hdarby.dicemaster.data.repository

import app.cash.turbine.test
import com.hdarby.dicemaster.data.local.dao.CharacterDao
import com.hdarby.dicemaster.data.local.dao.WeaponDao
import com.hdarby.dicemaster.data.local.entity.CharacterEntity
import com.hdarby.dicemaster.data.local.entity.CharacterWeaponAssignment
import com.hdarby.dicemaster.data.local.entity.WeaponEntity
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.Stats
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
}

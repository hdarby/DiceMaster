package com.hdarby.dicemaster.data.repository

import app.cash.turbine.test
import com.hdarby.dicemaster.data.local.dao.CharacterDao
import com.hdarby.dicemaster.data.local.entity.CharacterEntity
import com.hdarby.dicemaster.data.local.entity.CharacterWeaponCrossRef
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
    private val repository = CharacterRepositoryImpl(characterDao)

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
    fun `getCharactersWithWeapons returns domain models`() = runTest {
        val weaponEntity = WeaponEntity(1, "Axe", "Axe", "1d12", "Slashing", 2)
        val charWithWeaponsEntity = com.hdarby.dicemaster.data.local.entity.CharacterWithWeapons(
            character = characterEntity,
            weapons = listOf(weaponEntity)
        )
        
        every { characterDao.getCharactersWithWeapons() } returns flowOf(listOf(charWithWeaponsEntity))

        repository.getCharactersWithWeapons().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Grog", result[0].character.name)
            assertEquals("Axe", result[0].weapons[0].name)
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
    fun `assignWeaponToCharacter inserts cross ref`() = runTest {
        coEvery { characterDao.insertCharacterWeaponCrossRef(any()) } returns Unit

        repository.assignWeaponToCharacter(1L, 2L)

        coVerify { characterDao.insertCharacterWeaponCrossRef(CharacterWeaponCrossRef(1L, 2L)) }
    }

    @Test
    fun `unassignWeaponFromCharacter deletes cross ref`() = runTest {
        coEvery { characterDao.deleteCharacterWeaponCrossRef(any()) } returns Unit

        repository.unassignWeaponFromCharacter(1L, 2L)

        coVerify { characterDao.deleteCharacterWeaponCrossRef(CharacterWeaponCrossRef(1L, 2L)) }
    }
}

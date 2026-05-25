package com.hdarby.dicemaster.data.repository

import app.cash.turbine.test
import com.hdarby.dicemaster.data.local.dao.CharacterDao
import com.hdarby.dicemaster.data.local.dao.WeaponDao
import com.hdarby.dicemaster.data.local.entity.CharacterEntity
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

class CharacterArmorClassMappingTest {

    private val characterDao: CharacterDao = mockk()
    private val weaponDao: WeaponDao = mockk(relaxed = true)
    private val repository = CharacterRepositoryImpl(characterDao, weaponDao)

    @Test
    fun `armorClass is preserved when mapping entity to domain`() = runTest {
        val entity = CharacterEntity(
            id = 1, name = "Legolas", race = "Elf",
            strength = 10, dexterity = 18, constitution = 14,
            intelligence = 12, wisdom = 14, charisma = 10,
            armorClass = 17
        )
        every { characterDao.getAllCharacters() } returns flowOf(listOf(entity))
        every { weaponDao.getAllCharacterWeapons() } returns flowOf(emptyList())

        repository.getAllCharacters().test {
            assertEquals(17, awaitItem()[0].armorClass)
            awaitComplete()
        }
    }

    @Test
    fun `armorClass defaults to 10 when not set on entity`() = runTest {
        val entity = CharacterEntity(
            id = 2, name = "Frodo", race = "Hobbit",
            strength = 8, dexterity = 14, constitution = 10,
            intelligence = 12, wisdom = 10, charisma = 10
            // armorClass not specified — defaults to 10
        )
        every { characterDao.getAllCharacters() } returns flowOf(listOf(entity))
        every { weaponDao.getAllCharacterWeapons() } returns flowOf(emptyList())

        repository.getAllCharacters().test {
            assertEquals(10, awaitItem()[0].armorClass)
            awaitComplete()
        }
    }

    @Test
    fun `armorClass is serialised correctly when adding a character`() = runTest {
        val character = Character(
            id = 0, name = "Legolas", race = "Elf",
            stats = Stats(10, 0, 18, 4, 14, 2, 12, 1, 14, 2, 10, 0),
            armorClass = 16
        )
        coEvery { characterDao.insertCharacter(any()) } returns 1L

        repository.addCharacter(character)

        coVerify { characterDao.insertCharacter(match { it.armorClass == 16 }) }
    }

    @Test
    fun `armorClass is serialised correctly when updating a character`() = runTest {
        val character = Character(
            id = 1, name = "Legolas", race = "Elf",
            stats = Stats(10, 0, 18, 4, 14, 2, 12, 1, 14, 2, 10, 0),
            armorClass = 18
        )
        coEvery { characterDao.updateCharacter(any()) } returns Unit

        repository.updateCharacter(character)

        coVerify { characterDao.updateCharacter(match { it.armorClass == 18 }) }
    }
}

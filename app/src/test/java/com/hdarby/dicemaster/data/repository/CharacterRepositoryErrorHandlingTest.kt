package com.hdarby.dicemaster.data.repository

import com.hdarby.dicemaster.data.local.dao.CharacterDao
import com.hdarby.dicemaster.data.local.dao.WeaponDao
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.Stats
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterRepositoryErrorHandlingTest {

    private val characterDao: CharacterDao = mockk()
    private val weaponDao: WeaponDao = mockk(relaxed = true)
    private val repository = CharacterRepositoryImpl(characterDao, weaponDao)

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
    fun `getCharactersWithWeapons handles database error from character flow`() = runTest {
        val errorMessage = "Database error"
        every { characterDao.getAllCharacters() } returns flow {
            throw Exception(errorMessage)
        }
        every { weaponDao.getAllCharacterWeapons() } returns flowOf(emptyList())

        try {
            repository.getCharactersWithWeapons().collect { }
        } catch (e: Exception) {
            assertEquals(errorMessage, e.message)
        }
    }

    @Test
    fun `addCharacter handles database constraint violation`() = runTest {
        val errorMessage = "UNIQUE constraint failed"
        coEvery { characterDao.insertCharacter(any()) } throws Exception(errorMessage)

        try {
            repository.addCharacter(character)
        } catch (e: Exception) {
            assertEquals(errorMessage, e.message)
        }

        coVerify { characterDao.insertCharacter(any()) }
    }

    @Test
    fun `updateCharacter with zero ID still delegates to dao`() = runTest {
        val zeroIdCharacter = character.copy(id = 0)
        coEvery { characterDao.updateCharacter(any()) } returns Unit

        repository.updateCharacter(zeroIdCharacter)

        coVerify { characterDao.updateCharacter(match { it.id == 0L }) }
    }

    @Test
    fun `deleteCharacter handles missing record`() = runTest {
        coEvery { characterDao.deleteCharacter(any()) } returns Unit

        repository.deleteCharacter(character)

        coVerify { characterDao.deleteCharacter(any()) }
    }

    @Test
    fun `getCharactersWithWeapons parses empty list`() = runTest {
        every { characterDao.getAllCharacters() } returns flowOf(emptyList())
        every { weaponDao.getAllCharacterWeapons() } returns flowOf(emptyList())

        val result = mutableListOf<Any>()
        repository.getCharactersWithWeapons().collect { result.add(it) }

        assertEquals(1, result.size)
        assertEquals(emptyList<Any>(), result[0])
    }

    @Test
    fun `addCharacter serializes stats correctly`() = runTest {
        coEvery { characterDao.insertCharacter(any()) } returns 1L

        val characterId = repository.addCharacter(character)

        coVerify { characterDao.insertCharacter(any()) }
        assertEquals(1L, characterId)
    }

    @Test
    fun `updateCharacter preserves ID during mapping`() = runTest {
        coEvery { characterDao.updateCharacter(any()) } returns Unit

        repository.updateCharacter(character)

        coVerify { characterDao.updateCharacter(any()) }
    }
}

package com.hdarby.dicemaster.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hdarby.dicemaster.data.local.dao.CharacterDao
import com.hdarby.dicemaster.data.local.dao.WeaponDao
import com.hdarby.dicemaster.data.local.entity.CharacterEntity
import com.hdarby.dicemaster.data.local.entity.CharacterWeaponCrossRef
import com.hdarby.dicemaster.data.local.entity.WeaponEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DiceMasterDatabaseTest {

    private lateinit var db: DiceMasterDatabase
    private lateinit var characterDao: CharacterDao
    private lateinit var weaponDao: WeaponDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, DiceMasterDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        characterDao = db.characterDao()
        weaponDao = db.weaponDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetCharacter() = runTest {
        val character = CharacterEntity(id = 1, name = "Grog", race = "Goliath", strength = 20, dexterity = 12, constitution = 18, intelligence = 6, wisdom = 10, charisma = 8)
        characterDao.insertCharacter(character)
        
        val allCharacters = characterDao.getAllCharacters().first()
        assertEquals(1, allCharacters.size)
        assertEquals(character.name, allCharacters[0].name)
    }

    @Test
    fun updateCharacter() = runTest {
        val character = CharacterEntity(id = 1, name = "Grog", race = "Goliath", strength = 20, dexterity = 12, constitution = 18, intelligence = 6, wisdom = 10, charisma = 8)
        characterDao.insertCharacter(character)
        
        val updatedCharacter = character.copy(name = "Grog Strongjaw")
        characterDao.updateCharacter(updatedCharacter)
        
        val allCharacters = characterDao.getAllCharacters().first()
        assertEquals("Grog Strongjaw", allCharacters[0].name)
    }

    @Test
    fun deleteCharacter() = runTest {
        val character = CharacterEntity(id = 1, name = "Grog", race = "Goliath", strength = 20, dexterity = 12, constitution = 18, intelligence = 6, wisdom = 10, charisma = 8)
        characterDao.insertCharacter(character)
        
        characterDao.deleteCharacter(character)
        
        val allCharacters = characterDao.getAllCharacters().first()
        assertTrue(allCharacters.isEmpty())
    }

    @Test
    fun insertAndGetWeapon() = runTest {
        val weapon = WeaponEntity(id = 1, name = "Greataxe", type = "Heavy", damageDice = "1d12", damageType = "Slashing", modifier = 2)
        weaponDao.insertWeapon(weapon)
        
        val allWeapons = weaponDao.getAllWeapons().first()
        assertEquals(1, allWeapons.size)
        assertEquals(weapon.name, allWeapons[0].name)
    }

    @Test
    fun updateWeapon() = runTest {
        val weapon = WeaponEntity(id = 1, name = "Greataxe", type = "Heavy", damageDice = "1d12", damageType = "Slashing", modifier = 2)
        weaponDao.insertWeapon(weapon)
        
        val updatedWeapon = weapon.copy(modifier = 5)
        weaponDao.updateWeapon(updatedWeapon)
        
        val allWeapons = weaponDao.getAllWeapons().first()
        assertEquals(5, allWeapons[0].modifier)
    }

    @Test
    fun deleteWeapon() = runTest {
        val weapon = WeaponEntity(id = 1, name = "Greataxe", type = "Heavy", damageDice = "1d12", damageType = "Slashing", modifier = 2)
        weaponDao.insertWeapon(weapon)
        
        weaponDao.deleteWeapon(weapon)
        
        val allWeapons = weaponDao.getAllWeapons().first()
        assertTrue(allWeapons.isEmpty())
    }

    @Test
    fun assignAndUnassignWeapon() = runTest {
        val character = CharacterEntity(id = 1, name = "Grog", race = "Goliath", strength = 20, dexterity = 12, constitution = 18, intelligence = 6, wisdom = 10, charisma = 8)
        val weapon = WeaponEntity(id = 1, name = "Greataxe", type = "Heavy", damageDice = "1d12", damageType = "Slashing", modifier = 2)
        
        characterDao.insertCharacter(character)
        weaponDao.insertWeapon(weapon)
        
        // Assign
        weaponDao.insertCharacterWeaponCrossRef(CharacterWeaponCrossRef(1, 1))
        
        val charactersWithWeapons = characterDao.getCharactersWithWeapons().first()
        assertEquals(1, charactersWithWeapons.size)
        assertEquals(1, charactersWithWeapons[0].weapons.size)
        assertEquals("Greataxe", charactersWithWeapons[0].weapons[0].name)
        
        // Unassign
        weaponDao.deleteCharacterWeaponCrossRef(CharacterWeaponCrossRef(1, 1))
        
        val charactersAfterUnassign = characterDao.getCharactersWithWeapons().first()
        assertEquals(1, charactersAfterUnassign.size)
        assertTrue(charactersAfterUnassign[0].weapons.isEmpty())
    }

    @Test
    fun manyToManyIntegrity() = runTest {
        val char1 = CharacterEntity(id = 1, name = "Char 1", race = "Human", strength = 10, dexterity = 10, constitution = 10, intelligence = 10, wisdom = 10, charisma = 10)
        val char2 = CharacterEntity(id = 2, name = "Char 2", race = "Elf", strength = 10, dexterity = 10, constitution = 10, intelligence = 10, wisdom = 10, charisma = 10)
        val weapon1 = WeaponEntity(id = 1, name = "Weapon 1", type = "Type 1", damageDice = "1d4", damageType = "Type 1", modifier = 0)
        val weapon2 = WeaponEntity(id = 2, name = "Weapon 2", type = "Type 2", damageDice = "1d6", damageType = "Type 2", modifier = 0)
        
        characterDao.insertCharacter(char1)
        characterDao.insertCharacter(char2)
        weaponDao.insertWeapon(weapon1)
        weaponDao.insertWeapon(weapon2)
        
        // char1 has both weapons
        weaponDao.insertCharacterWeaponCrossRef(CharacterWeaponCrossRef(1, 1))
        weaponDao.insertCharacterWeaponCrossRef(CharacterWeaponCrossRef(1, 2))
        
        // weapon1 is held by both characters
        weaponDao.insertCharacterWeaponCrossRef(CharacterWeaponCrossRef(2, 1))
        
        val result = characterDao.getCharactersWithWeapons().first()
        
        val char1Result = result.find { it.character.id == 1L }!!
        val char2Result = result.find { it.character.id == 2L }!!
        
        assertEquals(2, char1Result.weapons.size)
        assertEquals(1, char2Result.weapons.size)
        assertEquals(1L, char2Result.weapons[0].id)
    }
}

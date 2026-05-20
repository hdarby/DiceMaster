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
class DiceMasterDatabaseEdgeCasesTest {

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

    // region Empty State Queries

    @Test
    fun getAllCharacters_emptyTable_returnsEmptyList() = runTest {
        val characters = characterDao.getAllCharacters().first()
        assertTrue(characters.isEmpty())
    }

    @Test
    fun getAllWeapons_emptyTable_returnsEmptyList() = runTest {
        val weapons = weaponDao.getAllWeapons().first()
        assertTrue(weapons.isEmpty())
    }

    @Test
    fun getCharactersWithWeapons_emptyTable_returnsEmptyList() = runTest {
        val result = characterDao.getCharactersWithWeapons().first()
        assertTrue(result.isEmpty())
    }

    // endregion

    // region Auto-Generated IDs

    @Test
    fun insertCharacter_withIdZero_autoAssignsPositiveId() = runTest {
        val character = CharacterEntity(
            id = 0, name = "AutoIdChar", race = "Gnome",
            strength = 8, dexterity = 14, constitution = 10,
            intelligence = 16, wisdom = 12, charisma = 10
        )
        characterDao.insertCharacter(character)

        val all = characterDao.getAllCharacters().first()
        assertEquals(1, all.size)
        assertTrue("Expected auto-generated ID > 0, was ${all[0].id}", all[0].id > 0)
        assertEquals("AutoIdChar", all[0].name)
    }

    @Test
    fun insertWeapon_withIdZero_autoAssignsPositiveId() = runTest {
        val weapon = WeaponEntity(
            id = 0, name = "AutoIdWeapon", type = "Magic",
            damageDice = "1d10", damageType = "Force", modifier = 0
        )
        weaponDao.insertWeapon(weapon)

        val all = weaponDao.getAllWeapons().first()
        assertEquals(1, all.size)
        assertTrue("Expected auto-generated ID > 0, was ${all[0].id}", all[0].id > 0)
        assertEquals("AutoIdWeapon", all[0].name)
    }

    // endregion

    // region Multiple Record Inserts

    @Test
    fun insertMultipleCharacters_allArePersistedAndQueryable() = runTest {
        val characters = listOf(
            CharacterEntity(id = 0, name = "Alice", race = "Elf", strength = 10, dexterity = 14, constitution = 10, intelligence = 16, wisdom = 12, charisma = 10),
            CharacterEntity(id = 0, name = "Bob", race = "Human", strength = 14, dexterity = 10, constitution = 12, intelligence = 10, wisdom = 10, charisma = 12),
            CharacterEntity(id = 0, name = "Charlie", race = "Dwarf", strength = 16, dexterity = 8, constitution = 16, intelligence = 10, wisdom = 12, charisma = 8)
        )
        characters.forEach { characterDao.insertCharacter(it) }

        val all = characterDao.getAllCharacters().first()
        assertEquals(3, all.size)
        val names = all.map { it.name }.toSet()
        assertTrue(names.containsAll(listOf("Alice", "Bob", "Charlie")))
    }

    @Test
    fun insertMultipleWeapons_allArePersistedAndQueryable() = runTest {
        val weapons = listOf(
            WeaponEntity(id = 0, name = "Longsword", type = "Melee", damageDice = "1d8", damageType = "Slashing", modifier = 1),
            WeaponEntity(id = 0, name = "Shortbow", type = "Ranged", damageDice = "1d6", damageType = "Piercing", modifier = 0),
            WeaponEntity(id = 0, name = "Dagger", type = "Melee", damageDice = "1d4", damageType = "Piercing", modifier = 2)
        )
        weapons.forEach { weaponDao.insertWeapon(it) }

        val all = weaponDao.getAllWeapons().first()
        assertEquals(3, all.size)
    }

    // endregion

    // region Update Isolation

    @Test
    fun updateCharacter_onlyUpdatesTargetRecord() = runTest {
        val char1 = CharacterEntity(id = 1, name = "Original1", race = "Human", strength = 10, dexterity = 10, constitution = 10, intelligence = 10, wisdom = 10, charisma = 10)
        val char2 = CharacterEntity(id = 2, name = "Original2", race = "Elf", strength = 10, dexterity = 10, constitution = 10, intelligence = 10, wisdom = 10, charisma = 10)
        characterDao.insertCharacter(char1)
        characterDao.insertCharacter(char2)

        characterDao.updateCharacter(char1.copy(name = "Updated1"))

        val all = characterDao.getAllCharacters().first()
        assertEquals("Updated1", all.find { it.id == 1L }?.name)
        assertEquals("Original2", all.find { it.id == 2L }?.name)
    }

    @Test
    fun updateWeapon_onlyUpdatesTargetRecord() = runTest {
        val weapon1 = WeaponEntity(id = 1, name = "Original1", type = "Type1", damageDice = "1d6", damageType = "Fire", modifier = 0)
        val weapon2 = WeaponEntity(id = 2, name = "Original2", type = "Type2", damageDice = "1d8", damageType = "Ice", modifier = 0)
        weaponDao.insertWeapon(weapon1)
        weaponDao.insertWeapon(weapon2)

        weaponDao.updateWeapon(weapon1.copy(modifier = 5))

        val all = weaponDao.getAllWeapons().first()
        assertEquals(5, all.find { it.id == 1L }?.modifier)
        assertEquals(0, all.find { it.id == 2L }?.modifier)
    }

    // endregion

    // region Cascading Relation Behaviour

    @Test
    fun deleteCharacter_withAssignedWeapons_weaponStillExists() = runTest {
        val character = CharacterEntity(id = 1, name = "TempChar", race = "Any", strength = 10, dexterity = 10, constitution = 10, intelligence = 10, wisdom = 10, charisma = 10)
        val weapon = WeaponEntity(id = 1, name = "TempWeapon", type = "Any", damageDice = "1d4", damageType = "Any", modifier = 0)
        characterDao.insertCharacter(character)
        weaponDao.insertWeapon(weapon)
        weaponDao.insertCharacterWeaponCrossRef(CharacterWeaponCrossRef(1, 1))

        characterDao.deleteCharacter(character)

        val characters = characterDao.getAllCharacters().first()
        assertTrue(characters.isEmpty())
        val weapons = weaponDao.getAllWeapons().first()
        assertEquals(1, weapons.size)
        assertEquals("TempWeapon", weapons[0].name)
    }

    @Test
    fun deleteWeapon_characterWithThatWeapon_characterHasNoWeapons() = runTest {
        val character = CharacterEntity(id = 1, name = "Survivor", race = "Human", strength = 10, dexterity = 10, constitution = 10, intelligence = 10, wisdom = 10, charisma = 10)
        val weapon = WeaponEntity(id = 1, name = "DeletedWeapon", type = "Any", damageDice = "1d4", damageType = "Any", modifier = 0)
        characterDao.insertCharacter(character)
        weaponDao.insertWeapon(weapon)
        weaponDao.insertCharacterWeaponCrossRef(CharacterWeaponCrossRef(1, 1))

        weaponDao.deleteWeapon(weapon)

        val characters = characterDao.getCharactersWithWeapons().first()
        assertEquals(1, characters.size)
        assertTrue(characters[0].weapons.isEmpty())
    }

    // endregion

    // region Many-to-Many Edge Cases

    @Test
    fun sameWeapon_assignedToMultipleCharacters_eachReflectsAssignment() = runTest {
        val char1 = CharacterEntity(id = 1, name = "Char1", race = "R1", strength = 10, dexterity = 10, constitution = 10, intelligence = 10, wisdom = 10, charisma = 10)
        val char2 = CharacterEntity(id = 2, name = "Char2", race = "R2", strength = 10, dexterity = 10, constitution = 10, intelligence = 10, wisdom = 10, charisma = 10)
        val weapon = WeaponEntity(id = 1, name = "SharedSword", type = "Melee", damageDice = "1d8", damageType = "Slashing", modifier = 0)
        characterDao.insertCharacter(char1)
        characterDao.insertCharacter(char2)
        weaponDao.insertWeapon(weapon)
        weaponDao.insertCharacterWeaponCrossRef(CharacterWeaponCrossRef(1, 1))
        weaponDao.insertCharacterWeaponCrossRef(CharacterWeaponCrossRef(2, 1))

        val result = characterDao.getCharactersWithWeapons().first()
        val c1Weapons = result.find { it.character.id == 1L }!!.weapons
        val c2Weapons = result.find { it.character.id == 2L }!!.weapons
        assertEquals("SharedSword", c1Weapons[0].name)
        assertEquals("SharedSword", c2Weapons[0].name)
    }

    @Test
    fun characterWithMultipleWeapons_allWeaponsReturned() = runTest {
        val character = CharacterEntity(id = 1, name = "Archer", race = "Elf", strength = 10, dexterity = 18, constitution = 10, intelligence = 12, wisdom = 14, charisma = 10)
        val bow = WeaponEntity(id = 1, name = "Longbow", type = "Ranged", damageDice = "1d8", damageType = "Piercing", modifier = 3)
        val dagger = WeaponEntity(id = 2, name = "Dagger", type = "Melee", damageDice = "1d4", damageType = "Piercing", modifier = 1)
        characterDao.insertCharacter(character)
        weaponDao.insertWeapon(bow)
        weaponDao.insertWeapon(dagger)
        weaponDao.insertCharacterWeaponCrossRef(CharacterWeaponCrossRef(1, 1))
        weaponDao.insertCharacterWeaponCrossRef(CharacterWeaponCrossRef(1, 2))

        val result = characterDao.getCharactersWithWeapons().first()
        assertEquals(1, result.size)
        assertEquals(2, result[0].weapons.size)
        val weaponNames = result[0].weapons.map { it.name }.toSet()
        assertTrue(weaponNames.contains("Longbow"))
        assertTrue(weaponNames.contains("Dagger"))
    }

    @Test
    fun unassignOneWeapon_otherWeaponRemainsAssigned() = runTest {
        val character = CharacterEntity(id = 1, name = "Fighter", race = "Human", strength = 16, dexterity = 12, constitution = 14, intelligence = 10, wisdom = 10, charisma = 10)
        val sword = WeaponEntity(id = 1, name = "Sword", type = "Melee", damageDice = "1d8", damageType = "Slashing", modifier = 1)
        val shield = WeaponEntity(id = 2, name = "Shield", type = "Off-hand", damageDice = "1d4", damageType = "Bludgeoning", modifier = 0)
        characterDao.insertCharacter(character)
        weaponDao.insertWeapon(sword)
        weaponDao.insertWeapon(shield)
        weaponDao.insertCharacterWeaponCrossRef(CharacterWeaponCrossRef(1, 1))
        weaponDao.insertCharacterWeaponCrossRef(CharacterWeaponCrossRef(1, 2))

        weaponDao.deleteCharacterWeaponCrossRef(CharacterWeaponCrossRef(1, 2))

        val result = characterDao.getCharactersWithWeapons().first()
        assertEquals(1, result.size)
        assertEquals(1, result[0].weapons.size)
        assertEquals("Sword", result[0].weapons[0].name)
    }

    @Test
    fun characterWithNoWeapons_returnsEmptyWeaponList() = runTest {
        val character = CharacterEntity(id = 1, name = "Pacifist", race = "Halfling", strength = 8, dexterity = 14, constitution = 10, intelligence = 10, wisdom = 12, charisma = 16)
        characterDao.insertCharacter(character)

        val result = characterDao.getCharactersWithWeapons().first()
        assertEquals(1, result.size)
        assertTrue(result[0].weapons.isEmpty())
        assertEquals("Pacifist", result[0].character.name)
    }

    // endregion
}


package com.hdarby.dicemaster.data.repository

import com.hdarby.dicemaster.data.local.dao.CharacterDao
import com.hdarby.dicemaster.data.local.entity.CharacterEntity
import com.hdarby.dicemaster.data.local.entity.CharacterWeaponCrossRef
import com.hdarby.dicemaster.data.local.entity.WeaponEntity
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.CharacterWithWeapons
import com.hdarby.dicemaster.domain.model.Stats
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CharacterRepositoryImpl(private val characterDao: CharacterDao) : CharacterRepository {
    override fun getAllCharacters(): Flow<List<Character>> {
        return characterDao.getAllCharacters().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCharactersWithWeapons(): Flow<List<CharacterWithWeapons>> {
        return characterDao.getCharactersWithWeapons().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addCharacter(character: Character): Long {
        return characterDao.insertCharacter(character.toEntity())
    }

    override suspend fun updateCharacter(character: Character) {
        characterDao.updateCharacter(character.toEntity())
    }

    override suspend fun deleteCharacter(character: Character) {
        characterDao.deleteCharacter(character.toEntity())
    }

    override suspend fun assignWeaponToCharacter(characterId: Long, weaponId: Long) {
        characterDao.insertCharacterWeaponCrossRef(CharacterWeaponCrossRef(characterId, weaponId))
    }

    override suspend fun unassignWeaponFromCharacter(characterId: Long, weaponId: Long) {
        characterDao.deleteCharacterWeaponCrossRef(CharacterWeaponCrossRef(characterId, weaponId))
    }

    private fun CharacterEntity.toDomain() = Character(
        id = id,
        name = name,
        race = race,
        stats = Stats(strength, dexterity, constitution, intelligence, wisdom, charisma)
    )

    private fun Character.toEntity() = CharacterEntity(
        id = id,
        name = name,
        race = race,
        strength = stats.strength,
        dexterity = stats.dexterity,
        constitution = stats.constitution,
        intelligence = stats.intelligence,
        wisdom = stats.wisdom,
        charisma = stats.charisma
    )

    private fun WeaponEntity.toDomain() = Weapon(
        id = id,
        name = name,
        type = type,
        damageDice = damageDice,
        damageType = damageType,
        modifier = modifier
    )

    private fun com.hdarby.dicemaster.data.local.entity.CharacterWithWeapons.toDomain() = CharacterWithWeapons(
        character = character.toDomain(),
        weapons = weapons.map { it.toDomain() }
    )
}

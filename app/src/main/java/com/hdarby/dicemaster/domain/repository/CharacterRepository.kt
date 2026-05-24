package com.hdarby.dicemaster.domain.repository

import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.CharacterWithWeapons
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun getAllCharacters(): Flow<List<Character>>
    fun getCharactersWithWeapons(): Flow<List<CharacterWithWeapons>>
    suspend fun addCharacter(character: Character): Long
    suspend fun updateCharacter(character: Character)
    suspend fun deleteCharacter(character: Character)
    suspend fun assignWeaponToCharacter(characterId: Long, weaponId: Long)
    suspend fun unassignWeaponFromCharacter(assignmentId: Long)
}

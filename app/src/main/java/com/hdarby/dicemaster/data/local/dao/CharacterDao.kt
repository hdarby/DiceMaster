package com.hdarby.dicemaster.data.local.dao

import androidx.room.*
import com.hdarby.dicemaster.data.local.entity.CharacterEntity
import com.hdarby.dicemaster.data.local.entity.CharacterWeaponCrossRef
import com.hdarby.dicemaster.data.local.entity.WeaponEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters")
    fun getAllCharacters(): Flow<List<CharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity): Long

    @Update
    suspend fun updateCharacter(character: CharacterEntity)

    @Delete
    suspend fun deleteCharacter(character: CharacterEntity)

    @Transaction
    @Query("SELECT * FROM characters")
    fun getCharactersWithWeapons(): Flow<List<com.hdarby.dicemaster.data.local.entity.CharacterWithWeapons>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCharacterWeaponCrossRef(crossRef: CharacterWeaponCrossRef)

    @Delete
    suspend fun deleteCharacterWeaponCrossRef(crossRef: CharacterWeaponCrossRef)
}

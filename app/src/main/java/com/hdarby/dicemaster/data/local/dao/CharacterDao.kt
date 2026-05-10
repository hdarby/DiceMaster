package com.hdarby.dicemaster.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.hdarby.dicemaster.data.local.entity.CharacterEntity
import com.hdarby.dicemaster.data.local.entity.CharacterWeaponCrossRef
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

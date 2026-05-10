package com.hdarby.dicemaster.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hdarby.dicemaster.data.local.entity.CharacterWeaponCrossRef
import com.hdarby.dicemaster.data.local.entity.WeaponEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeaponDao {
    @Query("SELECT * FROM weapons")
    fun getAllWeapons(): Flow<List<WeaponEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeapon(weapon: WeaponEntity): Long

    @Update
    suspend fun updateWeapon(weapon: WeaponEntity)

    @Delete
    suspend fun deleteWeapon(weapon: WeaponEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCharacterWeaponCrossRef(crossRef: CharacterWeaponCrossRef)

    @Delete
    suspend fun deleteCharacterWeaponCrossRef(crossRef: CharacterWeaponCrossRef)
}

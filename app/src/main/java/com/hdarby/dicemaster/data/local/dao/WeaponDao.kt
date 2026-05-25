package com.hdarby.dicemaster.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hdarby.dicemaster.data.local.entity.CharacterWeaponAssignment
import com.hdarby.dicemaster.data.local.entity.CharacterWeaponCrossRef
import com.hdarby.dicemaster.data.local.entity.WeaponEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeaponDao {
    @Query("SELECT * FROM weapons")
    fun getAllWeapons(): Flow<List<WeaponEntity>>

    @Query("""
        SELECT cwcr.assignmentId, cwcr.characterId,
               w.id, w.name, w.type, w.damageDice, w.damageType,
               w.toHitBonus, w.damageModifier, w.isAtomic
        FROM weapons w
        INNER JOIN character_weapon_cross_ref cwcr ON w.id = cwcr.weaponId
    """)
    fun getAllCharacterWeapons(): Flow<List<CharacterWeaponAssignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeapon(weapon: WeaponEntity): Long

    @Update
    suspend fun updateWeapon(weapon: WeaponEntity)

    @Delete
    suspend fun deleteWeapon(weapon: WeaponEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCharacterWeaponCrossRef(crossRef: CharacterWeaponCrossRef): Long

    @Query("DELETE FROM character_weapon_cross_ref WHERE assignmentId = :assignmentId")
    suspend fun deleteCharacterWeaponCrossRef(assignmentId: Long)

    @Query("SELECT COUNT(*) FROM character_weapon_cross_ref WHERE weaponId = :weaponId")
    suspend fun getWeaponAssignmentCount(weaponId: Long): Int

    @Query("SELECT isAtomic FROM weapons WHERE id = :weaponId")
    suspend fun isAtomicWeapon(weaponId: Long): Boolean
}

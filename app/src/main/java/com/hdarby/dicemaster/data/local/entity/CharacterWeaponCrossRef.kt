package com.hdarby.dicemaster.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cross-reference table linking characters to their equipped weapons.
 *
 * For atomic weapons ([WeaponEntity.isAtomic] == true) the business layer enforces that a given
 * [weaponId] appears at most once across all rows.  For non-atomic weapons multiple characters
 * (or the same character) may hold additional copies.
 */
@Entity(
    tableName = "character_weapon_cross_ref",
    indices = [Index(value = ["characterId", "weaponId"])]
)
data class CharacterWeaponCrossRef(
    @PrimaryKey(autoGenerate = true) val assignmentId: Long = 0,
    val characterId: Long,
    val weaponId: Long
)


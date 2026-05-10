package com.hdarby.dicemaster.data.local.entity

import androidx.room.Entity

@Entity(primaryKeys = ["characterId", "weaponId"], tableName = "character_weapon_cross_ref")
data class CharacterWeaponCrossRef(
    val characterId: Long,
    val weaponId: Long
)

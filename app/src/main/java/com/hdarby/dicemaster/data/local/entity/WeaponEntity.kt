package com.hdarby.dicemaster.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weapons")
data class WeaponEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String = "SIMPLE_MELEE",
    val damageDice: String = "D6",
    val damageType: String = "SLASHING",
    val toHitBonus: Int = 0,
    val damageModifier: Int = 0,
    val isAtomic: Boolean = true
)

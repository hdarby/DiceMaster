package com.hdarby.dicemaster.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weapons")
data class WeaponEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val damageDice: String,
    val damageType: String,
    val modifier: Int
)

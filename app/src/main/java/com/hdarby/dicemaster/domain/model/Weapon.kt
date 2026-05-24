package com.hdarby.dicemaster.domain.model

data class Weapon(
    val id: Long = 0,
    val name: String,
    val type: String,
    val damageDice: String,
    val damageType: String,
    val modifier: Int,
    val isAtomic: Boolean = true
)

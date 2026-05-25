package com.hdarby.dicemaster.domain.model

data class Weapon(
    val id: Long = 0,
    val name: String,
    val weaponType: WeaponType = WeaponType.SIMPLE_MELEE,
    val damageDice: DamageDice = DamageDice.D6,
    val damageType: DamageType = DamageType.SLASHING,
    val toHitBonus: Int = 0,
    val damageModifier: Int = 0,
    val isAtomic: Boolean = true
)

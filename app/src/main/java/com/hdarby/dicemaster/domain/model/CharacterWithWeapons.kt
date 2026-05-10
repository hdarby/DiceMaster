package com.hdarby.dicemaster.domain.model

data class CharacterWithWeapons(
    val character: Character,
    val weapons: List<Weapon>
)

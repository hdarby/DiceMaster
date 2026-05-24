package com.hdarby.dicemaster.domain.model

/** Represents a single weapon assignment on a character, carrying the cross-ref assignmentId for targeted unassignment. */
data class CharacterWeaponEntry(
    val assignmentId: Long,
    val weapon: Weapon
)


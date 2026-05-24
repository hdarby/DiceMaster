package com.hdarby.dicemaster.data.local.entity

import androidx.room.Embedded

/** Room query result: weapon data plus the assignmentId and characterId from the cross-ref table. */
data class CharacterWeaponAssignment(
    val assignmentId: Long,
    val characterId: Long,
    @Embedded val weapon: WeaponEntity
)


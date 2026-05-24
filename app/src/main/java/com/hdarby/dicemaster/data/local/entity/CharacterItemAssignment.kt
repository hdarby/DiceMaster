package com.hdarby.dicemaster.data.local.entity

import androidx.room.Embedded

/** Room query result: item data plus the assignmentId, characterId, and quantity from the cross-ref table. */
data class CharacterItemAssignment(
    val assignmentId: Long,
    val characterId: Long,
    @Embedded val item: ItemEntity,
    val quantity: Int
)

package com.hdarby.dicemaster.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cross-reference table linking characters to their item assignments.
 *
 * Each row represents an independent assignment — a character may hold multiple
 * independent copies of the same item, each with its own [assignmentId] and [quantity].
 */
@Entity(
    tableName = "character_item_cross_ref",
    indices = [Index(value = ["characterId", "itemId"])]
)
data class CharacterItemCrossRef(
    @PrimaryKey(autoGenerate = true) val assignmentId: Long = 0,
    val characterId: Long,
    val itemId: Long,
    val quantity: Int = 1
)

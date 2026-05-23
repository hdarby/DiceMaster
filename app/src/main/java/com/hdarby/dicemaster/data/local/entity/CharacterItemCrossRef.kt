package com.hdarby.dicemaster.data.local.entity

import androidx.room.Entity

@Entity(primaryKeys = ["characterId", "itemId"], tableName = "character_item_cross_ref")
data class CharacterItemCrossRef(
    val characterId: Long,
    val itemId: Long,
    val quantity: Int = 1
)


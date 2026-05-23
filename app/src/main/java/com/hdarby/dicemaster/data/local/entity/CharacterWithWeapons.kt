package com.hdarby.dicemaster.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CharacterWithWeapons(
    @Embedded val character: CharacterEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "characterId"
    )
    val weapons: List<WeaponEntity>
)

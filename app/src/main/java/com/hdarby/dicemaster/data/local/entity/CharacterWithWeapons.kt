package com.hdarby.dicemaster.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CharacterWithWeapons(
    @Embedded val character: CharacterEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = CharacterWeaponCrossRef::class,
            parentColumn = "characterId",
            entityColumn = "weaponId"
        )
    )
    val weapons: List<WeaponEntity>
)

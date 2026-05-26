package com.hdarby.dicemaster.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Room relation result: a character together with all weapons currently assigned to it,
 * resolved through the [CharacterWeaponCrossRef] junction table.
 */
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


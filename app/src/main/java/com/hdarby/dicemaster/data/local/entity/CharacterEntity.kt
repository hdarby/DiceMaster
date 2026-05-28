package com.hdarby.dicemaster.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val race: String,
    val strength: Int,
    val strengthModifier: Int = 0,
    val dexterity: Int,
    val dexterityModifier: Int = 0,
    val constitution: Int,
    val constitutionModifier: Int = 0,
    val intelligence: Int,
    val intelligenceModifier: Int = 0,
    val wisdom: Int,
    val wisdomModifier: Int = 0,
    val charisma: Int,
     val charismaModifier: Int = 0,
    val armorClass: Int = 10,
    val maxHitPoints: Int = 10,
    val currentHitPoints: Int = 10,
    val deathSaveFailures: Int = 0,
    val isDead: Boolean = false,
    val characterClass: String? = null,
    val level: Int = 1,
    val proficiencies: String = ""
)

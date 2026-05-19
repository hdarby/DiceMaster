package com.hdarby.dicemaster.domain.model

data class Character(
    val id: Long = 0,
    val name: String,
    val race: String,
    val stats: Stats
)

data class Stats(
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
    val charismaModifier: Int = 0
)

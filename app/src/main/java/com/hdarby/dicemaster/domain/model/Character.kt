package com.hdarby.dicemaster.domain.model

data class Character(
    val id: Long = 0,
    val name: String,
    val race: String,
    val stats: Stats
)

data class Stats(
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int
)

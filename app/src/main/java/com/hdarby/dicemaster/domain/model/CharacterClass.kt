package com.hdarby.dicemaster.domain.model

enum class CharacterClass(val displayName: String, val hitDie: Int) {
    ARTIFICER("Artificer", 8),
    BARBARIAN("Barbarian", 12),
    BARD("Bard", 8),
    CLERIC("Cleric", 8),
    DRUID("Druid", 8),
    FIGHTER("Fighter", 10),
    MONK("Monk", 8),
    PALADIN("Paladin", 10),
    RANGER("Ranger", 10),
    ROGUE("Rogue", 8),
    SORCERER("Sorcerer", 6),
    WARLOCK("Warlock", 8),
    WIZARD("Wizard", 6);

    val hitDieLabel: String get() = "d$hitDie"
}


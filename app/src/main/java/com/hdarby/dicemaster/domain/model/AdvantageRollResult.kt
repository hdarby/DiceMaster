package com.hdarby.dicemaster.domain.model

data class AdvantageRollResult(
    val roll1: Int,
    val roll2: Int,
    val mode: AdvantageMode
) {
    val selectedRoll: Int = if (mode == AdvantageMode.Advantage) maxOf(roll1, roll2) else minOf(roll1, roll2)
}



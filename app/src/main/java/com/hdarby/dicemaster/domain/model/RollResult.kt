package com.hdarby.dicemaster.domain.model

data class RollResult(
    val rolls: List<Int>,
    val modifier: Int = 0,
    val total: Int = rolls.sum() + modifier
)

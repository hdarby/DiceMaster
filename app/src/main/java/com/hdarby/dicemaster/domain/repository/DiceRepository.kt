package com.hdarby.dicemaster.domain.repository

interface DiceRepository {
    fun rollDice(faces: Int, quantity: Int): List<Int>
}

package com.hdarby.dicemaster.data

import kotlin.random.Random

interface DiceRepository {
    fun rollDice(faces: Int, quantity: Int): List<Int>
}

class DiceRepositoryImpl : DiceRepository {
    override fun rollDice(faces: Int, quantity: Int): List<Int> {
        return List(quantity) { Random.nextInt(1, faces + 1) }.sortedDescending()
    }
}

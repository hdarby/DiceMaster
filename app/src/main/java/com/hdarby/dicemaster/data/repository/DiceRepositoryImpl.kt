package com.hdarby.dicemaster.data.repository

import com.hdarby.dicemaster.domain.repository.DiceRepository
import kotlin.random.Random

class DiceRepositoryImpl : DiceRepository {
    override fun rollDice(faces: Int, quantity: Int): List<Int> {
        return List(quantity) { Random.nextInt(1, faces + 1) }
    }
}

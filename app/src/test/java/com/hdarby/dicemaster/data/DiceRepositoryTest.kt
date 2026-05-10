package com.hdarby.dicemaster.data.repository

import com.hdarby.dicemaster.domain.repository.DiceRepository
import com.hdarby.dicemaster.data.repository.DiceRepositoryImpl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DiceRepositoryTest {

    private lateinit var repository: DiceRepository

    @Before
    fun setup() {
        repository = DiceRepositoryImpl()
    }

    @Test
    fun `rollDice generates correct quantity of results`() {
        val faces = 6
        val quantity = 5
        val results = repository.rollDice(faces, quantity)
        assertEquals(quantity, results.size)
    }

    @Test
    fun `rollDice results are within correct range`() {
        val faces = 20
        val quantity = 100
        val results = repository.rollDice(faces, quantity)
        results.forEach { result ->
            assertTrue("Result $result should be between 1 and $faces", result in 1..faces)
        }
    }
}

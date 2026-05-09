package com.hdarby.dicemaster.data

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

    @Test
    fun `rollDice results are sorted descending`() {
        val faces = 100
        val quantity = 10
        val results = repository.rollDice(faces, quantity)
        for (i in 0 until results.size - 1) {
            assertTrue("Results should be sorted descending: ${results[i]} >= ${results[i+1]}", results[i] >= results[i + 1])
        }
    }
}

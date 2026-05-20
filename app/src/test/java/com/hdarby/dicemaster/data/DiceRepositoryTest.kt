package com.hdarby.dicemaster.data.repository

import com.hdarby.dicemaster.domain.repository.DiceRepository
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
        val results = repository.rollDice(faces = 6, quantity = 5)
        assertEquals(5, results.size)
    }

    @Test
    fun `rollDice results are within correct range for d20`() {
        val results = repository.rollDice(faces = 20, quantity = 100)
        results.forEach { result ->
            assertTrue("Result $result out of range for d20", result in 1..20)
        }
    }

    @Test
    fun `rollDice d3 results are in range 1 to 3`() {
        val results = repository.rollDice(faces = 3, quantity = 200)
        results.forEach { result ->
            assertTrue("Result $result out of range for d3", result in 1..3)
        }
    }

    @Test
    fun `rollDice d4 results are in range 1 to 4`() {
        val results = repository.rollDice(faces = 4, quantity = 200)
        results.forEach { result ->
            assertTrue("Result $result out of range for d4", result in 1..4)
        }
    }

    @Test
    fun `rollDice d8 results are in range 1 to 8`() {
        val results = repository.rollDice(faces = 8, quantity = 200)
        results.forEach { result ->
            assertTrue("Result $result out of range for d8", result in 1..8)
        }
    }

    @Test
    fun `rollDice d10 results are in range 1 to 10`() {
        val results = repository.rollDice(faces = 10, quantity = 200)
        results.forEach { result ->
            assertTrue("Result $result out of range for d10", result in 1..10)
        }
    }

    @Test
    fun `rollDice d12 results are in range 1 to 12`() {
        val results = repository.rollDice(faces = 12, quantity = 200)
        results.forEach { result ->
            assertTrue("Result $result out of range for d12", result in 1..12)
        }
    }

    @Test
    fun `rollDice d100 results are in range 1 to 100`() {
        val results = repository.rollDice(faces = 100, quantity = 200)
        results.forEach { result ->
            assertTrue("Result $result out of range for d100", result in 1..100)
        }
    }

    @Test
    fun `rollDice single die returns one result`() {
        val results = repository.rollDice(faces = 20, quantity = 1)
        assertEquals(1, results.size)
        assertTrue("Single d20 result out of range", results[0] in 1..20)
    }

    @Test
    fun `rollDice maximum quantity returns correct count`() {
        val results = repository.rollDice(faces = 6, quantity = 10)
        assertEquals(10, results.size)
    }

    @Test
    fun `rollDice results always include at least value 1 across large sample`() {
        // With 1000 d6 rolls, all faces should appear at least once
        val results = repository.rollDice(faces = 6, quantity = 1000)
        val uniqueValues = results.toSet()
        assertTrue("Expected all faces to appear in 1000 d6 rolls", uniqueValues.containsAll((1..6).toSet()))
    }
}

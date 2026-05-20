package com.hdarby.dicemaster.domain.usecase

import com.hdarby.dicemaster.domain.repository.DiceRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RollDiceUseCaseTest {

    private val repository: DiceRepository = mockk()
    private val useCase = RollDiceUseCase(repository)

    @Test
    fun `invoke should return sorted results and total from repository`() {
        val unsortedResults = listOf(5, 20, 10)
        val expectedSortedResults = listOf(20, 10, 5)
        every { repository.rollDice(20, 3) } returns unsortedResults

        val result = useCase(20, 3)

        assertEquals(expectedSortedResults, result.rolls)
        assertEquals(35, result.total)
        verify { repository.rollDice(20, 3) }
    }

    @Test
    fun `invoke should apply positive modifier to total`() {
        val rolls = listOf(5, 20, 10)
        every { repository.rollDice(20, 3) } returns rolls

        val result = useCase(20, 3, modifier = 5)

        assertEquals(40, result.total)
        assertEquals(5, result.modifier)
    }

    @Test
    fun `invoke should apply negative modifier to total`() {
        val rolls = listOf(10, 8)
        every { repository.rollDice(6, 2) } returns rolls

        val result = useCase(6, 2, modifier = -3)

        assertEquals(15, result.total)
        assertEquals(-3, result.modifier)
    }

    @Test
    fun `invoke with zero modifier does not change total`() {
        val rolls = listOf(7)
        every { repository.rollDice(10, 1) } returns rolls

        val result = useCase(10, 1, modifier = 0)

        assertEquals(7, result.total)
        assertEquals(0, result.modifier)
    }

    @Test
    fun `invoke should handle single result`() {
        val rolls = listOf(7)
        every { repository.rollDice(10, 1) } returns rolls

        val result = useCase(10, 1)

        assertEquals(rolls, result.rolls)
        assertEquals(7, result.total)
        verify { repository.rollDice(10, 1) }
    }

    @Test
    fun `invoke sorts results in descending order`() {
        val unsorted = listOf(2, 5, 1, 4, 3)
        every { repository.rollDice(6, 5) } returns unsorted

        val result = useCase(6, 5)

        assertEquals(listOf(5, 4, 3, 2, 1), result.rolls)
    }

    @Test
    fun `invoke preserves duplicate values in sorted order`() {
        val rolls = listOf(3, 3, 5, 5, 1)
        every { repository.rollDice(6, 5) } returns rolls

        val result = useCase(6, 5)

        assertTrue(result.rolls[0] >= result.rolls[1])
        assertTrue(result.rolls[1] >= result.rolls[2])
        assertTrue(result.rolls[2] >= result.rolls[3])
        assertTrue(result.rolls[3] >= result.rolls[4])
    }

    @Test
    fun `invoke with modifier negative enough to produce negative total`() {
        val rolls = listOf(1)
        every { repository.rollDice(4, 1) } returns rolls

        val result = useCase(4, 1, modifier = -5)

        assertEquals(-4, result.total)
    }

    @Test
    fun `total equals sum of rolls plus modifier`() {
        val rolls = listOf(6, 4, 3)
        every { repository.rollDice(6, 3) } returns rolls

        val result = useCase(6, 3, modifier = 2)

        assertEquals(rolls.sum() + 2, result.total)
    }
}

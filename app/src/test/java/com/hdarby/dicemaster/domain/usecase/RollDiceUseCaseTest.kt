package com.hdarby.dicemaster.domain.usecase

import com.hdarby.dicemaster.data.DiceRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class RollDiceUseCaseTest {

    private val repository: DiceRepository = mockk()
    private val useCase = RollDiceUseCase(repository)

    @Test
    fun `invoke should return sorted results from repository`() {
        // Given
        val unsortedResults = listOf(5, 20, 10)
        val expectedSortedResults = listOf(20, 10, 5)
        every { repository.rollDice(20, 3) } returns unsortedResults

        // When
        val result = useCase(20, 3)

        // Then
        assertEquals(expectedSortedResults, result)
        verify { repository.rollDice(20, 3) }
    }

    @Test
    fun `invoke should handle single result`() {
        // Given
        val unsortedResults = listOf(7)
        every { repository.rollDice(10, 1) } returns unsortedResults

        // When
        val result = useCase(10, 1)

        // Then
        assertEquals(unsortedResults, result)
        verify { repository.rollDice(10, 1) }
    }
}

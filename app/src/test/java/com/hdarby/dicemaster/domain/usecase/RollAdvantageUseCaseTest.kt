package com.hdarby.dicemaster.domain.usecase

import com.hdarby.dicemaster.domain.model.AdvantageMode
import com.hdarby.dicemaster.domain.repository.DiceRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class RollAdvantageUseCaseTest {

    private val repository: DiceRepository = mockk()
    private val useCase = RollAdvantageUseCase(repository)

    @Test
    fun `invoke with Advantage selects the higher of the two rolls`() {
        every { repository.rollDice(20, 2) } returns listOf(15, 7)

        val result = useCase(AdvantageMode.Advantage)

        assertEquals(15, result.roll1)
        assertEquals(7, result.roll2)
        assertEquals(15, result.selectedRoll)
        assertEquals(AdvantageMode.Advantage, result.mode)
        verify { repository.rollDice(20, 2) }
    }

    @Test
    fun `invoke with Disadvantage selects the lower of the two rolls`() {
        every { repository.rollDice(20, 2) } returns listOf(15, 7)

        val result = useCase(AdvantageMode.Disadvantage)

        assertEquals(7, result.selectedRoll)
        assertEquals(AdvantageMode.Disadvantage, result.mode)
    }

    @Test
    fun `invoke with equal rolls returns the same value for both Advantage and Disadvantage`() {
        every { repository.rollDice(20, 2) } returns listOf(10, 10)

        val advantageResult = useCase(AdvantageMode.Advantage)
        val disadvantageResult = useCase(AdvantageMode.Disadvantage)

        assertEquals(10, advantageResult.selectedRoll)
        assertEquals(10, disadvantageResult.selectedRoll)
    }

    @Test
    fun `invoke passes D20_FACES and two dice count to repository`() {
        every { repository.rollDice(20, 2) } returns listOf(1, 20)

        useCase(AdvantageMode.Advantage)

        verify(exactly = 1) { repository.rollDice(20, 2) }
    }
}


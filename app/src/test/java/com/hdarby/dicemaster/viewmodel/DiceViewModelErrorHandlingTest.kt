package com.hdarby.dicemaster.viewmodel

import app.cash.turbine.test
import com.hdarby.dicemaster.domain.model.RollResult
import com.hdarby.dicemaster.domain.usecase.RollDiceUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiceViewModelErrorHandlingTest {

    private val rollDiceUseCase: RollDiceUseCase = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `rollDice handles exception gracefully`() = runTest {
        val errorMessage = "Random number generation failed"
        coEvery { rollDiceUseCase(any(), any(), any()) } throws Exception(errorMessage)

        val viewModel = DiceViewModel(rollDiceUseCase)
        viewModel.updateQuantity(3)
        viewModel.updateFaces(6)

        viewModel.rollDice()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(errorMessage, state.error)
        }
    }

    @Test
    fun `updateModifier with negative value`() = runTest {
        coEvery { rollDiceUseCase(any(), any(), any()) } returns RollResult(
            rolls = listOf(5, 4, 3),
            modifier = -5
        )

        val viewModel = DiceViewModel(rollDiceUseCase)

        viewModel.updateQuantity(3)
        viewModel.updateFaces(6)
        viewModel.updateModifier(-5)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(-5, state.modifier)
            assertEquals(null, state.error)
        }
    }

    @Test
    fun `updateModifier persists zero value`() = runTest {
        val viewModel = DiceViewModel(rollDiceUseCase)

        viewModel.updateModifier(0)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.modifier)
        }
    }

    @Test
    fun `rollDice with different configurations`() = runTest {
        val roll1 = RollResult(rolls = listOf(20), modifier = 0)
        val roll2 = RollResult(rolls = listOf(1, 1), modifier = 2)

        coEvery { rollDiceUseCase(20, 1, 0) } returns roll1
        coEvery { rollDiceUseCase(4, 2, 2) } returns roll2

        val viewModel = DiceViewModel(rollDiceUseCase)

        viewModel.updateFaces(20)
        viewModel.updateQuantity(1)
        viewModel.updateModifier(0)
        viewModel.rollDice()

        viewModel.updateFaces(4)
        viewModel.updateQuantity(2)
        viewModel.updateModifier(2)
        viewModel.rollDice()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(roll2, state.rollResult)
        }
    }

    @Test
    fun `dismissResults hides results`() = runTest {
        coEvery { rollDiceUseCase(any(), any(), any()) } returns RollResult(
            rolls = listOf(5),
            modifier = 0
        )

        val viewModel = DiceViewModel(rollDiceUseCase)

        viewModel.rollDice()
        viewModel.dismissResults()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.showResults)
        }
    }
}


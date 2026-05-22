package com.hdarby.dicemaster.viewmodel

import app.cash.turbine.test
import com.hdarby.dicemaster.domain.model.RollResult
import com.hdarby.dicemaster.domain.usecase.RollAdvantageUseCase
import com.hdarby.dicemaster.domain.usecase.RollDiceUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiceViewModelTest {

    private val rollDiceUseCase: RollDiceUseCase = mockk()
    private val rollAdvantageUseCase: RollAdvantageUseCase = mockk(relaxed = true)
    private lateinit var viewModel: DiceViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DiceViewModel(rollDiceUseCase, rollAdvantageUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(20, state.faces)
            assertEquals(1, state.quantity)
            assertEquals(0, state.modifier)
            assertNull(state.rollResult)
            assertFalse(state.showResults)
        }
    }

    @Test
    fun `updateFaces updates state`() = runTest {
        viewModel.uiState.test {
            assertEquals(20, awaitItem().faces)
            viewModel.updateFaces(10)
            assertEquals(10, awaitItem().faces)
        }
    }

    @Test
    fun `updateQuantity updates state`() = runTest {
        viewModel.uiState.test {
            assertEquals(1, awaitItem().quantity)
            viewModel.updateQuantity(5)
            assertEquals(5, awaitItem().quantity)
        }
    }

    @Test
    fun `updateModifier updates state`() = runTest {
        viewModel.uiState.test {
            assertEquals(0, awaitItem().modifier)
            viewModel.updateModifier(3)
            assertEquals(3, awaitItem().modifier)
        }
    }

    @Test
    fun `rollDice updates results and shows results`() = runTest {
        val expectedResult = RollResult(rolls = listOf(20, 15, 10), modifier = 0)
        every { rollDiceUseCase(20, 3, 0) } returns expectedResult

        viewModel.uiState.test {
            awaitItem() // Initial state (1 qty)
            
            viewModel.updateQuantity(3)
            assertEquals(3, awaitItem().quantity)
            
            viewModel.rollDice()
            
            val state = awaitItem()
            assertEquals(expectedResult, state.rollResult)
            assertTrue(state.showResults)
            
            verify { rollDiceUseCase(20, 3, 0) }
        }
    }

    @Test
    fun `rollDice with modifier updates results`() = runTest {
        val expectedResult = RollResult(rolls = listOf(15), modifier = 5)
        every { rollDiceUseCase(20, 1, 5) } returns expectedResult

        viewModel.uiState.test {
            awaitItem()
            
            viewModel.updateModifier(5)
            awaitItem()
            
            viewModel.rollDice()
            
            val state = awaitItem()
            assertEquals(expectedResult, state.rollResult)
            assertEquals(20, state.rollResult?.total)
            
            verify { rollDiceUseCase(20, 1, 5) }
        }
    }

    @Test
    fun `dismissResults updates showResults to false`() = runTest {
        val expectedResult = RollResult(rolls = listOf(10))
        every { rollDiceUseCase(any(), any(), any()) } returns expectedResult
        
        viewModel.uiState.test {
            awaitItem() // Initial
            
            viewModel.rollDice()
            assertTrue(awaitItem().showResults)
            
            viewModel.dismissResults()
            assertFalse(awaitItem().showResults)
        }
    }

    @Test
    fun `rollDice updates error on failure`() = runTest {
        val errorMessage = "Roll failed"
        every { rollDiceUseCase(any(), any(), any()) } throws Exception(errorMessage)
        
        viewModel.uiState.test {
            awaitItem() // Initial
            
            viewModel.rollDice()
            
            assertEquals(errorMessage, awaitItem().error)
        }
    }
}

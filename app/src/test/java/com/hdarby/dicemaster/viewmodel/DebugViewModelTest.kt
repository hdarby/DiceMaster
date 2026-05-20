package com.hdarby.dicemaster.viewmodel

import app.cash.turbine.test
import kotlin.math.sqrt
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DebugViewModelTest {

    private lateinit var viewModel: DebugViewModel

    @Before
    fun setUp() {
        viewModel = DebugViewModel()
    }

    @Test
    fun `initial state contains stats for all expected die types`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            val faces = state.dieStatsList.map { it.faces }
            assertEquals(listOf(3, 4, 6, 8, 10, 12, 20, 100), faces)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `each die stats has totalRolls equal to faces times 1000`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            state.dieStatsList.forEach { stats ->
                assertEquals(
                    "Expected totalRolls = ${stats.faces * 1000} for d${stats.faces}",
                    stats.faces * 1000,
                    stats.totalRolls
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `each die stats has frequencies only within valid range`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            state.dieStatsList.forEach { stats ->
                stats.frequencies.keys.forEach { face ->
                    assertTrue(
                        "Face value $face out of range for d${stats.faces}",
                        face in 1..stats.faces
                    )
                }
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `each die stats min and max are within valid range`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            state.dieStatsList.forEach { stats ->
                assertTrue("Min ${stats.min} < 1 for d${stats.faces}", stats.min >= 1)
                assertTrue("Max ${stats.max} > ${stats.faces} for d${stats.faces}", stats.max <= stats.faces)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `each die stats mean is within expected range`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            state.dieStatsList.forEach { stats ->
                val expectedMidpoint = (stats.faces + 1) / 2.0
                val tolerance = stats.faces * 0.1
                assertTrue(
                    "Mean ${stats.mean} too far from expected midpoint $expectedMidpoint for d${stats.faces}",
                    stats.mean in (expectedMidpoint - tolerance)..(expectedMidpoint + tolerance)
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `runSimulations refreshes the stats list`() = runTest {
        viewModel.uiState.test {
            val firstState = awaitItem()
            viewModel.runSimulations()
            val secondState = awaitItem()
            assertEquals(firstState.dieStatsList.size, secondState.dieStatsList.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `each die stats has positive stdDev`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            state.dieStatsList.forEach { stats ->
                assertTrue(
                    "Expected stdDev > 0 for d${stats.faces}, was ${stats.stdDev}",
                    stats.stdDev > 0.0
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `each die stats stdDev is within expected statistical range`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            state.dieStatsList.forEach { stats ->
                // Theoretical std dev of a uniform distribution: sqrt((n^2 - 1) / 12)
                val theoreticalStdDev = sqrt((stats.faces.toLong() * stats.faces - 1) / 12.0)
                val tolerance = theoreticalStdDev * 0.15 // allow 15% tolerance
                assertTrue(
                    "StdDev ${stats.stdDev} too far from theoretical $theoreticalStdDev for d${stats.faces}",
                    stats.stdDev in (theoreticalStdDev - tolerance)..(theoreticalStdDev + tolerance)
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `each die stats has non-null frequencies map`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            state.dieStatsList.forEach { stats ->
                assertNotNull("Frequencies should not be null for d${stats.faces}", stats.frequencies)
                assertTrue("Frequencies should not be empty for d${stats.faces}", stats.frequencies.isNotEmpty())
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `each die stats total frequencies sum equals totalRolls`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            state.dieStatsList.forEach { stats ->
                val frequencySum = stats.frequencies.values.sum()
                assertEquals(
                    "Frequency sum should equal totalRolls for d${stats.faces}",
                    stats.totalRolls,
                    frequencySum
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }
}


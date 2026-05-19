package com.hdarby.dicemaster.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

data class DieStats(
    val faces: Int,
    val frequencies: Map<Int, Int>,
    val mean: Double,
    val stdDev: Double,
    val min: Int,
    val max: Int,
    val totalRolls: Int
)

data class DebugUiState(
    val dieStatsList: List<DieStats> = emptyList()
)

class DebugViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DebugUiState())
    val uiState: StateFlow<DebugUiState> = _uiState.asStateFlow()

    init {
        runSimulations()
    }

    fun runSimulations() {
        val dieTypes = listOf(3, 4, 6, 8, 10, 12, 20, 100)

        val statsList = dieTypes.map { faces ->
            val numRolls = faces * 1000
            val rolls = List(numRolls) { Random.nextInt(1, faces + 1) }
            val frequencies = rolls.groupingBy { it }.eachCount()
            val mean = rolls.average()
            val variance = rolls.map { (it - mean).pow(2) }.average()
            val stdDev = sqrt(variance)

            DieStats(
                faces = faces,
                frequencies = frequencies,
                mean = mean,
                stdDev = stdDev,
                min = rolls.minOrNull() ?: 0,
                max = rolls.maxOrNull() ?: 0,
                totalRolls = numRolls
            )
        }

        _uiState.update { it.copy(dieStatsList = statsList) }
    }
}

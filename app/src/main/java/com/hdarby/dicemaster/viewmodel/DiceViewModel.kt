package com.hdarby.dicemaster.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdarby.dicemaster.domain.model.RollResult
import com.hdarby.dicemaster.domain.usecase.RollDiceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiceUiState(
    val faces: Int = 20,
    val quantity: Int = 1,
    val modifier: Int = 0,
    val rollResult: RollResult? = null,
    val showResults: Boolean = false,
    val error: String? = null
)

class DiceViewModel(private val rollDiceUseCase: RollDiceUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(DiceUiState())
    val uiState: StateFlow<DiceUiState> = _uiState.asStateFlow()

    fun updateFaces(faces: Int) {
        _uiState.update { it.copy(faces = faces) }
    }

    fun updateQuantity(quantity: Int) {
        _uiState.update { it.copy(quantity = quantity) }
    }

    fun updateModifier(modifier: Int) {
        _uiState.update { it.copy(modifier = modifier) }
    }

    fun rollDice() {
        viewModelScope.launch {
            try {
                val result = rollDiceUseCase(
                    faces = _uiState.value.faces,
                    quantity = _uiState.value.quantity,
                    modifier = _uiState.value.modifier
                )
                _uiState.update {
                    it.copy(
                        rollResult = result,
                        showResults = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun dismissResults() {
        _uiState.update { it.copy(showResults = false) }
    }
}

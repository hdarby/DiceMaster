package com.hdarby.dicemaster.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdarby.dicemaster.domain.usecase.RollDiceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiceUiState(
    val faces: Int = 20,
    val quantity: Int = 1,
    val rollResults: List<Int> = emptyList(),
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

    fun rollDice() {
        viewModelScope.launch {
            try {
                val results = rollDiceUseCase(_uiState.value.faces, _uiState.value.quantity)
                _uiState.update {
                    it.copy(
                        rollResults = results,
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

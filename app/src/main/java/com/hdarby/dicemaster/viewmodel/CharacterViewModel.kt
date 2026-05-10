package com.hdarby.dicemaster.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.usecase.character.*
import com.hdarby.dicemaster.viewmodel.state.CharacterUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CharacterViewModel(
    private val getCharactersWithWeaponsUseCase: GetCharactersWithWeaponsUseCase,
    private val addCharacterUseCase: AddCharacterUseCase,
    private val updateCharacterUseCase: UpdateCharacterUseCase,
    private val deleteCharacterUseCase: DeleteCharacterUseCase,
    private val unassignWeaponFromCharacterUseCase: UnassignWeaponFromCharacterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CharacterUiState())
    val uiState: StateFlow<CharacterUiState> = _uiState.asStateFlow()

    init {
        loadCharacters()
    }

    private fun loadCharacters() {
        getCharactersWithWeaponsUseCase()
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { characters ->
                _uiState.update { it.copy(characters = characters, isLoading = false) }
            }
            .catch { error ->
                _uiState.update { it.copy(error = error.message, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun addCharacter(character: Character) {
        viewModelScope.launch {
            try {
                addCharacterUseCase(character)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateCharacter(character: Character) {
        viewModelScope.launch {
            try {
                updateCharacterUseCase(character)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteCharacter(character: Character) {
        viewModelScope.launch {
            try {
                deleteCharacterUseCase(character)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun unassignWeapon(characterId: Long, weaponId: Long) {
        viewModelScope.launch {
            try {
                unassignWeaponFromCharacterUseCase(characterId, weaponId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}

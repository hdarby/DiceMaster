package com.hdarby.dicemaster.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.UserRole
import com.hdarby.dicemaster.domain.repository.SessionRepository
import com.hdarby.dicemaster.domain.usecase.character.AddCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.AssignWeaponToCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.DamageCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.DeleteCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.GetCharactersWithWeaponsUseCase
import com.hdarby.dicemaster.domain.usecase.character.HealCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.LevelUpCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.MarkCharacterDeadUseCase
import com.hdarby.dicemaster.domain.usecase.character.SetDeathSaveFailuresUseCase
import com.hdarby.dicemaster.domain.usecase.character.UnassignWeaponFromCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.UpdateCharacterUseCase
import com.hdarby.dicemaster.viewmodel.state.CharacterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CharacterViewModel(
    private val getCharactersWithWeaponsUseCase: GetCharactersWithWeaponsUseCase,
    private val addCharacterUseCase: AddCharacterUseCase,
    private val updateCharacterUseCase: UpdateCharacterUseCase,
    private val deleteCharacterUseCase: DeleteCharacterUseCase,
    private val unassignWeaponFromCharacterUseCase: UnassignWeaponFromCharacterUseCase,
    private val assignWeaponToCharacterUseCase: AssignWeaponToCharacterUseCase,
    private val sessionRepository: SessionRepository,
    private val healCharacterUseCase: HealCharacterUseCase,
    private val damageCharacterUseCase: DamageCharacterUseCase,
    private val setDeathSaveFailuresUseCase: SetDeathSaveFailuresUseCase,
    private val markCharacterDeadUseCase: MarkCharacterDeadUseCase,
    private val levelUpCharacterUseCase: LevelUpCharacterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CharacterUiState())
    val uiState: StateFlow<CharacterUiState> = _uiState.asStateFlow()

    init {
        loadCharacters()
    }

    private fun loadCharacters() {
        getCharactersWithWeaponsUseCase()
            .combine(sessionRepository.observeSession()) { characters, session ->
                Pair(characters, session?.role)
            }
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { (characters, role) ->
                val visible = when (role) {
                    is UserRole.Player -> characters.filter { it.character.id == role.characterId }
                    else -> characters
                }
                _uiState.update { it.copy(characters = visible, userRole = role, isLoading = false) }
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

    fun assignWeapon(characterId: Long, weaponId: Long) {
        viewModelScope.launch {
            try {
                assignWeaponToCharacterUseCase(characterId, weaponId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun unassignWeapon(assignmentId: Long) {
        viewModelScope.launch {
            try {
                unassignWeaponFromCharacterUseCase(assignmentId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun heal(character: Character) {
        viewModelScope.launch {
            try {
                healCharacterUseCase(character)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun damage(character: Character) {
        viewModelScope.launch {
            try {
                damageCharacterUseCase(character)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun setDeathSaveFailures(character: Character, failures: Int) {
        viewModelScope.launch {
            try {
                setDeathSaveFailuresUseCase(character, failures)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun markDead(character: Character) {
        viewModelScope.launch {
            try {
                markCharacterDeadUseCase(character)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun levelUp(character: Character) {
        viewModelScope.launch {
            try {
                levelUpCharacterUseCase(character)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}

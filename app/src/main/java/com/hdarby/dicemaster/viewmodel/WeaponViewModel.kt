package com.hdarby.dicemaster.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.domain.usecase.character.AssignWeaponToCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.AddWeaponUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.DeleteWeaponUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.GetWeaponsUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.UpdateWeaponUseCase
import com.hdarby.dicemaster.viewmodel.state.WeaponUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeaponViewModel(
    private val getWeaponsUseCase: GetWeaponsUseCase,
    private val addWeaponUseCase: AddWeaponUseCase,
    private val updateWeaponUseCase: UpdateWeaponUseCase,
    private val deleteWeaponUseCase: DeleteWeaponUseCase,
    private val assignWeaponToCharacterUseCase: AssignWeaponToCharacterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeaponUiState())
    val uiState: StateFlow<WeaponUiState> = _uiState.asStateFlow()

    init {
        loadWeapons()
    }

    private fun loadWeapons() {
        getWeaponsUseCase()
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { weapons ->
                _uiState.update { it.copy(weapons = weapons, isLoading = false) }
            }
            .catch { error ->
                _uiState.update { it.copy(error = error.message, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun addWeapon(weapon: Weapon) {
        viewModelScope.launch {
            try {
                addWeaponUseCase(weapon)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateWeapon(weapon: Weapon) {
        viewModelScope.launch {
            try {
                updateWeaponUseCase(weapon)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteWeapon(weapon: Weapon) {
        viewModelScope.launch {
            try {
                deleteWeaponUseCase(weapon)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun assignWeaponToCharacter(characterId: Long, weaponId: Long) {
        viewModelScope.launch {
            try {
                assignWeaponToCharacterUseCase(characterId, weaponId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}

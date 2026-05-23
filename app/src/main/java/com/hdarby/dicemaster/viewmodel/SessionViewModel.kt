package com.hdarby.dicemaster.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdarby.dicemaster.data.remote.FirebaseAuthDataSource
import com.hdarby.dicemaster.data.remote.SessionRemoteDataSource
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.Session
import com.hdarby.dicemaster.domain.model.UserRole
import com.hdarby.dicemaster.domain.repository.CharacterRepository
import com.hdarby.dicemaster.domain.repository.SessionRepository
import com.hdarby.dicemaster.viewmodel.state.SessionSetupStep
import com.hdarby.dicemaster.viewmodel.state.SessionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class SessionViewModel(
    private val sessionRepository: SessionRepository,
    private val authDataSource: FirebaseAuthDataSource,
    private val remoteDataSource: SessionRemoteDataSource,
    private val characterRepository: CharacterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            try {
                val session = sessionRepository.getActiveSession()
                _uiState.update { it.copy(isCheckingSession = false, currentSession = session) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isCheckingSession = false, error = e.message) }
            }
        }
    }

    fun onCreateSessionClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val uid = authDataSource.ensureSignedIn()
                val code = generateSessionCode()
                remoteDataSource.createSession(code, uid)
                val session = Session(code, UserRole.DungeonMaster)
                sessionRepository.saveSession(session)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        setupStep = SessionSetupStep.ShowingSessionCode,
                        generatedCode = code
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onSessionCodeConfirmed() {
        val code = _uiState.value.generatedCode ?: return
        _uiState.update { it.copy(currentSession = Session(code, UserRole.DungeonMaster)) }
    }

    fun onJoinSessionClicked() {
        _uiState.update { it.copy(setupStep = SessionSetupStep.EnteringJoinCode, error = null) }
    }

    fun onJoinCodeChanged(code: String) {
        _uiState.update { it.copy(joinCodeInput = code.uppercase()) }
    }

    fun onJoinCodeSubmitted() {
        val code = _uiState.value.joinCodeInput.trim()
        if (code.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                authDataSource.ensureSignedIn()
                val exists = remoteDataSource.sessionExists(code)
                if (!exists) {
                    _uiState.update { it.copy(isLoading = false, error = "Session '$code' not found. Check the code and try again.") }
                    return@launch
                }
                // TODO (FEAT-006d): load characters from Firestore once remote character sync is in place.
                // For now, characters come from the local Room database.
                val characters = characterRepository.getAllCharacters().first()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        setupStep = SessionSetupStep.SelectingCharacter,
                        availableCharacters = characters
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onCharacterSelected(character: Character) {
        val code = _uiState.value.joinCodeInput.trim()
        viewModelScope.launch {
            try {
                val session = Session(code, UserRole.Player(character.id))
                sessionRepository.saveSession(session)
                _uiState.update { it.copy(currentSession = session) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onLeaveSession() {
        viewModelScope.launch {
            try {
                sessionRepository.clearSession()
                _uiState.update {
                    SessionUiState(
                        isCheckingSession = false,
                        currentSession = null,
                        setupStep = SessionSetupStep.Landing
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(error = null) }
    }

    private fun generateSessionCode(): String =
        UUID.randomUUID().toString().replace("-", "").take(SESSION_CODE_LENGTH).uppercase()

    companion object {
        private const val SESSION_CODE_LENGTH = 8
    }
}


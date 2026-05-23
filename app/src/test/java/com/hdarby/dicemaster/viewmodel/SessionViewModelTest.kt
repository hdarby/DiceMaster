package com.hdarby.dicemaster.viewmodel

import app.cash.turbine.test
import com.hdarby.dicemaster.data.remote.SessionRemoteDataSource
import com.hdarby.dicemaster.data.remote.FirebaseAuthDataSource
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.Session
import com.hdarby.dicemaster.domain.model.Stats
import com.hdarby.dicemaster.domain.model.UserRole
import com.hdarby.dicemaster.domain.repository.CharacterRepository
import com.hdarby.dicemaster.domain.repository.SessionRepository
import com.hdarby.dicemaster.viewmodel.state.SessionSetupStep
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    private val sessionRepository: SessionRepository = mockk(relaxed = true)
    private val authDataSource: FirebaseAuthDataSource = mockk(relaxed = true)
    private val remoteDataSource: SessionRemoteDataSource = mockk(relaxed = true)
    private val characterRepository: CharacterRepository = mockk(relaxed = true)
    private lateinit var viewModel: SessionViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testCharacter = Character(
        id = 1L,
        name = "Gandalf",
        race = "Wizard",
        stats = Stats(10, 0, 10, 0, 12, 1, 10, 0, 18, 4, 14, 2)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { sessionRepository.getActiveSession() } returns null
        coEvery { authDataSource.ensureSignedIn() } returns "uid-123"
        coEvery { characterRepository.getAllCharacters() } returns flowOf(listOf(testCharacter))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── init ──────────────────────────────────────────────────────────────────

    @Test
    fun `init - no existing session - isCheckingSession becomes false and currentSession is null`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns null
        viewModel = buildViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isCheckingSession)
            assertNull(state.currentSession)
        }
    }

    @Test
    fun `init - existing session found - currentSession is populated`() = runTest {
        val session = Session("ABCD1234", UserRole.DungeonMaster)
        coEvery { sessionRepository.getActiveSession() } returns session
        viewModel = buildViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isCheckingSession)
            assertEquals(session, state.currentSession)
        }
    }

    @Test
    fun `init - repository throws - error is set and isCheckingSession becomes false`() = runTest {
        coEvery { sessionRepository.getActiveSession() } throws RuntimeException("DataStore error")
        viewModel = buildViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isCheckingSession)
            assertNull(state.currentSession)
            assertEquals("DataStore error", state.error)
        }
    }

    // ── onCreateSessionClicked ────────────────────────────────────────────────

    @Test
    fun `onCreateSessionClicked - success - step becomes ShowingSessionCode with generated code`() = runTest {
        coEvery { remoteDataSource.createSession(any(), any()) } returns Unit
        coEvery { sessionRepository.saveSession(any()) } returns Unit
        viewModel = buildViewModel()

        viewModel.onCreateSessionClicked()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(SessionSetupStep.ShowingSessionCode, state.setupStep)
            assertFalse(state.isLoading)
            assertNotNull(state.generatedCode)
            assertTrue(state.generatedCode!!.length == 8)
        }
    }

    @Test
    fun `onCreateSessionClicked - remote throws - error is set`() = runTest {
        coEvery { remoteDataSource.createSession(any(), any()) } throws RuntimeException("Network error")
        viewModel = buildViewModel()

        viewModel.onCreateSessionClicked()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("Network error", state.error)
        }
    }

    @Test
    fun `onCreateSessionClicked - saves session with DungeonMaster role`() = runTest {
        coEvery { remoteDataSource.createSession(any(), any()) } returns Unit
        viewModel = buildViewModel()

        viewModel.onCreateSessionClicked()

        coVerify { sessionRepository.saveSession(match { it.role is UserRole.DungeonMaster }) }
    }

    // ── onSessionCodeConfirmed ────────────────────────────────────────────────

    @Test
    fun `onSessionCodeConfirmed - sets currentSession with DungeonMaster role`() = runTest {
        coEvery { remoteDataSource.createSession(any(), any()) } returns Unit
        coEvery { sessionRepository.saveSession(any()) } returns Unit
        viewModel = buildViewModel()
        viewModel.onCreateSessionClicked()
        viewModel.onSessionCodeConfirmed()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.currentSession)
            assertTrue(state.currentSession!!.role is UserRole.DungeonMaster)
        }
    }

    @Test
    fun `onSessionCodeConfirmed - no generated code - does nothing`() = runTest {
        viewModel = buildViewModel()
        // generatedCode is null at this point
        viewModel.onSessionCodeConfirmed()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.currentSession)
        }
    }

    // ── onJoinSessionClicked ──────────────────────────────────────────────────

    @Test
    fun `onJoinSessionClicked - step becomes EnteringJoinCode`() = runTest {
        viewModel = buildViewModel()

        viewModel.onJoinSessionClicked()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(SessionSetupStep.EnteringJoinCode, state.setupStep)
        }
    }

    // ── onJoinCodeChanged ─────────────────────────────────────────────────────

    @Test
    fun `onJoinCodeChanged - input is uppercased`() = runTest {
        viewModel = buildViewModel()

        viewModel.onJoinCodeChanged("ab12cd34")

        viewModel.uiState.test {
            assertEquals("AB12CD34", awaitItem().joinCodeInput)
        }
    }

    // ── onJoinCodeSubmitted ───────────────────────────────────────────────────

    @Test
    fun `onJoinCodeSubmitted - blank code - does nothing`() = runTest {
        viewModel = buildViewModel()
        viewModel.onJoinSessionClicked()
        viewModel.onJoinCodeChanged("   ")

        viewModel.onJoinCodeSubmitted()

        viewModel.uiState.test {
            val state = awaitItem()
            // Still on EnteringJoinCode, not progressed
            assertEquals(SessionSetupStep.EnteringJoinCode, state.setupStep)
        }
    }

    @Test
    fun `onJoinCodeSubmitted - session not found - error is set`() = runTest {
        coEvery { remoteDataSource.sessionExists(any()) } returns false
        viewModel = buildViewModel()
        viewModel.onJoinSessionClicked()
        viewModel.onJoinCodeChanged("NOTFOUND")

        viewModel.onJoinCodeSubmitted()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNotNull(state.error)
            assertTrue(state.error!!.contains("NOTFOUND"))
        }
    }

    @Test
    fun `onJoinCodeSubmitted - session exists - step becomes SelectingCharacter with characters`() = runTest {
        coEvery { remoteDataSource.sessionExists("VALID123") } returns true
        viewModel = buildViewModel()
        viewModel.onJoinSessionClicked()
        viewModel.onJoinCodeChanged("VALID123")

        viewModel.onJoinCodeSubmitted()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(SessionSetupStep.SelectingCharacter, state.setupStep)
            assertEquals(listOf(testCharacter), state.availableCharacters)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `onJoinCodeSubmitted - remote throws - error is set`() = runTest {
        coEvery { remoteDataSource.sessionExists(any()) } throws RuntimeException("Firestore error")
        viewModel = buildViewModel()
        viewModel.onJoinSessionClicked()
        viewModel.onJoinCodeChanged("ABCD1234")

        viewModel.onJoinCodeSubmitted()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("Firestore error", state.error)
        }
    }

    // ── onCharacterSelected ───────────────────────────────────────────────────

    @Test
    fun `onCharacterSelected - saves session with Player role and character id`() = runTest {
        coEvery { remoteDataSource.sessionExists("ABCD1234") } returns true
        viewModel = buildViewModel()
        viewModel.onJoinSessionClicked()
        viewModel.onJoinCodeChanged("ABCD1234")
        viewModel.onJoinCodeSubmitted()

        viewModel.onCharacterSelected(testCharacter)

        coVerify {
            sessionRepository.saveSession(
                match { it.role is UserRole.Player && (it.role as UserRole.Player).characterId == testCharacter.id }
            )
        }
    }

    @Test
    fun `onCharacterSelected - currentSession is set with Player role`() = runTest {
        coEvery { remoteDataSource.sessionExists("ABCD1234") } returns true
        viewModel = buildViewModel()
        viewModel.onJoinSessionClicked()
        viewModel.onJoinCodeChanged("ABCD1234")
        viewModel.onJoinCodeSubmitted()

        viewModel.onCharacterSelected(testCharacter)

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.currentSession)
            val role = state.currentSession!!.role
            assertTrue(role is UserRole.Player)
            assertEquals(testCharacter.id, (role as UserRole.Player).characterId)
        }
    }

    @Test
    fun `onCharacterSelected - repository throws - error is set`() = runTest {
        coEvery { remoteDataSource.sessionExists("ABCD1234") } returns true
        coEvery { sessionRepository.saveSession(any()) } throws RuntimeException("Save error")
        viewModel = buildViewModel()
        viewModel.onJoinSessionClicked()
        viewModel.onJoinCodeChanged("ABCD1234")
        viewModel.onJoinCodeSubmitted()

        viewModel.onCharacterSelected(testCharacter)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Save error", state.error)
        }
    }

    // ── onLeaveSession ────────────────────────────────────────────────────────

    @Test
    fun `onLeaveSession - clears session repository and resets state`() = runTest {
        val session = Session("ABCD1234", UserRole.DungeonMaster)
        coEvery { sessionRepository.getActiveSession() } returns session
        viewModel = buildViewModel()

        viewModel.onLeaveSession()

        coVerify { sessionRepository.clearSession() }
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.currentSession)
            assertFalse(state.isCheckingSession)
            assertEquals(SessionSetupStep.Landing, state.setupStep)
        }
    }

    @Test
    fun `onLeaveSession - repository throws - error is set`() = runTest {
        coEvery { sessionRepository.clearSession() } throws RuntimeException("Clear error")
        viewModel = buildViewModel()

        viewModel.onLeaveSession()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Clear error", state.error)
        }
    }

    // ── onErrorDismissed ──────────────────────────────────────────────────────

    @Test
    fun `onErrorDismissed - clears error`() = runTest {
        coEvery { remoteDataSource.createSession(any(), any()) } throws RuntimeException("err")
        viewModel = buildViewModel()
        viewModel.onCreateSessionClicked()

        viewModel.onErrorDismissed()

        viewModel.uiState.test {
            assertNull(awaitItem().error)
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun buildViewModel() = SessionViewModel(
        sessionRepository = sessionRepository,
        authDataSource = authDataSource,
        remoteDataSource = remoteDataSource,
        characterRepository = characterRepository
    )
}


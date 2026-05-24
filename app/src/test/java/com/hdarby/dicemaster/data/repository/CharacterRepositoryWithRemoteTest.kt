package com.hdarby.dicemaster.data.repository

import app.cash.turbine.test
import com.hdarby.dicemaster.data.local.dao.CharacterDao
import com.hdarby.dicemaster.data.local.dao.WeaponDao
import com.hdarby.dicemaster.data.local.entity.CharacterEntity
import com.hdarby.dicemaster.data.local.entity.WeaponEntity
import com.hdarby.dicemaster.data.remote.FakeCharacterRemoteDataSource
import com.hdarby.dicemaster.data.remote.FakeWeaponRemoteDataSource
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.Session
import com.hdarby.dicemaster.domain.model.Stats
import com.hdarby.dicemaster.domain.model.UserRole
import com.hdarby.dicemaster.domain.repository.SessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterRepositoryWithRemoteTest {

    private val characterDao: CharacterDao = mockk(relaxed = true)
    private val weaponDao: WeaponDao = mockk(relaxed = true)
    private val sessionRepository: SessionRepository = mockk()
    private val characterRemote = FakeCharacterRemoteDataSource()
    private val weaponRemote = FakeWeaponRemoteDataSource()

    private val activeSession = Session("SESSION1", UserRole.DungeonMaster)

    private val testCharacter = Character(
        id = 1L,
        name = "Grog",
        race = "Goliath",
        stats = Stats(20, 5, 12, 1, 18, 4, 6, -2, 10, 0, 8, -1)
    )

    private fun buildRepo() = CharacterRepositoryImpl(
        characterDao = characterDao,
        weaponDao = weaponDao,
        sessionRepository = sessionRepository,
        characterRemoteDataSource = characterRemote,
        weaponRemoteDataSource = weaponRemote,
        externalScope = kotlinx.coroutines.CoroutineScope(UnconfinedTestDispatcher())
    )

    // ── addCharacter ──────────────────────────────────────────────────────────

    @Test
    fun `addCharacter - session active - upserts to remote`() = runTest {
        coEvery { characterDao.insertCharacter(any()) } returns 1L
        coEvery { sessionRepository.getActiveSession() } returns activeSession
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().addCharacter(testCharacter)

        assertEquals(1, characterRemote.upsertCallCount)
        assertEquals("SESSION1", characterRemote.lastUpsertedSessionId)
    }

    @Test
    fun `addCharacter - no session - does not call remote`() = runTest {
        coEvery { characterDao.insertCharacter(any()) } returns 1L
        coEvery { sessionRepository.getActiveSession() } returns null
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().addCharacter(testCharacter)

        assertEquals(0, characterRemote.upsertCallCount)
    }

    // ── updateCharacter ───────────────────────────────────────────────────────

    @Test
    fun `updateCharacter - session active - upserts to remote`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns activeSession
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().updateCharacter(testCharacter)

        assertEquals(1, characterRemote.upsertCallCount)
    }

    @Test
    fun `updateCharacter - no session - skips remote`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns null
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().updateCharacter(testCharacter)

        assertEquals(0, characterRemote.upsertCallCount)
    }

    // ── deleteCharacter ───────────────────────────────────────────────────────

    @Test
    fun `deleteCharacter - session active - deletes from remote`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns activeSession
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().deleteCharacter(testCharacter)

        assertEquals(1, characterRemote.deleteCallCount)
        assertEquals(testCharacter.id, characterRemote.lastDeletedCharacterId)
    }

    @Test
    fun `deleteCharacter - no session - skips remote`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns null
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().deleteCharacter(testCharacter)

        assertEquals(0, characterRemote.deleteCallCount)
    }

    // ── weapon assignment ─────────────────────────────────────────────────────

    @Test
    fun `assignWeaponToCharacter - inserts cross-ref in local DAO`() = runTest {
        every { sessionRepository.observeSession() } returns flowOf(null)
        // relaxed mock returns false for isAtomicWeapon and 0 for getWeaponAssignmentCount

        buildRepo().assignWeaponToCharacter(characterId = 1L, weaponId = 2L)

        coVerify { weaponDao.insertCharacterWeaponCrossRef(match { it.characterId == 1L && it.weaponId == 2L }) }
    }

    @Test
    fun `unassignWeaponFromCharacter - deletes cross-ref by assignmentId`() = runTest {
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().unassignWeaponFromCharacter(assignmentId = 5L)

        coVerify { weaponDao.deleteCharacterWeaponCrossRef(5L) }
    }

    // ── remote snapshot sync ──────────────────────────────────────────────────

    @Test
    fun `remote character snapshot - syncs to Room via insertCharacter`() = runTest {
        coEvery { characterDao.insertCharacter(any()) } returns testCharacter.id
        every { sessionRepository.observeSession() } returns flowOf(activeSession)

        buildRepo()

        characterRemote.simulateRemoteUpdate(testCharacter)

        coVerify { characterDao.insertCharacter(match { it.name == "Grog" }) }
    }

    @Test
    fun `no session - snapshot listener not started`() = runTest {
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo()

        characterRemote.simulateRemoteUpdate(testCharacter)

        coVerify(exactly = 0) { characterDao.insertCharacter(any()) }
    }

    // ── local DAO pass-through (no session) ───────────────────────────────────

    @Test
    fun `getAllCharacters returns characters from DAO`() = runTest {
        val entity = CharacterEntity(
            id = 1, name = "Grog", race = "Goliath",
            strength = 20, dexterity = 12, constitution = 18,
            intelligence = 6, wisdom = 10, charisma = 8
        )
        every { characterDao.getAllCharacters() } returns flowOf(listOf(entity))
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().getAllCharacters().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Grog", result[0].name)
            awaitComplete()
        }
    }
}






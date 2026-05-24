package com.hdarby.dicemaster.data.repository

import com.hdarby.dicemaster.data.local.dao.WeaponDao
import com.hdarby.dicemaster.data.local.entity.WeaponEntity
import com.hdarby.dicemaster.data.remote.FakeWeaponRemoteDataSource
import com.hdarby.dicemaster.data.remote.RemoteWeapon
import com.hdarby.dicemaster.domain.model.Session
import com.hdarby.dicemaster.domain.model.UserRole
import com.hdarby.dicemaster.domain.model.Weapon
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
class WeaponRepositoryWithRemoteTest {

    private val weaponDao: WeaponDao = mockk(relaxed = true)
    private val sessionRepository: SessionRepository = mockk()
    private val weaponRemote = FakeWeaponRemoteDataSource()

    private val activeSession = Session("SESSION1", UserRole.DungeonMaster)
    private val testWeapon = Weapon(1L, "Greataxe", "Greataxe", "1d12", "Slashing", 2)

    private fun buildRepo() = WeaponRepositoryImpl(
        weaponDao = weaponDao,
        sessionRepository = sessionRepository,
        weaponRemoteDataSource = weaponRemote,
        externalScope = kotlinx.coroutines.CoroutineScope(UnconfinedTestDispatcher())
    )

    // ── addWeapon ─────────────────────────────────────────────────────────────

    @Test
    fun `addWeapon - session active - upserts to remote`() = runTest {
        coEvery { weaponDao.insertWeapon(any()) } returns 1L
        coEvery { sessionRepository.getActiveSession() } returns activeSession
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().addWeapon(testWeapon)

        assertEquals(1, weaponRemote.upsertCallCount)
        assertEquals("SESSION1", weaponRemote.lastUpsertedSessionId)
    }

    @Test
    fun `addWeapon - no session - skips remote`() = runTest {
        coEvery { weaponDao.insertWeapon(any()) } returns 1L
        coEvery { sessionRepository.getActiveSession() } returns null
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().addWeapon(testWeapon)

        assertEquals(0, weaponRemote.upsertCallCount)
    }

    // ── updateWeapon ──────────────────────────────────────────────────────────

    @Test
    fun `updateWeapon - session active - upserts to remote`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns activeSession
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().updateWeapon(testWeapon)

        assertEquals(1, weaponRemote.upsertCallCount)
    }

    @Test
    fun `updateWeapon - no session - skips remote`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns null
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().updateWeapon(testWeapon)

        assertEquals(0, weaponRemote.upsertCallCount)
    }

    // ── deleteWeapon ──────────────────────────────────────────────────────────

    @Test
    fun `deleteWeapon - session active - deletes from remote`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns activeSession
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().deleteWeapon(testWeapon)

        assertEquals(1, weaponRemote.deleteCallCount)
    }

    @Test
    fun `deleteWeapon - no session - skips remote`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns null
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().deleteWeapon(testWeapon)

        assertEquals(0, weaponRemote.deleteCallCount)
    }

    // ── remote snapshot sync ──────────────────────────────────────────────────

    @Test
    fun `remote weapon snapshot - syncs to Room`() = runTest {
        every { sessionRepository.observeSession() } returns flowOf(activeSession)

        buildRepo()

        weaponRemote.simulateRemoteUpdate(RemoteWeapon(testWeapon))

        coVerify { weaponDao.insertWeapon(match { it.name == "Greataxe" }) }
    }

    @Test
    fun `no session - snapshot listener not started`() = runTest {
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo()

        weaponRemote.simulateRemoteUpdate(RemoteWeapon(testWeapon))

        coVerify(exactly = 0) { weaponDao.insertWeapon(any()) }
    }

    // ── DAO pass-through ──────────────────────────────────────────────────────

    @Test
    fun `getAllWeapons - returns DAO weapons`() = runTest {
        val entity = WeaponEntity(1L, "Greataxe", "Greataxe", "1d12", "Slashing", 2)
        every { weaponDao.getAllWeapons() } returns flowOf(listOf(entity))
        every { sessionRepository.observeSession() } returns flowOf(null)

        val repo = buildRepo()
        val result = mutableListOf<List<Weapon>>()
        repo.getAllWeapons().collect { result.add(it) }

        assertEquals(1, result[0].size)
        assertEquals("Greataxe", result[0][0].name)
    }
}



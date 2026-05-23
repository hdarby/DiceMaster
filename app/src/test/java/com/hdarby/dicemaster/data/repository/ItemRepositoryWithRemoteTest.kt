package com.hdarby.dicemaster.data.repository

import com.hdarby.dicemaster.data.local.dao.ItemDao
import com.hdarby.dicemaster.data.local.entity.CharacterItemCrossRef
import com.hdarby.dicemaster.data.local.entity.ItemEntity
import com.hdarby.dicemaster.data.remote.FakeItemRemoteDataSource
import com.hdarby.dicemaster.data.remote.RemoteCharacterItem
import com.hdarby.dicemaster.domain.model.ConsumableItem
import com.hdarby.dicemaster.domain.model.Session
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
class ItemRepositoryWithRemoteTest {

    private val itemDao: ItemDao = mockk(relaxed = true)
    private val sessionRepository: SessionRepository = mockk()
    private val itemRemote = FakeItemRemoteDataSource()

    private val activeSession = Session("SESSION1", UserRole.DungeonMaster)
    private val testItem = ConsumableItem(1L, "Healing Potion", "Restores 2d4+2 HP", totalQuantity = 5)

    private fun buildRepo() = ItemRepositoryImpl(
        itemDao = itemDao,
        sessionRepository = sessionRepository,
        itemRemoteDataSource = itemRemote,
        externalScope = kotlinx.coroutines.CoroutineScope(UnconfinedTestDispatcher())
    )

    // ── addItem ───────────────────────────────────────────────────────────────

    @Test
    fun `addItem - session active - upserts to remote`() = runTest {
        coEvery { itemDao.insertItem(any()) } returns 1L
        coEvery { sessionRepository.getActiveSession() } returns activeSession
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().addItem(testItem)

        assertEquals(1, itemRemote.upsertItemCallCount)
    }

    @Test
    fun `addItem - no session - skips remote`() = runTest {
        coEvery { itemDao.insertItem(any()) } returns 1L
        coEvery { sessionRepository.getActiveSession() } returns null
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().addItem(testItem)

        assertEquals(0, itemRemote.upsertItemCallCount)
    }

    // ── updateItem ────────────────────────────────────────────────────────────

    @Test
    fun `updateItem - session active - upserts to remote`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns activeSession
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().updateItem(testItem)

        assertEquals(1, itemRemote.upsertItemCallCount)
    }

    @Test
    fun `updateItem - no session - skips remote`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns null
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().updateItem(testItem)

        assertEquals(0, itemRemote.upsertItemCallCount)
    }

    // ── deleteItem ────────────────────────────────────────────────────────────

    @Test
    fun `deleteItem - session active - deletes from remote`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns activeSession
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().deleteItem(testItem)

        assertEquals(1, itemRemote.deleteItemCallCount)
    }

    @Test
    fun `deleteItem - no session - skips remote`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns null
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().deleteItem(testItem)

        assertEquals(0, itemRemote.deleteItemCallCount)
    }

    // ── assignItemToCharacter ─────────────────────────────────────────────────

    @Test
    fun `assignItemToCharacter - session active - upserts cross-ref to remote`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns activeSession
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().assignItemToCharacter(characterId = 1L, itemId = 2L, quantity = 3)

        coVerify { itemDao.insertCharacterItemCrossRef(CharacterItemCrossRef(1L, 2L, 3)) }
        assertEquals(1, itemRemote.upsertCharacterItemCallCount)
    }

    @Test
    fun `assignItemToCharacter - no session - skips remote`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns null
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().assignItemToCharacter(characterId = 1L, itemId = 2L, quantity = 3)

        assertEquals(0, itemRemote.upsertCharacterItemCallCount)
    }

    // ── unassignItemFromCharacter ─────────────────────────────────────────────

    @Test
    fun `unassignItemFromCharacter - session active - deletes cross-ref from remote`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns activeSession
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().unassignItemFromCharacter(characterId = 1L, itemId = 2L)

        coVerify { itemDao.deleteCharacterItemCrossRef(1L, 2L) }
        assertEquals(1, itemRemote.deleteCharacterItemCallCount)
    }

    // ── updateItemQuantity ────────────────────────────────────────────────────

    @Test
    fun `updateItemQuantity - session active - updates remote quantity`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns activeSession
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().updateItemQuantity(characterId = 1L, itemId = 2L, quantity = 5)

        coVerify { itemDao.updateQuantity(1L, 2L, 5) }
        assertEquals(1, itemRemote.updateQuantityCallCount)
        assertEquals(Triple(1L, 2L, 5), itemRemote.lastQuantityUpdate)
    }

    @Test
    fun `updateItemQuantity - no session - skips remote`() = runTest {
        coEvery { sessionRepository.getActiveSession() } returns null
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().updateItemQuantity(characterId = 1L, itemId = 2L, quantity = 5)

        assertEquals(0, itemRemote.updateQuantityCallCount)
    }

    // ── remote snapshot sync ──────────────────────────────────────────────────

    @Test
    fun `remote item snapshot - syncs to Room`() = runTest {
        every { sessionRepository.observeSession() } returns flowOf(activeSession)

        buildRepo()

        itemRemote.simulateRemoteItemUpdate(testItem)

        coVerify { itemDao.insertItem(match { it.name == "Healing Potion" }) }
    }

    @Test
    fun `remote character item snapshot - upserts cross-ref to Room`() = runTest {
        every { sessionRepository.observeSession() } returns flowOf(activeSession)

        buildRepo()

        itemRemote.simulateRemoteCharacterItemUpdate(RemoteCharacterItem(1L, 2L, 4))

        coVerify { itemDao.upsertCharacterItemCrossRef(CharacterItemCrossRef(1L, 2L, 4)) }
    }

    @Test
    fun `no session - snapshot listeners not started`() = runTest {
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo()

        itemRemote.simulateRemoteItemUpdate(testItem)

        coVerify(exactly = 0) { itemDao.insertItem(any()) }
    }

    // ── DAO pass-through ──────────────────────────────────────────────────────

    @Test
    fun `getAllItems - returns DAO items`() = runTest {
        val entity = ItemEntity(1L, "Healing Potion", "Restores 2d4+2 HP", 5)
        every { itemDao.getAllItems() } returns flowOf(listOf(entity))
        every { sessionRepository.observeSession() } returns flowOf(null)

        val result = mutableListOf<List<ConsumableItem>>()
        buildRepo().getAllItems().collect { result.add(it) }

        assertEquals(1, result[0].size)
        assertEquals("Healing Potion", result[0][0].name)
    }
}


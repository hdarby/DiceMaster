package com.hdarby.dicemaster.data.repository

import com.hdarby.dicemaster.data.local.dao.ItemDao
import com.hdarby.dicemaster.data.local.entity.ItemEntity
import com.hdarby.dicemaster.data.remote.FakeItemRemoteDataSource
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
    fun `assignItemToCharacter - inserts cross-ref in local DAO and returns assignmentId`() = runTest {
        coEvery { itemDao.insertCharacterItemCrossRef(any()) } returns 7L
        every { sessionRepository.observeSession() } returns flowOf(null)

        val assignmentId = buildRepo().assignItemToCharacter(characterId = 1L, itemId = 2L, quantity = 3)

        assertEquals(7L, assignmentId)
        coVerify { itemDao.insertCharacterItemCrossRef(match { it.characterId == 1L && it.itemId == 2L && it.quantity == 3 }) }
    }

    // ── unassignItemFromCharacter ─────────────────────────────────────────────

    @Test
    fun `unassignItemFromCharacter - deletes cross-ref by assignmentId in local DAO`() = runTest {
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().unassignItemFromCharacter(assignmentId = 5L)

        coVerify { itemDao.deleteCharacterItemCrossRef(5L) }
    }

    // ── updateItemQuantity ────────────────────────────────────────────────────

    @Test
    fun `updateItemQuantity - updates quantity for assignmentId in local DAO`() = runTest {
        every { sessionRepository.observeSession() } returns flowOf(null)

        buildRepo().updateItemQuantity(assignmentId = 10L, quantity = 5)

        coVerify { itemDao.updateQuantity(10L, 5) }
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





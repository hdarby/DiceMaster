package com.hdarby.dicemaster.data.repository

import com.hdarby.dicemaster.domain.model.Session
import com.hdarby.dicemaster.domain.model.UserRole
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SessionRepositoryImplTest {

    private lateinit var dataStore: FakePreferencesDataStore
    private lateinit var repository: SessionRepositoryImpl

    @Before
    fun setUp() {
        dataStore = FakePreferencesDataStore()
        repository = SessionRepositoryImpl(dataStore)
    }

    @Test
    fun `getActiveSession returns null when no session has been saved`() = runTest {
        assertNull(repository.getActiveSession())
    }

    @Test
    fun `saveSession then getActiveSession returns DungeonMaster session`() = runTest {
        val session = Session(sessionId = "abc123", role = UserRole.DungeonMaster)

        repository.saveSession(session)

        assertEquals(session, repository.getActiveSession())
    }

    @Test
    fun `saveSession then getActiveSession returns Player session with correct characterId`() = runTest {
        val session = Session(sessionId = "xyz789", role = UserRole.Player(characterId = 42L))

        repository.saveSession(session)

        assertEquals(session, repository.getActiveSession())
    }

    @Test
    fun `saveSession overwrites a previous session`() = runTest {
        val first = Session(sessionId = "first", role = UserRole.DungeonMaster)
        val second = Session(sessionId = "second", role = UserRole.Player(characterId = 7L))

        repository.saveSession(first)
        repository.saveSession(second)

        assertEquals(second, repository.getActiveSession())
    }

    @Test
    fun `clearSession causes getActiveSession to return null`() = runTest {
        repository.saveSession(Session(sessionId = "toDelete", role = UserRole.DungeonMaster))

        repository.clearSession()

        assertNull(repository.getActiveSession())
    }

    @Test
    fun `Player session does not leak characterId after switching to DungeonMaster`() = runTest {
        repository.saveSession(Session(sessionId = "s1", role = UserRole.Player(characterId = 99L)))
        repository.saveSession(Session(sessionId = "s1", role = UserRole.DungeonMaster))

        val result = repository.getActiveSession()

        assertEquals(UserRole.DungeonMaster, result?.role)
    }
}


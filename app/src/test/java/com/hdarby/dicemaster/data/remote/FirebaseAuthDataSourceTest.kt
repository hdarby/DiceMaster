package com.hdarby.dicemaster.data.remote

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FirebaseAuthDataSourceTest {

    private lateinit var auth: FirebaseAuth
    private lateinit var dataSource: FirebaseAuthDataSource

    @Before
    fun setUp() {
        auth = mockk()
        dataSource = FirebaseAuthDataSource(auth)
    }

    @Test
    fun `currentUid returns null when no user is signed in`() {
        every { auth.currentUser } returns null

        assertNull(dataSource.currentUid)
    }

    @Test
    fun `currentUid returns uid when a user is signed in`() {
        val user = mockk<FirebaseUser>()
        every { user.uid } returns "existing-uid"
        every { auth.currentUser } returns user

        assertEquals("existing-uid", dataSource.currentUid)
    }

    @Test
    fun `signInAnonymously returns uid from completed task`() = runTest {
        val user = mockk<FirebaseUser>()
        every { user.uid } returns "anon-uid"

        val authResult = mockk<AuthResult>()
        every { authResult.user } returns user

        val task = mockk<Task<AuthResult>>()
        every { task.isComplete } returns true
        every { task.isSuccessful } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns authResult
        every { auth.signInAnonymously() } returns task

        val result = dataSource.signInAnonymously()

        assertEquals("anon-uid", result)
        verify { auth.signInAnonymously() }
    }

    @Test
    fun `ensureSignedIn returns existing uid without calling signInAnonymously`() = runTest {
        val user = mockk<FirebaseUser>()
        every { user.uid } returns "already-signed-in-uid"
        every { auth.currentUser } returns user

        val result = dataSource.ensureSignedIn()

        assertEquals("already-signed-in-uid", result)
        verify(exactly = 0) { auth.signInAnonymously() }
    }

    @Test(expected = IllegalStateException::class)
    fun `signInAnonymously throws when task result user is null`() = runTest {
        val authResult = mockk<AuthResult>()
        every { authResult.user } returns null

        val task = mockk<Task<AuthResult>>()
        every { task.isComplete } returns true
        every { task.isSuccessful } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns authResult
        every { auth.signInAnonymously() } returns task

        dataSource.signInAnonymously()
    }
}


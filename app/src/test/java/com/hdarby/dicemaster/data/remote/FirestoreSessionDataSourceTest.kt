package com.hdarby.dicemaster.data.remote

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private const val SESSION_ID = "session1"

class FirestoreSessionDataSourceTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionsCollection: CollectionReference
    private lateinit var sessionDocument: DocumentReference
    private lateinit var dataSource: FirestoreSessionDataSource

    @Before
    fun setUp() {
        firestore = mockk()
        sessionsCollection = mockk()
        sessionDocument = mockk()
        every { firestore.collection("sessions") } returns sessionsCollection
        every { sessionsCollection.document(SESSION_ID) } returns sessionDocument
        dataSource = FirestoreSessionDataSource(firestore)
    }

    @Test
    fun `createSession writes createdBy and createdAt fields to Firestore`() = runTest {
        val dataSlot = io.mockk.slot<Any>()
        every { sessionDocument.set(capture(dataSlot)) } returns voidTask()

        dataSource.createSession(SESSION_ID, "uid-abc")

        @Suppress("UNCHECKED_CAST")
        val written = dataSlot.captured as Map<String, Any?>
        assertEquals("uid-abc", written["createdBy"])
        assertEquals(true, written.containsKey("createdAt"))
    }

    @Test
    fun `sessionExists returns true when document exists`() = runTest {
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.exists() } returns true
        every { sessionDocument.get() } returns documentSnapshotTask(snapshot)

        val result = dataSource.sessionExists(SESSION_ID)

        assertEquals(true, result)
    }

    @Test
    fun `sessionExists returns false when document does not exist`() = runTest {
        val snapshot = mockk<DocumentSnapshot>()
        every { snapshot.exists() } returns false
        every { sessionDocument.get() } returns documentSnapshotTask(snapshot)

        val result = dataSource.sessionExists(SESSION_ID)

        assertEquals(false, result)
    }
}



package com.hdarby.dicemaster.data.remote

import app.cash.turbine.test
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.hdarby.dicemaster.domain.model.ConsumableItem
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private const val SESSION_ID = "session1"
private const val CHARACTER_ID = 2L
private const val ITEM_ID = 1L
private const val CROSS_REF_DOC_ID = "2_1"

class FirestoreItemDataSourceTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionsCollection: CollectionReference
    private lateinit var sessionDocument: DocumentReference
    private lateinit var itemsCollection: CollectionReference
    private lateinit var itemDocument: DocumentReference
    private lateinit var charItemsCollection: CollectionReference
    private lateinit var charItemDocument: DocumentReference
    private lateinit var dataSource: FirestoreItemDataSource

    private val item = ConsumableItem(
        id = ITEM_ID,
        name = "Healing Potion",
        description = "Restores 2d4+2 hit points.",
        totalQuantity = 3
    )

    @Before
    fun setUp() {
        firestore = mockk()
        sessionsCollection = mockk()
        sessionDocument = mockk()
        itemsCollection = mockk()
        itemDocument = mockk()
        charItemsCollection = mockk()
        charItemDocument = mockk()

        every { firestore.collection("sessions") } returns sessionsCollection
        every { sessionsCollection.document(SESSION_ID) } returns sessionDocument
        every { sessionDocument.collection("items") } returns itemsCollection
        every { itemsCollection.document(any()) } returns itemDocument
        every { sessionDocument.collection("characterItems") } returns charItemsCollection
        every { charItemsCollection.document(any()) } returns charItemDocument

        dataSource = FirestoreItemDataSource(firestore)
    }

    // ── upsertItem ───────────────────────────────────────────────────────────

    @Test
    fun `upsertItem writes correct fields to the item document`() = runTest {
        val dataSlot = io.mockk.slot<Any>()
        every { itemDocument.set(capture(dataSlot)) } returns voidTask()

        dataSource.upsertItem(SESSION_ID, item)

        @Suppress("UNCHECKED_CAST")
        val written = dataSlot.captured as Map<String, Any?>
        assertEquals(ITEM_ID, written["id"])
        assertEquals("Healing Potion", written["name"])
        assertEquals("Restores 2d4+2 hit points.", written["description"])
        assertEquals(3, written["totalQuantity"])
    }

    @Test
    fun `upsertItem targets the document keyed by item id`() = runTest {
        every { itemDocument.set(any()) } returns voidTask()

        dataSource.upsertItem(SESSION_ID, item)

        verify { itemsCollection.document(ITEM_ID.toString()) }
    }

    // ── deleteItem ───────────────────────────────────────────────────────────

    @Test
    fun `deleteItem deletes the document keyed by item id`() = runTest {
        every { itemDocument.delete() } returns voidTask()

        dataSource.deleteItem(SESSION_ID, ITEM_ID)

        verify { itemsCollection.document(ITEM_ID.toString()) }
        verify { itemDocument.delete() }
    }

    // ── upsertCharacterItem ──────────────────────────────────────────────────

    @Test
    fun `upsertCharacterItem writes correct cross-ref fields with composite doc id`() = runTest {
        val dataSlot = io.mockk.slot<Any>()
        every { charItemDocument.set(capture(dataSlot)) } returns voidTask()

        dataSource.upsertCharacterItem(SESSION_ID, CHARACTER_ID, ITEM_ID, quantity = 2)

        verify { charItemsCollection.document(CROSS_REF_DOC_ID) }
        @Suppress("UNCHECKED_CAST")
        val written = dataSlot.captured as Map<String, Any?>
        assertEquals(CHARACTER_ID, written["characterId"])
        assertEquals(ITEM_ID, written["itemId"])
        assertEquals(2, written["quantity"])
    }

    // ── deleteCharacterItem ──────────────────────────────────────────────────

    @Test
    fun `deleteCharacterItem deletes the composite-keyed cross-ref document`() = runTest {
        every { charItemDocument.delete() } returns voidTask()

        dataSource.deleteCharacterItem(SESSION_ID, CHARACTER_ID, ITEM_ID)

        verify { charItemsCollection.document(CROSS_REF_DOC_ID) }
        verify { charItemDocument.delete() }
    }

    // ── updateCharacterItemQuantity ──────────────────────────────────────────

    @Test
    fun `updateCharacterItemQuantity updates the quantity field on the cross-ref document`() = runTest {
        val dataSlot = io.mockk.slot<Map<String, Any>>()
        every { charItemDocument.update(capture(dataSlot)) } returns voidTask()

        dataSource.updateCharacterItemQuantity(SESSION_ID, CHARACTER_ID, ITEM_ID, quantity = 5)

        verify { charItemsCollection.document(CROSS_REF_DOC_ID) }
        assertEquals(5, dataSlot.captured["quantity"])
    }

    // ── observeItems ─────────────────────────────────────────────────────────

    @Test
    fun `observeItems emits a mapped list of ConsumableItem from the snapshot`() = runTest {
        val docSnapshot = buildItemDocSnapshot()
        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns listOf(docSnapshot)
        triggerItemsSnapshotListener(querySnapshot, error = null)

        dataSource.observeItems(SESSION_ID).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(ITEM_ID, result[0].id)
            assertEquals("Healing Potion", result[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeItems filters out documents missing required fields`() = runTest {
        val invalidDoc = mockk<DocumentSnapshot>()
        every { invalidDoc.getLong("id") } returns null // missing id -> filtered out

        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns listOf(invalidDoc)
        triggerItemsSnapshotListener(querySnapshot, error = null)

        dataSource.observeItems(SESSION_ID).test {
            assertEquals(emptyList<ConsumableItem>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeItems closes the flow when Firestore reports an error`() = runTest {
        val error = mockk<FirebaseFirestoreException>(relaxed = true)
        triggerItemsSnapshotListener(snapshot = null, error = error)

        dataSource.observeItems(SESSION_ID).test {
            awaitError()
        }
    }

    // ── observeCharacterItems ────────────────────────────────────────────────

    @Test
    fun `observeCharacterItems emits a mapped list of RemoteCharacterItem from the snapshot`() = runTest {
        val docSnapshot = buildCharacterItemDocSnapshot()
        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns listOf(docSnapshot)
        triggerCharacterItemsSnapshotListener(querySnapshot, error = null)

        dataSource.observeCharacterItems(SESSION_ID).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(CHARACTER_ID, result[0].characterId)
            assertEquals(ITEM_ID, result[0].itemId)
            assertEquals(2, result[0].quantity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeCharacterItems filters out documents missing required fields`() = runTest {
        val invalidDoc = mockk<DocumentSnapshot>()
        every { invalidDoc.getLong("characterId") } returns null // missing -> filtered

        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns listOf(invalidDoc)
        triggerCharacterItemsSnapshotListener(querySnapshot, error = null)

        dataSource.observeCharacterItems(SESSION_ID).test {
            assertEquals(emptyList<RemoteCharacterItem>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeCharacterItems closes the flow when Firestore reports an error`() = runTest {
        val error = mockk<FirebaseFirestoreException>(relaxed = true)
        triggerCharacterItemsSnapshotListener(snapshot = null, error = error)

        dataSource.observeCharacterItems(SESSION_ID).test {
            awaitError()
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun triggerItemsSnapshotListener(
        snapshot: QuerySnapshot?,
        error: FirebaseFirestoreException?
    ) {
        val listenerReg = mockk<ListenerRegistration>(relaxed = true)
        every { itemsCollection.addSnapshotListener(any<EventListener<QuerySnapshot>>()) } answers {
            firstArg<EventListener<QuerySnapshot>>().onEvent(snapshot, error)
            listenerReg
        }
    }

    private fun triggerCharacterItemsSnapshotListener(
        snapshot: QuerySnapshot?,
        error: FirebaseFirestoreException?
    ) {
        val listenerReg = mockk<ListenerRegistration>(relaxed = true)
        every { charItemsCollection.addSnapshotListener(any<EventListener<QuerySnapshot>>()) } answers {
            firstArg<EventListener<QuerySnapshot>>().onEvent(snapshot, error)
            listenerReg
        }
    }

    private fun buildItemDocSnapshot(): DocumentSnapshot = mockk<DocumentSnapshot>().apply {
        every { getLong("id") } returns ITEM_ID
        every { getString("name") } returns "Healing Potion"
        every { getString("description") } returns "Restores 2d4+2 hit points."
        every { getLong("totalQuantity") } returns 3L
    }

    private fun buildCharacterItemDocSnapshot(): DocumentSnapshot = mockk<DocumentSnapshot>().apply {
        every { getLong("characterId") } returns CHARACTER_ID
        every { getLong("itemId") } returns ITEM_ID
        every { getLong("quantity") } returns 2L
    }
}






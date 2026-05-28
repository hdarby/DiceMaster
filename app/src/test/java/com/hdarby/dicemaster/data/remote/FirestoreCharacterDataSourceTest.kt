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
import com.hdarby.dicemaster.domain.model.Character
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private const val SESSION_ID = "session1"
private const val CHARACTER_ID_STR = "1"

class FirestoreCharacterDataSourceTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionsCollection: CollectionReference
    private lateinit var sessionDocument: DocumentReference
    private lateinit var charactersCollection: CollectionReference
    private lateinit var characterDocument: DocumentReference
    private lateinit var dataSource: FirestoreCharacterDataSource

    private val character = buildTestCharacter()

    @Before
    fun setUp() {
        firestore = mockk()
        sessionsCollection = mockk()
        sessionDocument = mockk()
        charactersCollection = mockk()
        characterDocument = mockk()

        every { firestore.collection("sessions") } returns sessionsCollection
        every { sessionsCollection.document(SESSION_ID) } returns sessionDocument
        every { sessionDocument.collection("characters") } returns charactersCollection
        every { charactersCollection.document(any()) } returns characterDocument

        dataSource = FirestoreCharacterDataSource(firestore)
    }

    // ── upsertCharacter ──────────────────────────────────────────────────────

    @Test
    fun `upsertCharacter writes correct fields to the character document`() = runTest {
        val dataSlot = io.mockk.slot<Any>()
        every { characterDocument.set(capture(dataSlot)) } returns voidTask()

        dataSource.upsertCharacter(SESSION_ID, character)

        @Suppress("UNCHECKED_CAST")
        val written = dataSlot.captured as Map<String, Any?>
        assertEquals(1L, written["id"])
        assertEquals("Grog", written["name"])
        assertEquals("Goliath", written["race"])
        assertEquals(1, written["level"])
        assertEquals(20, written["strength"])
        assertEquals(100, written["maxHitPoints"])
        assertEquals(80, written["currentHitPoints"])
        assertEquals(0, written["deathSaveFailures"])
        assertEquals(false, written["isDead"])
    }

    @Test
    fun `upsertCharacter targets the document keyed by character id`() = runTest {
        every { characterDocument.set(any()) } returns voidTask()

        dataSource.upsertCharacter(SESSION_ID, character)

        verify { charactersCollection.document(CHARACTER_ID_STR) }
    }

    // ── deleteCharacter ──────────────────────────────────────────────────────

    @Test
    fun `deleteCharacter deletes the document keyed by character id`() = runTest {
        every { characterDocument.delete() } returns voidTask()

        dataSource.deleteCharacter(SESSION_ID, character.id)

        verify { charactersCollection.document(CHARACTER_ID_STR) }
        verify { characterDocument.delete() }
    }

    // ── observeCharacters ────────────────────────────────────────────────────

    @Test
    fun `observeCharacters emits a mapped list of characters from the snapshot`() = runTest {
        val docSnapshot = buildCharacterDocSnapshot()
        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns listOf(docSnapshot)
        triggerSnapshotListener(querySnapshot, error = null)

        dataSource.observeCharacters(SESSION_ID).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(1L, result[0].id)
            assertEquals("Grog", result[0].name)
            assertEquals("Goliath", result[0].race)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeCharacters emits an empty list when the snapshot contains no documents`() = runTest {
        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns emptyList()
        triggerSnapshotListener(querySnapshot, error = null)

        dataSource.observeCharacters(SESSION_ID).test {
            assertEquals(emptyList<Character>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeCharacters filters out documents that are missing required fields`() = runTest {
        val validDoc = buildCharacterDocSnapshot()
        val invalidDoc = mockk<DocumentSnapshot>()
        every { invalidDoc.getLong("id") } returns null // missing required id -> filtered out

        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns listOf(validDoc, invalidDoc)
        triggerSnapshotListener(querySnapshot, error = null)

        dataSource.observeCharacters(SESSION_ID).test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeCharacters closes the flow when Firestore reports an error`() = runTest {
        val error = mockk<FirebaseFirestoreException>(relaxed = true)
        triggerSnapshotListener(snapshot = null, error = error)

        dataSource.observeCharacters(SESSION_ID).test {
            awaitError()
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Configures the mock so that [addSnapshotListener] immediately fires with the given args. */
    private fun triggerSnapshotListener(
        snapshot: QuerySnapshot?,
        error: FirebaseFirestoreException?
    ) {
        val listenerReg = mockk<ListenerRegistration>(relaxed = true)
        every { charactersCollection.addSnapshotListener(any<EventListener<QuerySnapshot>>()) } answers {
            firstArg<EventListener<QuerySnapshot>>().onEvent(snapshot, error)
            listenerReg
        }
    }

    private fun buildCharacterDocSnapshot(): DocumentSnapshot = mockk<DocumentSnapshot>().apply {
        every { getLong("id") } returns 1L
        every { getString("name") } returns "Grog"
        every { getString("race") } returns "Goliath"
        every { getString("characterClass") } returns null
        every { getLong("level") } returns 1L
        every { getLong("strength") } returns 20L
        every { getLong("strengthModifier") } returns 5L
        every { getLong("dexterity") } returns 12L
        every { getLong("dexterityModifier") } returns 1L
        every { getLong("constitution") } returns 18L
        every { getLong("constitutionModifier") } returns 4L
        every { getLong("intelligence") } returns 6L
        every { getLong("intelligenceModifier") } returns -2L
        every { getLong("wisdom") } returns 10L
        every { getLong("wisdomModifier") } returns 0L
        every { getLong("charisma") } returns 8L
        every { getLong("charismaModifier") } returns -1L
        every { getLong("maxHitPoints") } returns 100L
        every { getLong("currentHitPoints") } returns 80L
        every { getLong("deathSaveFailures") } returns 0L
        every { getBoolean("isDead") } returns false
    }

    private fun buildTestCharacter() = Character(
        id = 1L,
        name = "Grog",
        race = "Goliath",
        stats = com.hdarby.dicemaster.domain.model.Stats(
            strength = 20, strengthModifier = 5,
            dexterity = 12, dexterityModifier = 1,
            constitution = 18, constitutionModifier = 4,
            intelligence = 6, intelligenceModifier = -2,
            wisdom = 10, wisdomModifier = 0,
            charisma = 8, charismaModifier = -1
        ),
        maxHitPoints = 100,
        currentHitPoints = 80
    )
}




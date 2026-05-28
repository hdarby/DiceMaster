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
import com.hdarby.dicemaster.domain.model.DamageDice
import com.hdarby.dicemaster.domain.model.DamageType
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.domain.model.WeaponType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private const val SESSION_ID = "session1"

class FirestoreWeaponDataSourceTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var sessionsCollection: CollectionReference
    private lateinit var sessionDocument: DocumentReference
    private lateinit var weaponsCollection: CollectionReference
    private lateinit var weaponDocument: DocumentReference
    private lateinit var dataSource: FirestoreWeaponDataSource

    private val weapon = Weapon(
        id = 1L,
        name = "Greataxe",
        weaponType = WeaponType.MARTIAL_MELEE,
        damageDice = DamageDice.D12,
        damageType = DamageType.SLASHING,
        toHitBonus = 0,
        damageModifier = 2,
        isAtomic = true
    )

    @Before
    fun setUp() {
        firestore = mockk()
        sessionsCollection = mockk()
        sessionDocument = mockk()
        weaponsCollection = mockk()
        weaponDocument = mockk()

        every { firestore.collection("sessions") } returns sessionsCollection
        every { sessionsCollection.document(SESSION_ID) } returns sessionDocument
        every { sessionDocument.collection("weapons") } returns weaponsCollection
        every { weaponsCollection.document(any()) } returns weaponDocument

        dataSource = FirestoreWeaponDataSource(firestore)
    }

    // ── upsertWeapon ─────────────────────────────────────────────────────────

    @Test
    fun `upsertWeapon writes correct fields to the weapon document`() = runTest {
        val dataSlot = io.mockk.slot<Any>()
        every { weaponDocument.set(capture(dataSlot)) } returns voidTask()

        dataSource.upsertWeapon(SESSION_ID, weapon)

        @Suppress("UNCHECKED_CAST")
        val written = dataSlot.captured as Map<String, Any?>
        assertEquals(1L, written["id"])
        assertEquals("Greataxe", written["name"])
        assertEquals("MARTIAL_MELEE", written["type"])
        assertEquals("D12", written["damageDice"])
        assertEquals("SLASHING", written["damageType"])
        assertEquals(0, written["toHitBonus"])
        assertEquals(2, written["damageModifier"])
        assertEquals(true, written["isAtomic"])
    }

    @Test
    fun `upsertWeapon targets the document keyed by weapon id`() = runTest {
        every { weaponDocument.set(any()) } returns voidTask()

        dataSource.upsertWeapon(SESSION_ID, weapon)

        verify { weaponsCollection.document("1") }
    }

    // ── deleteWeapon ─────────────────────────────────────────────────────────

    @Test
    fun `deleteWeapon deletes the document keyed by weapon id`() = runTest {
        every { weaponDocument.delete() } returns voidTask()

        dataSource.deleteWeapon(SESSION_ID, weapon.id)

        verify { weaponsCollection.document("1") }
        verify { weaponDocument.delete() }
    }

    // ── observeWeapons ───────────────────────────────────────────────────────

    @Test
    fun `observeWeapons emits a mapped list of RemoteWeapon from the snapshot`() = runTest {
        val docSnapshot = buildWeaponDocSnapshot()
        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns listOf(docSnapshot)
        triggerSnapshotListener(querySnapshot, error = null)

        dataSource.observeWeapons(SESSION_ID).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(1L, result[0].weapon.id)
            assertEquals("Greataxe", result[0].weapon.name)
            assertEquals(WeaponType.MARTIAL_MELEE, result[0].weapon.weaponType)
            assertEquals(DamageDice.D12, result[0].weapon.damageDice)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeWeapons emits an empty list when the snapshot contains no documents`() = runTest {
        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns emptyList()
        triggerSnapshotListener(querySnapshot, error = null)

        dataSource.observeWeapons(SESSION_ID).test {
            assertEquals(emptyList<RemoteWeapon>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeWeapons filters out documents that are missing required fields`() = runTest {
        val invalidDoc = mockk<DocumentSnapshot>()
        every { invalidDoc.getLong("id") } returns null  // missing id -> filtered out

        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns listOf(invalidDoc)
        triggerSnapshotListener(querySnapshot, error = null)

        dataSource.observeWeapons(SESSION_ID).test {
            assertEquals(emptyList<RemoteWeapon>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeWeapons closes the flow when Firestore reports an error`() = runTest {
        val error = mockk<FirebaseFirestoreException>(relaxed = true)
        triggerSnapshotListener(snapshot = null, error = error)

        dataSource.observeWeapons(SESSION_ID).test {
            awaitError()
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun triggerSnapshotListener(
        snapshot: QuerySnapshot?,
        error: FirebaseFirestoreException?
    ) {
        val listenerReg = mockk<ListenerRegistration>(relaxed = true)
        every { weaponsCollection.addSnapshotListener(any<EventListener<QuerySnapshot>>()) } answers {
            firstArg<EventListener<QuerySnapshot>>().onEvent(snapshot, error)
            listenerReg
        }
    }

    private fun buildWeaponDocSnapshot(): DocumentSnapshot = mockk<DocumentSnapshot>().apply {
        every { getLong("id") } returns 1L
        every { getString("name") } returns "Greataxe"
        every { getString("type") } returns "MARTIAL_MELEE"
        every { getString("damageDice") } returns "D12"
        every { getString("damageType") } returns "SLASHING"
        every { getLong("toHitBonus") } returns 0L
        every { getLong("damageModifier") } returns 2L
        every { getBoolean("isAtomic") } returns true
    }
}



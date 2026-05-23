package com.hdarby.dicemaster.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.hdarby.dicemaster.domain.model.ConsumableItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreItemDataSource(
    private val firestore: FirebaseFirestore
) : ItemRemoteDataSource {

    // ── Item definitions ──────────────────────────────────────────────────────

    override suspend fun upsertItem(sessionId: String, item: ConsumableItem) {
        val data = mapOf(
            FIELD_ID to item.id,
            FIELD_NAME to item.name,
            FIELD_DESCRIPTION to item.description,
            FIELD_TOTAL_QUANTITY to item.totalQuantity
        )
        itemsCollection(sessionId).document(item.id.toString()).set(data).await()
    }

    override suspend fun deleteItem(sessionId: String, itemId: Long) {
        itemsCollection(sessionId).document(itemId.toString()).delete().await()
    }

    override fun observeItems(sessionId: String): Flow<List<ConsumableItem>> = callbackFlow {
        val listener = itemsCollection(sessionId).addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val items = snapshot?.documents?.mapNotNull { it.toConsumableItem() } ?: emptyList()
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

    // ── Character–item cross-refs ─────────────────────────────────────────────

    override suspend fun upsertCharacterItem(sessionId: String, characterId: Long, itemId: Long, quantity: Int) {
        val docId = crossRefDocId(characterId, itemId)
        val data = mapOf(
            FIELD_CHARACTER_ID to characterId,
            FIELD_ITEM_ID to itemId,
            FIELD_QUANTITY to quantity
        )
        characterItemsCollection(sessionId).document(docId).set(data).await()
    }

    override suspend fun deleteCharacterItem(sessionId: String, characterId: Long, itemId: Long) {
        characterItemsCollection(sessionId).document(crossRefDocId(characterId, itemId)).delete().await()
    }

    override suspend fun updateCharacterItemQuantity(sessionId: String, characterId: Long, itemId: Long, quantity: Int) {
        val docId = crossRefDocId(characterId, itemId)
        characterItemsCollection(sessionId).document(docId)
            .update(mapOf(FIELD_QUANTITY to quantity)).await()
    }

    override fun observeCharacterItems(sessionId: String): Flow<List<RemoteCharacterItem>> = callbackFlow {
        val listener = characterItemsCollection(sessionId).addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            val refs = snapshot?.documents?.mapNotNull { it.toRemoteCharacterItem() } ?: emptyList()
            trySend(refs)
        }
        awaitClose { listener.remove() }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun itemsCollection(sessionId: String) =
        firestore.collection(COLLECTION_SESSIONS).document(sessionId).collection(COLLECTION_ITEMS)

    private fun characterItemsCollection(sessionId: String) =
        firestore.collection(COLLECTION_SESSIONS).document(sessionId).collection(COLLECTION_CHARACTER_ITEMS)

    private fun crossRefDocId(characterId: Long, itemId: Long) = "${characterId}_${itemId}"

    private fun com.google.firebase.firestore.DocumentSnapshot.toConsumableItem(): ConsumableItem? {
        val id = getLong(FIELD_ID) ?: return null
        val name = getString(FIELD_NAME) ?: return null
        val description = getString(FIELD_DESCRIPTION) ?: return null
        val totalQuantity = getLong(FIELD_TOTAL_QUANTITY)?.toInt() ?: 1
        return ConsumableItem(id = id, name = name, description = description, totalQuantity = totalQuantity)
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toRemoteCharacterItem(): RemoteCharacterItem? {
        val characterId = getLong(FIELD_CHARACTER_ID) ?: return null
        val itemId = getLong(FIELD_ITEM_ID) ?: return null
        val quantity = getLong(FIELD_QUANTITY)?.toInt() ?: return null
        return RemoteCharacterItem(characterId = characterId, itemId = itemId, quantity = quantity)
    }

    companion object {
        private const val COLLECTION_SESSIONS = "sessions"
        private const val COLLECTION_ITEMS = "items"
        private const val COLLECTION_CHARACTER_ITEMS = "characterItems"
        private const val FIELD_ID = "id"
        private const val FIELD_NAME = "name"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_TOTAL_QUANTITY = "totalQuantity"
        private const val FIELD_CHARACTER_ID = "characterId"
        private const val FIELD_ITEM_ID = "itemId"
        private const val FIELD_QUANTITY = "quantity"
    }
}


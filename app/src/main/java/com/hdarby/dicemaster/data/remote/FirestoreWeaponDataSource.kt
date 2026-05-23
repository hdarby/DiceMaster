package com.hdarby.dicemaster.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.hdarby.dicemaster.domain.model.Weapon
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreWeaponDataSource(
    private val firestore: FirebaseFirestore
) : WeaponRemoteDataSource {

    override suspend fun upsertWeapon(sessionId: String, weapon: Weapon) {
        weaponsCollection(sessionId).document(weapon.id.toString()).set(weapon.toMap()).await()
    }

    override suspend fun deleteWeapon(sessionId: String, weaponId: Long) {
        weaponsCollection(sessionId).document(weaponId.toString()).delete().await()
    }

    override suspend fun updateWeaponAssignment(sessionId: String, weaponId: Long, characterId: Long?) {
        val update = mapOf(FIELD_CHARACTER_ID to characterId)
        weaponsCollection(sessionId).document(weaponId.toString()).update(update).await()
    }

    override fun observeWeapons(sessionId: String): Flow<List<RemoteWeapon>> = callbackFlow {
        val listener = weaponsCollection(sessionId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val weapons = snapshot?.documents?.mapNotNull { it.toRemoteWeapon() } ?: emptyList()
            trySend(weapons)
        }
        awaitClose { listener.remove() }
    }

    private fun weaponsCollection(sessionId: String) =
        firestore.collection(COLLECTION_SESSIONS).document(sessionId).collection(COLLECTION_WEAPONS)

    private fun Weapon.toMap(): Map<String, Any?> = mapOf(
        FIELD_ID to id,
        FIELD_NAME to name,
        FIELD_TYPE to type,
        FIELD_DAMAGE_DICE to damageDice,
        FIELD_DAMAGE_TYPE to damageType,
        FIELD_MODIFIER to modifier,
        FIELD_CHARACTER_ID to null
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toRemoteWeapon(): RemoteWeapon? {
        val id = getLong(FIELD_ID) ?: return null
        val name = getString(FIELD_NAME) ?: return null
        val type = getString(FIELD_TYPE) ?: return null
        val damageDice = getString(FIELD_DAMAGE_DICE) ?: return null
        val damageType = getString(FIELD_DAMAGE_TYPE) ?: return null
        val modifier = getLong(FIELD_MODIFIER)?.toInt() ?: 0
        val characterId = getLong(FIELD_CHARACTER_ID)
        return RemoteWeapon(
            weapon = Weapon(id = id, name = name, type = type, damageDice = damageDice, damageType = damageType, modifier = modifier),
            characterId = characterId
        )
    }

    companion object {
        private const val COLLECTION_SESSIONS = "sessions"
        private const val COLLECTION_WEAPONS = "weapons"
        private const val FIELD_ID = "id"
        private const val FIELD_NAME = "name"
        private const val FIELD_TYPE = "type"
        private const val FIELD_DAMAGE_DICE = "damageDice"
        private const val FIELD_DAMAGE_TYPE = "damageType"
        private const val FIELD_MODIFIER = "modifier"
        private const val FIELD_CHARACTER_ID = "characterId"
    }
}


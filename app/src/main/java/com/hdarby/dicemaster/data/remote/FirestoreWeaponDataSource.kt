package com.hdarby.dicemaster.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.hdarby.dicemaster.domain.model.DamageDice
import com.hdarby.dicemaster.domain.model.DamageType
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.domain.model.WeaponType
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
        FIELD_TYPE to weaponType.name,
        FIELD_DAMAGE_DICE to damageDice.name,
        FIELD_DAMAGE_TYPE to damageType.name,
        FIELD_TO_HIT_BONUS to toHitBonus,
        FIELD_DAMAGE_MODIFIER to damageModifier,
        FIELD_IS_ATOMIC to isAtomic
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toRemoteWeapon(): RemoteWeapon? {
        val id = getLong(FIELD_ID) ?: return null
        val name = getString(FIELD_NAME) ?: return null
        return RemoteWeapon(
            weapon = Weapon(
                id = id,
                name = name,
                weaponType = getString(FIELD_TYPE)
                    ?.let { runCatching { WeaponType.valueOf(it) }.getOrNull() }
                    ?: WeaponType.SIMPLE_MELEE,
                damageDice = getString(FIELD_DAMAGE_DICE)
                    ?.let { runCatching { DamageDice.valueOf(it) }.getOrNull() }
                    ?: DamageDice.D6,
                damageType = getString(FIELD_DAMAGE_TYPE)
                    ?.let { runCatching { DamageType.valueOf(it) }.getOrNull() }
                    ?: DamageType.SLASHING,
                toHitBonus = getLong(FIELD_TO_HIT_BONUS)?.toInt() ?: 0,
                damageModifier = getLong(FIELD_DAMAGE_MODIFIER)?.toInt() ?: 0,
                isAtomic = getBoolean(FIELD_IS_ATOMIC) ?: true
            )
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
        private const val FIELD_TO_HIT_BONUS = "toHitBonus"
        private const val FIELD_DAMAGE_MODIFIER = "damageModifier"
        private const val FIELD_IS_ATOMIC = "isAtomic"
    }
}

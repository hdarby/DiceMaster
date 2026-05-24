package com.hdarby.dicemaster.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.Stats
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreCharacterDataSource(
    private val firestore: FirebaseFirestore
) : CharacterRemoteDataSource {

    override suspend fun upsertCharacter(sessionId: String, character: Character) {
        val data = mapOf(
            FIELD_ID to character.id,
            FIELD_NAME to character.name,
            FIELD_RACE to character.race,
            FIELD_STRENGTH to character.stats.strength,
            FIELD_STRENGTH_MOD to character.stats.strengthModifier,
            FIELD_DEXTERITY to character.stats.dexterity,
            FIELD_DEXTERITY_MOD to character.stats.dexterityModifier,
            FIELD_CONSTITUTION to character.stats.constitution,
            FIELD_CONSTITUTION_MOD to character.stats.constitutionModifier,
            FIELD_INTELLIGENCE to character.stats.intelligence,
            FIELD_INTELLIGENCE_MOD to character.stats.intelligenceModifier,
            FIELD_WISDOM to character.stats.wisdom,
            FIELD_WISDOM_MOD to character.stats.wisdomModifier,
            FIELD_CHARISMA to character.stats.charisma,
            FIELD_CHARISMA_MOD to character.stats.charismaModifier
        )
        charactersCollection(sessionId).document(character.id.toString()).set(data).await()
    }

    override suspend fun deleteCharacter(sessionId: String, characterId: Long) {
        charactersCollection(sessionId).document(characterId.toString()).delete().await()
    }

    override fun observeCharacters(sessionId: String): Flow<List<Character>> = callbackFlow {
        val listener = charactersCollection(sessionId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val characters = snapshot?.documents?.mapNotNull { it.toCharacter() } ?: emptyList()
            trySend(characters)
        }
        awaitClose { listener.remove() }
    }

    private fun charactersCollection(sessionId: String) =
        firestore.collection(COLLECTION_SESSIONS).document(sessionId).collection(COLLECTION_CHARACTERS)

    private fun com.google.firebase.firestore.DocumentSnapshot.toCharacter(): Character? {
        val id = getLong(FIELD_ID) ?: return null
        val name = getString(FIELD_NAME) ?: return null
        val race = getString(FIELD_RACE) ?: return null
        return Character(
            id = id,
            name = name,
            race = race,
            stats = Stats(
                strength = getLong(FIELD_STRENGTH)?.toInt() ?: 10,
                strengthModifier = getLong(FIELD_STRENGTH_MOD)?.toInt() ?: 0,
                dexterity = getLong(FIELD_DEXTERITY)?.toInt() ?: 10,
                dexterityModifier = getLong(FIELD_DEXTERITY_MOD)?.toInt() ?: 0,
                constitution = getLong(FIELD_CONSTITUTION)?.toInt() ?: 10,
                constitutionModifier = getLong(FIELD_CONSTITUTION_MOD)?.toInt() ?: 0,
                intelligence = getLong(FIELD_INTELLIGENCE)?.toInt() ?: 10,
                intelligenceModifier = getLong(FIELD_INTELLIGENCE_MOD)?.toInt() ?: 0,
                wisdom = getLong(FIELD_WISDOM)?.toInt() ?: 10,
                wisdomModifier = getLong(FIELD_WISDOM_MOD)?.toInt() ?: 0,
                charisma = getLong(FIELD_CHARISMA)?.toInt() ?: 10,
                charismaModifier = getLong(FIELD_CHARISMA_MOD)?.toInt() ?: 0
            ),
            maxHitPoints = getLong(FIELD_MAX_HIT_POINTS)?.toInt() ?: 10,
            currentHitPoints = getLong(FIELD_CURRENT_HIT_POINTS)?.toInt() ?: 10,
            deathSaveFailures = getLong(FIELD_DEATH_SAVE_FAILURES)?.toInt() ?: 0,
            isDead = getBoolean(FIELD_IS_DEAD) ?: false
        )
    }

    companion object {
        private const val COLLECTION_SESSIONS = "sessions"
        private const val COLLECTION_CHARACTERS = "characters"
        private const val FIELD_ID = "id"
        private const val FIELD_NAME = "name"
        private const val FIELD_RACE = "race"
        private const val FIELD_STRENGTH = "strength"
        private const val FIELD_STRENGTH_MOD = "strengthModifier"
        private const val FIELD_DEXTERITY = "dexterity"
        private const val FIELD_DEXTERITY_MOD = "dexterityModifier"
        private const val FIELD_CONSTITUTION = "constitution"
        private const val FIELD_CONSTITUTION_MOD = "constitutionModifier"
        private const val FIELD_INTELLIGENCE = "intelligence"
        private const val FIELD_INTELLIGENCE_MOD = "intelligenceModifier"
        private const val FIELD_WISDOM = "wisdom"
        private const val FIELD_WISDOM_MOD = "wisdomModifier"
        private const val FIELD_CHARISMA = "charisma"
        private const val FIELD_CHARISMA_MOD = "charismaModifier"
        private const val FIELD_MAX_HIT_POINTS = "maxHitPoints"
        private const val FIELD_CURRENT_HIT_POINTS = "currentHitPoints"
        private const val FIELD_DEATH_SAVE_FAILURES = "deathSaveFailures"
        private const val FIELD_IS_DEAD = "isDead"
    }
}



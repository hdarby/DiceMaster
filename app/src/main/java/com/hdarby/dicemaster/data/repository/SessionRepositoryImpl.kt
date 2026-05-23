package com.hdarby.dicemaster.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hdarby.dicemaster.domain.model.Session
import com.hdarby.dicemaster.domain.model.UserRole
import com.hdarby.dicemaster.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SessionRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SessionRepository {

    override suspend fun getActiveSession(): Session? {
        val prefs = dataStore.data.first()
        val sessionId = prefs[KEY_SESSION_ID] ?: return null
        val roleType = prefs[KEY_ROLE_TYPE] ?: return null
        val role: UserRole = when (roleType) {
            ROLE_TYPE_DM -> UserRole.DungeonMaster
            ROLE_TYPE_PLAYER -> {
                val characterId = prefs[KEY_ROLE_CHARACTER_ID] ?: return null
                UserRole.Player(characterId)
            }
            else -> return null
        }
        return Session(sessionId, role)
    }

    override fun observeSession(): Flow<Session?> = dataStore.data.map { prefs ->
        val sessionId = prefs[KEY_SESSION_ID] ?: return@map null
        val roleType = prefs[KEY_ROLE_TYPE] ?: return@map null
        val role: UserRole = when (roleType) {
            ROLE_TYPE_DM -> UserRole.DungeonMaster
            ROLE_TYPE_PLAYER -> {
                val characterId = prefs[KEY_ROLE_CHARACTER_ID] ?: return@map null
                UserRole.Player(characterId)
            }
            else -> return@map null
        }
        Session(sessionId, role)
    }

    override suspend fun saveSession(session: Session) {
        dataStore.edit { prefs ->
            prefs[KEY_SESSION_ID] = session.sessionId
            prefs[KEY_ROLE_TYPE] = when (session.role) {
                is UserRole.DungeonMaster -> ROLE_TYPE_DM
                is UserRole.Player -> ROLE_TYPE_PLAYER
            }
            when (val role = session.role) {
                is UserRole.Player -> prefs[KEY_ROLE_CHARACTER_ID] = role.characterId
                is UserRole.DungeonMaster -> prefs.remove(KEY_ROLE_CHARACTER_ID)
            }
        }
    }

    override suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }

    companion object {
        private val KEY_SESSION_ID = stringPreferencesKey("session_id")
        private val KEY_ROLE_TYPE = stringPreferencesKey("role_type")
        private val KEY_ROLE_CHARACTER_ID = longPreferencesKey("role_character_id")
        private const val ROLE_TYPE_DM = "DM"
        private const val ROLE_TYPE_PLAYER = "PLAYER"
    }
}

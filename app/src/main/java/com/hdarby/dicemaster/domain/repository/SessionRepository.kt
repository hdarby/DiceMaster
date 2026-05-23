package com.hdarby.dicemaster.domain.repository

import com.hdarby.dicemaster.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun getActiveSession(): Session?
    suspend fun saveSession(session: Session)
    suspend fun clearSession()
    fun observeSession(): Flow<Session?>
}



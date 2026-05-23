package com.hdarby.dicemaster.data.remote

interface SessionRemoteDataSource {
    suspend fun createSession(sessionId: String, createdByUid: String)
    suspend fun sessionExists(sessionId: String): Boolean
}


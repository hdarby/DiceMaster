package com.hdarby.dicemaster.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreSessionDataSource(
    private val firestore: FirebaseFirestore
) : SessionRemoteDataSource {

    override suspend fun createSession(sessionId: String, createdByUid: String) {
        val data = mapOf(
            FIELD_CREATED_BY to createdByUid,
            FIELD_CREATED_AT to System.currentTimeMillis()
        )
        firestore.collection(COLLECTION_SESSIONS).document(sessionId).set(data).await()
    }

    override suspend fun sessionExists(sessionId: String): Boolean {
        val snapshot = firestore.collection(COLLECTION_SESSIONS).document(sessionId).get().await()
        return snapshot.exists()
    }

    companion object {
        private const val COLLECTION_SESSIONS = "sessions"
        private const val FIELD_CREATED_BY = "createdBy"
        private const val FIELD_CREATED_AT = "createdAt"
    }
}


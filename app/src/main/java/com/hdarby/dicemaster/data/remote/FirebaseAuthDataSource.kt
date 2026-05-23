package com.hdarby.dicemaster.data.remote

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthDataSource(private val auth: FirebaseAuth) {

    val currentUid: String?
        get() = auth.currentUser?.uid

    suspend fun signInAnonymously(): String {
        val result = auth.signInAnonymously().await()
        return result.user?.uid
            ?: throw IllegalStateException("Anonymous sign-in succeeded but UID was null")
    }

    suspend fun ensureSignedIn(): String = currentUid ?: signInAnonymously()
}


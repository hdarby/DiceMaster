package com.hdarby.dicemaster.data.remote

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import io.mockk.every
import io.mockk.mockk

/**
 * Creates a completed [Task<Void>] suitable for mocking Firebase write operations
 * (`set`, `delete`, `update`).  The [kotlinx.coroutines.tasks.await] extension takes
 * the fast path when [Task.isComplete] is true, so no listener wiring is required.
 */
internal fun voidTask(): Task<Void> = mockk<Task<Void>>().apply {
    every { isComplete } returns true
    every { isSuccessful } returns true
    every { isCanceled } returns false
    every { result } returns null
    every { exception } returns null
}

/**
 * Creates a completed [Task<DocumentSnapshot>] suitable for mocking Firebase read operations
 * (`get`).
 */
internal fun documentSnapshotTask(snapshot: DocumentSnapshot): Task<DocumentSnapshot> =
    mockk<Task<DocumentSnapshot>>().apply {
        every { isComplete } returns true
        every { isSuccessful } returns true
        every { isCanceled } returns false
        every { result } returns snapshot
        every { exception } returns null
    }


package com.hdarby.dicemaster.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [DataStore] implementation for use in unit tests.
 *
 * The [edit] extension function calls [updateData] internally, so implementing
 * [updateData] here is sufficient to support all DataStore preference operations.
 */
internal class FakePreferencesDataStore : DataStore<Preferences> {

    private val _data = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = _data

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(_data.value)
        _data.value = updated
        return updated
    }
}


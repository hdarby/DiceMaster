package com.hdarby.dicemaster.data.remote

import com.hdarby.dicemaster.domain.model.Weapon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeWeaponRemoteDataSource : WeaponRemoteDataSource {

    private val _weapons = MutableStateFlow<List<RemoteWeapon>>(emptyList())
    val weapons: List<RemoteWeapon> get() = _weapons.value

    var upsertCallCount = 0
    var deleteCallCount = 0
    var lastUpsertedSessionId: String? = null

    override suspend fun upsertWeapon(sessionId: String, weapon: Weapon) {
        upsertCallCount++
        lastUpsertedSessionId = sessionId
        val current = _weapons.value.toMutableList()
        val existing = current.indexOfFirst { it.weapon.id == weapon.id }
        if (existing >= 0) current[existing] = RemoteWeapon(weapon)
        else current.add(RemoteWeapon(weapon))
        _weapons.value = current
    }

    override suspend fun deleteWeapon(sessionId: String, weaponId: Long) {
        deleteCallCount++
        _weapons.value = _weapons.value.filter { it.weapon.id != weaponId }
    }

    override fun observeWeapons(sessionId: String): Flow<List<RemoteWeapon>> = _weapons.asStateFlow()

    /** Simulate a remote device pushing a weapon update. */
    fun simulateRemoteUpdate(vararg remoteWeapons: RemoteWeapon) {
        _weapons.value = remoteWeapons.toList()
    }
}



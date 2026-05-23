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
    var assignmentCallCount = 0
    var lastUpsertedSessionId: String? = null
    var lastAssignment: Triple<String, Long, Long?>? = null  // sessionId, weaponId, characterId

    override suspend fun upsertWeapon(sessionId: String, weapon: Weapon) {
        upsertCallCount++
        lastUpsertedSessionId = sessionId
        val current = _weapons.value.toMutableList()
        val existing = current.indexOfFirst { it.weapon.id == weapon.id }
        val characterId = if (existing >= 0) current[existing].characterId else null
        if (existing >= 0) current[existing] = RemoteWeapon(weapon, characterId)
        else current.add(RemoteWeapon(weapon, null))
        _weapons.value = current
    }

    override suspend fun deleteWeapon(sessionId: String, weaponId: Long) {
        deleteCallCount++
        _weapons.value = _weapons.value.filter { it.weapon.id != weaponId }
    }

    override suspend fun updateWeaponAssignment(sessionId: String, weaponId: Long, characterId: Long?) {
        assignmentCallCount++
        lastAssignment = Triple(sessionId, weaponId, characterId)
        val current = _weapons.value.toMutableList()
        val idx = current.indexOfFirst { it.weapon.id == weaponId }
        if (idx >= 0) current[idx] = current[idx].copy(characterId = characterId)
        _weapons.value = current
    }

    override fun observeWeapons(sessionId: String): Flow<List<RemoteWeapon>> = _weapons.asStateFlow()

    /** Simulate a remote device pushing a weapon update. */
    fun simulateRemoteUpdate(vararg remoteWeapons: RemoteWeapon) {
        _weapons.value = remoteWeapons.toList()
    }
}


package com.hdarby.dicemaster.data.remote

import com.hdarby.dicemaster.domain.model.Weapon
import kotlinx.coroutines.flow.Flow

interface WeaponRemoteDataSource {
    suspend fun upsertWeapon(sessionId: String, weapon: Weapon)
    suspend fun deleteWeapon(sessionId: String, weaponId: Long)
    suspend fun updateWeaponAssignment(sessionId: String, weaponId: Long, characterId: Long?)
    fun observeWeapons(sessionId: String): Flow<List<RemoteWeapon>>
}


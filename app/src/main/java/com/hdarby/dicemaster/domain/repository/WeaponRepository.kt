package com.hdarby.dicemaster.domain.repository

import com.hdarby.dicemaster.domain.model.Weapon
import kotlinx.coroutines.flow.Flow

interface WeaponRepository {
    fun getAllWeapons(): Flow<List<Weapon>>
    suspend fun addWeapon(weapon: Weapon): Long
    suspend fun updateWeapon(weapon: Weapon)
    suspend fun deleteWeapon(weapon: Weapon)
}

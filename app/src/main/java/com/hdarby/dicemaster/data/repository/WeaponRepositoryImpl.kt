package com.hdarby.dicemaster.data.repository

import com.hdarby.dicemaster.data.local.dao.WeaponDao
import com.hdarby.dicemaster.data.local.entity.WeaponEntity
import com.hdarby.dicemaster.data.remote.WeaponRemoteDataSource
import com.hdarby.dicemaster.domain.model.DamageDice
import com.hdarby.dicemaster.domain.model.DamageType
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.domain.model.WeaponType
import com.hdarby.dicemaster.domain.repository.SessionRepository
import com.hdarby.dicemaster.domain.repository.WeaponRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WeaponRepositoryImpl(
    private val weaponDao: WeaponDao,
    private val sessionRepository: SessionRepository? = null,
    private val weaponRemoteDataSource: WeaponRemoteDataSource? = null,
    private val externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : WeaponRepository {

    init {
        if (sessionRepository != null && weaponRemoteDataSource != null) {
            startRemoteSync()
        }
    }

    private fun startRemoteSync() {
        externalScope.launch {
            sessionRepository!!.observeSession().collectLatest { session ->
                if (session == null) return@collectLatest
                weaponRemoteDataSource!!.observeWeapons(session.sessionId).collect { remoteWeapons ->
                    remoteWeapons.forEach { remoteWeapon ->
                        weaponDao.insertWeapon(remoteWeapon.weapon.toEntity())
                    }
                }
            }
        }
    }

    override fun getAllWeapons(): Flow<List<Weapon>> =
        weaponDao.getAllWeapons().map { it.map { entity -> entity.toDomain() } }

    override suspend fun addWeapon(weapon: Weapon): Long {
        val localId = weaponDao.insertWeapon(weapon.toEntity())
        sessionRepository?.getActiveSession()?.let { session ->
            weaponRemoteDataSource?.upsertWeapon(session.sessionId, weapon.copy(id = localId))
        }
        return localId
    }

    override suspend fun updateWeapon(weapon: Weapon) {
        weaponDao.updateWeapon(weapon.toEntity())
        sessionRepository?.getActiveSession()?.let { session ->
            weaponRemoteDataSource?.upsertWeapon(session.sessionId, weapon)
        }
    }

    override suspend fun deleteWeapon(weapon: Weapon) {
        weaponDao.deleteWeapon(weapon.toEntity())
        sessionRepository?.getActiveSession()?.let { session ->
            weaponRemoteDataSource?.deleteWeapon(session.sessionId, weapon.id)
        }
    }

    private fun WeaponEntity.toDomain() = Weapon(
        id = id,
        name = name,
        weaponType = runCatching { WeaponType.valueOf(type) }.getOrDefault(WeaponType.SIMPLE_MELEE),
        damageDice = runCatching { DamageDice.valueOf(damageDice) }.getOrDefault(DamageDice.D6),
        damageType = runCatching { DamageType.valueOf(damageType) }.getOrDefault(DamageType.SLASHING),
        toHitBonus = toHitBonus,
        damageModifier = damageModifier,
        isAtomic = isAtomic
    )

    private fun Weapon.toEntity() = WeaponEntity(
        id = id,
        name = name,
        type = weaponType.name,
        damageDice = damageDice.name,
        damageType = damageType.name,
        toHitBonus = toHitBonus,
        damageModifier = damageModifier,
        isAtomic = isAtomic
    )
}

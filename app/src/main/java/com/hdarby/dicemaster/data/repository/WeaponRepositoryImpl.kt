package com.hdarby.dicemaster.data.repository

import com.hdarby.dicemaster.data.local.dao.WeaponDao
import com.hdarby.dicemaster.data.local.entity.WeaponEntity
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.domain.repository.WeaponRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WeaponRepositoryImpl(private val weaponDao: WeaponDao) : WeaponRepository {
    override fun getAllWeapons(): Flow<List<Weapon>> {
        return weaponDao.getAllWeapons().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addWeapon(weapon: Weapon): Long {
        return weaponDao.insertWeapon(weapon.toEntity())
    }

    override suspend fun updateWeapon(weapon: Weapon) {
        weaponDao.updateWeapon(weapon.toEntity())
    }

    override suspend fun deleteWeapon(weapon: Weapon) {
        weaponDao.deleteWeapon(weapon.toEntity())
    }

    private fun WeaponEntity.toDomain() = Weapon(
        id = id,
        name = name,
        damageDice = damageDice,
        damageType = damageType,
        modifier = modifier
    )

    private fun Weapon.toEntity() = WeaponEntity(
        id = id,
        name = name,
        damageDice = damageDice,
        damageType = damageType,
        modifier = modifier
    )
}

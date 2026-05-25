package com.hdarby.dicemaster.data.repository

import com.hdarby.dicemaster.data.local.dao.WeaponDao
import com.hdarby.dicemaster.domain.model.Weapon
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WeaponRepositoryErrorHandlingTest {

    private val weaponDao: WeaponDao = mockk()
    private val repository = WeaponRepositoryImpl(weaponDao)

    private val weapon = Weapon(
        id = 1,
        name = "Longsword",
        weaponType = com.hdarby.dicemaster.domain.model.WeaponType.MARTIAL_MELEE,
        damageDice = com.hdarby.dicemaster.domain.model.DamageDice.D8,
        damageType = com.hdarby.dicemaster.domain.model.DamageType.SLASHING,
        damageModifier = 2
    )

    @Test
    fun `getAllWeapons handles database error`() = runTest {
        val errorMessage = "Database connection lost"
        every { weaponDao.getAllWeapons() } returns flow {
            throw Exception(errorMessage)
        }

        try {
            repository.getAllWeapons().collect { }
        } catch (e: Exception) {
            assertEquals(errorMessage, e.message)
        }
    }

    @Test
    fun `addWeapon handles constraint violation on duplicate name`() = runTest {
        val errorMessage = "UNIQUE constraint failed: weapon.name"
        coEvery { weaponDao.insertWeapon(any()) } throws Exception(errorMessage)

        try {
            repository.addWeapon(weapon)
        } catch (e: Exception) {
            assertEquals(errorMessage, e.message)
        }

        coVerify { weaponDao.insertWeapon(any()) }
    }

    @Test
    fun `updateWeapon delegates to dao`() = runTest {
        coEvery { weaponDao.updateWeapon(any()) } returns Unit

        repository.updateWeapon(weapon)

        coVerify { weaponDao.updateWeapon(any()) }
    }

    @Test
    fun `deleteWeapon removes weapon successfully`() = runTest {
        coEvery { weaponDao.deleteWeapon(any()) } returns Unit

        repository.deleteWeapon(weapon)

        coVerify { weaponDao.deleteWeapon(any()) }
    }

    @Test
    fun `getAllWeapons with empty database returns empty flow`() = runTest {
        every { weaponDao.getAllWeapons() } returns flow {
            emit(emptyList())
        }

        val result = mutableListOf<Any>()
        repository.getAllWeapons().collect { result.add(it) }

        assertEquals(1, result.size)
        assertEquals(emptyList<Any>(), result[0])
    }

    @Test
    fun `addWeapon with high modifier value`() = runTest {
        val extremeWeapon = weapon.copy(damageModifier = 999)
        coEvery { weaponDao.insertWeapon(any()) } returns 1L

        val weaponId = repository.addWeapon(extremeWeapon)

        coVerify { weaponDao.insertWeapon(any()) }
        assertEquals(1L, weaponId)
    }

    @Test
    fun `updateWeapon handles negative modifier`() = runTest {
        val negativeModWeapon = weapon.copy(damageModifier = -5)
        coEvery { weaponDao.updateWeapon(any()) } returns Unit

        repository.updateWeapon(negativeModWeapon)

        coVerify { weaponDao.updateWeapon(any()) }
    }
}


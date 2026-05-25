package com.hdarby.dicemaster.data.repository

import app.cash.turbine.test
import com.hdarby.dicemaster.data.local.dao.WeaponDao
import com.hdarby.dicemaster.data.local.entity.WeaponEntity
import com.hdarby.dicemaster.domain.model.Weapon
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WeaponRepositoryImplTest {

    private val weaponDao: WeaponDao = mockk()
    private val repository = WeaponRepositoryImpl(weaponDao)

    private val weaponEntity = WeaponEntity(
        id = 1, name = "Greataxe",
        type = "MARTIAL_MELEE", damageDice = "D12", damageType = "SLASHING",
        toHitBonus = 0, damageModifier = 2
    )
    private val weapon = Weapon(
        id = 1, name = "Greataxe",
        weaponType = com.hdarby.dicemaster.domain.model.WeaponType.MARTIAL_MELEE,
        damageDice = com.hdarby.dicemaster.domain.model.DamageDice.D12,
        damageType = com.hdarby.dicemaster.domain.model.DamageType.SLASHING,
        toHitBonus = 0, damageModifier = 2
    )

    @Test
    fun `getAllWeapons returns domain models`() = runTest {
        every { weaponDao.getAllWeapons() } returns flowOf(listOf(weaponEntity))

        repository.getAllWeapons().test {
            assertEquals(listOf(weapon), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `addWeapon inserts entity`() = runTest {
        coEvery { weaponDao.insertWeapon(any()) } returns 1L

        val result = repository.addWeapon(weapon)

        assertEquals(1L, result)
        coVerify { weaponDao.insertWeapon(match { it.name == "Greataxe" }) }
    }

    @Test
    fun `updateWeapon updates entity`() = runTest {
        coEvery { weaponDao.updateWeapon(any()) } returns Unit

        repository.updateWeapon(weapon)

        coVerify { weaponDao.updateWeapon(match { it.name == "Greataxe" }) }
    }

    @Test
    fun `deleteWeapon deletes entity`() = runTest {
        coEvery { weaponDao.deleteWeapon(any()) } returns Unit

        repository.deleteWeapon(weapon)

        coVerify { weaponDao.deleteWeapon(match { it.name == "Greataxe" }) }
    }
}

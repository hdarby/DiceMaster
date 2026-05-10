package com.hdarby.dicemaster.domain.usecase.weapon

import app.cash.turbine.test
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.domain.repository.WeaponRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WeaponUseCasesTest {

    private val repository: WeaponRepository = mockk()
    
    private val weapon = Weapon(1, "Greataxe", "Greataxe", "1d12", "Slashing", 2)

    @Test
    fun `GetWeaponsUseCase returns weapons from repository`() = runTest {
        val useCase = GetWeaponsUseCase(repository)
        every { repository.getAllWeapons() } returns flowOf(listOf(weapon))

        useCase().test {
            assertEquals(listOf(weapon), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `AddWeaponUseCase calls repository`() = runTest {
        val useCase = AddWeaponUseCase(repository)
        coEvery { repository.addWeapon(weapon) } returns 1L

        val result = useCase(weapon)

        assertEquals(1L, result)
        coVerify { repository.addWeapon(weapon) }
    }

    @Test
    fun `UpdateWeaponUseCase calls repository`() = runTest {
        val useCase = UpdateWeaponUseCase(repository)
        coEvery { repository.updateWeapon(weapon) } returns Unit

        useCase(weapon)

        coVerify { repository.updateWeapon(weapon) }
    }

    @Test
    fun `DeleteWeaponUseCase calls repository`() = runTest {
        val useCase = DeleteWeaponUseCase(repository)
        coEvery { repository.deleteWeapon(weapon) } returns Unit

        useCase(weapon)

        coVerify { repository.deleteWeapon(weapon) }
    }
}

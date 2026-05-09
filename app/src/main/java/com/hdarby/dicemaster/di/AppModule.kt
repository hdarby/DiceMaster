package com.hdarby.dicemaster.di

import com.hdarby.dicemaster.data.DiceRepository
import com.hdarby.dicemaster.data.DiceRepositoryImpl
import com.hdarby.dicemaster.domain.usecase.RollDiceUseCase
import com.hdarby.dicemaster.viewmodel.DiceViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<DiceRepository> { DiceRepositoryImpl() }
    factory { RollDiceUseCase(get()) }
    viewModel { DiceViewModel(get()) }
}

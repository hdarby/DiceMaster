package com.hdarby.dicemaster.di

import androidx.room.Room
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hdarby.dicemaster.data.local.DiceMasterDatabase
import com.hdarby.dicemaster.data.local.sessionDataStore
import com.hdarby.dicemaster.data.remote.FirebaseAuthDataSource
import com.hdarby.dicemaster.data.remote.FirestoreSessionDataSource
import com.hdarby.dicemaster.data.repository.CharacterRepositoryImpl
import com.hdarby.dicemaster.data.repository.DiceRepositoryImpl
import com.hdarby.dicemaster.data.repository.ItemRepositoryImpl
import com.hdarby.dicemaster.data.repository.SessionRepositoryImpl
import com.hdarby.dicemaster.data.repository.WeaponRepositoryImpl
import com.hdarby.dicemaster.data.remote.SessionRemoteDataSource
import com.hdarby.dicemaster.domain.repository.CharacterRepository
import com.hdarby.dicemaster.domain.repository.DiceRepository
import com.hdarby.dicemaster.domain.repository.ItemRepository
import com.hdarby.dicemaster.domain.repository.SessionRepository
import com.hdarby.dicemaster.domain.repository.WeaponRepository
import com.hdarby.dicemaster.domain.usecase.RollAdvantageUseCase
import com.hdarby.dicemaster.domain.usecase.RollDiceUseCase
import com.hdarby.dicemaster.domain.usecase.character.AddCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.AssignWeaponToCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.DeleteCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.GetCharactersWithWeaponsUseCase
import com.hdarby.dicemaster.domain.usecase.character.UnassignWeaponFromCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.character.UpdateCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.item.AddItemUseCase
import com.hdarby.dicemaster.domain.usecase.item.AssignItemToCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.item.DeleteItemUseCase
import com.hdarby.dicemaster.domain.usecase.item.GetItemsByCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.item.GetItemsUseCase
import com.hdarby.dicemaster.domain.usecase.item.UnassignItemFromCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.item.UpdateItemQuantityUseCase
import com.hdarby.dicemaster.domain.usecase.item.UpdateItemUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.AddWeaponUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.DeleteWeaponUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.GetWeaponsUseCase
import com.hdarby.dicemaster.domain.usecase.weapon.UpdateWeaponUseCase
import com.hdarby.dicemaster.viewmodel.CharacterViewModel
import com.hdarby.dicemaster.viewmodel.DebugViewModel
import com.hdarby.dicemaster.viewmodel.DiceViewModel
import com.hdarby.dicemaster.viewmodel.ItemViewModel
import com.hdarby.dicemaster.viewmodel.SessionViewModel
import com.hdarby.dicemaster.viewmodel.WeaponViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            DiceMasterDatabase::class.java,
            "dice_master_db"
        ).fallbackToDestructiveMigration().build()
    }

    // DAOs
    single { get<DiceMasterDatabase>().characterDao() }
    single { get<DiceMasterDatabase>().weaponDao() }
    single { get<DiceMasterDatabase>().itemDao() }

    // DataStore
    single<DataStore<Preferences>> { androidContext().sessionDataStore }

    // Repositories
    single<DiceRepository> { DiceRepositoryImpl() }
    single<CharacterRepository> { CharacterRepositoryImpl(get(), get()) }
    single<WeaponRepository> { WeaponRepositoryImpl(get()) }
    single<ItemRepository> { ItemRepositoryImpl(get()) }
    single<SessionRepository> { SessionRepositoryImpl(get()) }

    // Firebase
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseAuthDataSource(get()) }
    single<SessionRemoteDataSource> { FirestoreSessionDataSource(get()) }

    // Use Cases
    factory { RollDiceUseCase(get()) }
    factory { RollAdvantageUseCase(get()) }

    // Character Use Cases
    factory { GetCharactersWithWeaponsUseCase(get()) }
    factory { AddCharacterUseCase(get()) }
    factory { DeleteCharacterUseCase(get()) }
    factory { UpdateCharacterUseCase(get()) }
    factory { AssignWeaponToCharacterUseCase(get()) }
    factory { UnassignWeaponFromCharacterUseCase(get()) }

    // Weapon Use Cases
    factory { GetWeaponsUseCase(get()) }
    factory { AddWeaponUseCase(get()) }
    factory { UpdateWeaponUseCase(get()) }
    factory { DeleteWeaponUseCase(get()) }

    // Item Use Cases
    factory { GetItemsUseCase(get()) }
    factory { GetItemsByCharacterUseCase(get()) }
    factory { AddItemUseCase(get()) }
    factory { UpdateItemUseCase(get()) }
    factory { DeleteItemUseCase(get()) }
    factory { AssignItemToCharacterUseCase(get()) }
    factory { UnassignItemFromCharacterUseCase(get()) }
    factory { UpdateItemQuantityUseCase(get()) }

    // ViewModels
    viewModel { DiceViewModel(get(), get()) }
    viewModel { CharacterViewModel(get(), get(), get(), get(), get()) }
    viewModel { WeaponViewModel(get(), get(), get(), get(), get()) }
    viewModel { ItemViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { DebugViewModel() }
    viewModel { SessionViewModel(get(), get(), get(), get()) }
}

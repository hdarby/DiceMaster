package com.hdarby.dicemaster.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hdarby.dicemaster.data.local.dao.CharacterDao
import com.hdarby.dicemaster.data.local.dao.WeaponDao
import com.hdarby.dicemaster.data.local.entity.CharacterEntity
import com.hdarby.dicemaster.data.local.entity.CharacterWeaponCrossRef
import com.hdarby.dicemaster.data.local.entity.WeaponEntity

@Database(
    entities = [CharacterEntity::class, WeaponEntity::class, CharacterWeaponCrossRef::class],
    version = 2,
    exportSchema = false
)
abstract class DiceMasterDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun weaponDao(): WeaponDao
}

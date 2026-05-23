package com.hdarby.dicemaster.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hdarby.dicemaster.data.local.dao.CharacterDao
import com.hdarby.dicemaster.data.local.dao.ItemDao
import com.hdarby.dicemaster.data.local.dao.WeaponDao
import com.hdarby.dicemaster.data.local.entity.CharacterEntity
import com.hdarby.dicemaster.data.local.entity.CharacterItemCrossRef
import com.hdarby.dicemaster.data.local.entity.ItemEntity
import com.hdarby.dicemaster.data.local.entity.WeaponEntity

@Database(
    entities = [
        CharacterEntity::class,
        WeaponEntity::class,
        CharacterItemCrossRef::class,
        ItemEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class DiceMasterDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun weaponDao(): WeaponDao
    abstract fun itemDao(): ItemDao
}

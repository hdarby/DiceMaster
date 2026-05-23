package com.hdarby.dicemaster.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hdarby.dicemaster.data.local.entity.CharacterItemAssignment
import com.hdarby.dicemaster.data.local.entity.CharacterItemCrossRef
import com.hdarby.dicemaster.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM consumable_items ORDER BY name ASC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Query("""
        SELECT cir.characterId, i.id, i.name, i.description, cir.quantity
        FROM consumable_items i
        INNER JOIN character_item_cross_ref cir ON i.id = cir.itemId
    """)
    fun getAllCharacterItems(): Flow<List<CharacterItemAssignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCharacterItemCrossRef(crossRef: CharacterItemCrossRef)

    @Query("""
        UPDATE character_item_cross_ref
        SET quantity = :quantity
        WHERE characterId = :characterId AND itemId = :itemId
    """)
    suspend fun updateQuantity(characterId: Long, itemId: Long, quantity: Int)

    @Query("""
        DELETE FROM character_item_cross_ref
        WHERE characterId = :characterId AND itemId = :itemId
    """)
    suspend fun deleteCharacterItemCrossRef(characterId: Long, itemId: Long)
}


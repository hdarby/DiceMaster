package com.hdarby.dicemaster.data.remote

/**
 * Carries a character–item cross-reference as returned from the remote data source,
 * where the relationship is stored as a flat document rather than a Room junction table.
 */
data class RemoteCharacterItem(
    val characterId: Long,
    val itemId: Long,
    val quantity: Int
)


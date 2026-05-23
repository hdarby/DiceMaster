package com.hdarby.dicemaster.domain.model

sealed class UserRole {
    data object DungeonMaster : UserRole()
    data class Player(val characterId: Long) : UserRole()
}


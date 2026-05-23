package com.hdarby.dicemaster.data.remote

import com.hdarby.dicemaster.domain.model.Character
import kotlinx.coroutines.flow.Flow

interface CharacterRemoteDataSource {
    suspend fun upsertCharacter(sessionId: String, character: Character)
    suspend fun deleteCharacter(sessionId: String, characterId: Long)
    fun observeCharacters(sessionId: String): Flow<List<Character>>
}


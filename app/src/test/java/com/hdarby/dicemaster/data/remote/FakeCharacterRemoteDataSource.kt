package com.hdarby.dicemaster.data.remote

import com.hdarby.dicemaster.domain.model.Character
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCharacterRemoteDataSource : CharacterRemoteDataSource {

    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: List<Character> get() = _characters.value

    var upsertCallCount = 0
    var deleteCallCount = 0
    var lastUpsertedSessionId: String? = null
    var lastDeletedCharacterId: Long? = null

    override suspend fun upsertCharacter(sessionId: String, character: Character) {
        upsertCallCount++
        lastUpsertedSessionId = sessionId
        val current = _characters.value.toMutableList()
        current.removeAll { it.id == character.id }
        current.add(character)
        _characters.value = current
    }

    override suspend fun deleteCharacter(sessionId: String, characterId: Long) {
        deleteCallCount++
        lastDeletedCharacterId = characterId
        _characters.value = _characters.value.filter { it.id != characterId }
    }

    override fun observeCharacters(sessionId: String): Flow<List<Character>> = _characters.asStateFlow()

    /** Simulate a remote device pushing a character update. */
    fun simulateRemoteUpdate(vararg chars: Character) {
        _characters.value = chars.toList()
    }
}


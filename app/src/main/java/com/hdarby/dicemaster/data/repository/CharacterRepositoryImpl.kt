package com.hdarby.dicemaster.data.repository

import com.hdarby.dicemaster.data.local.dao.CharacterDao
import com.hdarby.dicemaster.data.local.dao.WeaponDao
import com.hdarby.dicemaster.data.local.entity.CharacterEntity
import com.hdarby.dicemaster.data.local.entity.CharacterWeaponCrossRef
import com.hdarby.dicemaster.data.remote.CharacterRemoteDataSource
import com.hdarby.dicemaster.data.remote.WeaponRemoteDataSource
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.CharacterWeaponEntry
import com.hdarby.dicemaster.domain.model.CharacterWithWeapons
import com.hdarby.dicemaster.domain.model.Stats
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.domain.repository.CharacterRepository
import com.hdarby.dicemaster.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CharacterRepositoryImpl(
    private val characterDao: CharacterDao,
    private val weaponDao: WeaponDao,
    private val sessionRepository: SessionRepository? = null,
    private val characterRemoteDataSource: CharacterRemoteDataSource? = null,
    private val weaponRemoteDataSource: WeaponRemoteDataSource? = null,
    private val externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : CharacterRepository {

    init {
        if (sessionRepository != null && characterRemoteDataSource != null) {
            startRemoteSync()
        }
    }

    private fun startRemoteSync() {
        externalScope.launch {
            sessionRepository!!.observeSession().collectLatest { session ->
                if (session == null) return@collectLatest
                characterRemoteDataSource!!.observeCharacters(session.sessionId).collect { remoteChars ->
                    remoteChars.forEach { character -> characterDao.insertCharacter(character.toEntity()) }
                }
            }
        }
    }

    override fun getAllCharacters(): Flow<List<Character>> =
        characterDao.getAllCharacters().map { it.map { entity -> entity.toDomain() } }

    override fun getCharactersWithWeapons(): Flow<List<CharacterWithWeapons>> =
        characterDao.getAllCharacters()
            .combine(weaponDao.getAllCharacterWeapons()) { characters, weaponAssignments ->
                val weaponsByCharId = weaponAssignments.groupBy { it.characterId }
                characters.map { char ->
                    CharacterWithWeapons(
                        character = char.toDomain(),
                        weapons = weaponsByCharId[char.id]
                            ?.map { CharacterWeaponEntry(it.assignmentId, it.weapon.toDomain()) }
                            ?: emptyList()
                    )
                }
            }

    override suspend fun addCharacter(character: Character): Long {
        val localId = characterDao.insertCharacter(character.toEntity())
        sessionRepository?.getActiveSession()?.let { session ->
            characterRemoteDataSource?.upsertCharacter(session.sessionId, character.copy(id = localId))
        }
        return localId
    }

    override suspend fun updateCharacter(character: Character) {
        characterDao.updateCharacter(character.toEntity())
        sessionRepository?.getActiveSession()?.let { session ->
            characterRemoteDataSource?.upsertCharacter(session.sessionId, character)
        }
    }

    override suspend fun deleteCharacter(character: Character) {
        characterDao.deleteCharacter(character.toEntity())
        sessionRepository?.getActiveSession()?.let { session ->
            characterRemoteDataSource?.deleteCharacter(session.sessionId, character.id)
        }
    }

    override suspend fun assignWeaponToCharacter(characterId: Long, weaponId: Long) {
        val isAtomic = weaponDao.isAtomicWeapon(weaponId)
        if (isAtomic && weaponDao.getWeaponAssignmentCount(weaponId) > 0) {
            throw IllegalStateException("This weapon is unique and is already assigned to a character.")
        }
        weaponDao.insertCharacterWeaponCrossRef(
            CharacterWeaponCrossRef(characterId = characterId, weaponId = weaponId)
        )
    }

    override suspend fun unassignWeaponFromCharacter(assignmentId: Long) {
        weaponDao.deleteCharacterWeaponCrossRef(assignmentId)
    }

    private fun CharacterEntity.toDomain() = Character(
        id = id, name = name, race = race,
        stats = Stats(
            strength = strength, strengthModifier = strengthModifier,
            dexterity = dexterity, dexterityModifier = dexterityModifier,
            constitution = constitution, constitutionModifier = constitutionModifier,
            intelligence = intelligence, intelligenceModifier = intelligenceModifier,
            wisdom = wisdom, wisdomModifier = wisdomModifier,
            charisma = charisma, charismaModifier = charismaModifier
        ),
        maxHitPoints = maxHitPoints,
        currentHitPoints = currentHitPoints,
        deathSaveFailures = deathSaveFailures,
        isDead = isDead
    )

    private fun Character.toEntity() = CharacterEntity(
        id = id, name = name, race = race,
        strength = stats.strength, strengthModifier = stats.strengthModifier,
        dexterity = stats.dexterity, dexterityModifier = stats.dexterityModifier,
        constitution = stats.constitution, constitutionModifier = stats.constitutionModifier,
        intelligence = stats.intelligence, intelligenceModifier = stats.intelligenceModifier,
        wisdom = stats.wisdom, wisdomModifier = stats.wisdomModifier,
        charisma = stats.charisma, charismaModifier = stats.charismaModifier,
        maxHitPoints = maxHitPoints,
        currentHitPoints = currentHitPoints,
        deathSaveFailures = deathSaveFailures,
        isDead = isDead
    )

    private fun com.hdarby.dicemaster.data.local.entity.WeaponEntity.toDomain() = Weapon(
        id = id, name = name, type = type,
        damageDice = damageDice, damageType = damageType,
        modifier = modifier, isAtomic = isAtomic
    )
}

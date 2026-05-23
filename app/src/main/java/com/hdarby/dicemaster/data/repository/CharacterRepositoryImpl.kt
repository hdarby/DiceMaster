package com.hdarby.dicemaster.data.repository

import com.hdarby.dicemaster.data.local.dao.CharacterDao
import com.hdarby.dicemaster.data.local.dao.WeaponDao
import com.hdarby.dicemaster.data.local.entity.CharacterEntity
import com.hdarby.dicemaster.data.local.entity.WeaponEntity
import com.hdarby.dicemaster.data.local.entity.CharacterWithWeapons as CharacterWithWeaponsEntity
import com.hdarby.dicemaster.data.remote.CharacterRemoteDataSource
import com.hdarby.dicemaster.data.remote.WeaponRemoteDataSource
import com.hdarby.dicemaster.domain.model.Character
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
        if (weaponRemoteDataSource != null) {
            externalScope.launch {
                sessionRepository!!.observeSession().collectLatest { session ->
                    if (session == null) return@collectLatest
                    weaponRemoteDataSource.observeWeapons(session.sessionId).collect { remoteWeapons ->
                        remoteWeapons.forEach { rw -> weaponDao.insertWeapon(rw.weapon.toEntity(rw.characterId)) }
                    }
                }
            }
        }
    }

    override fun getAllCharacters(): Flow<List<Character>> =
        characterDao.getAllCharacters().map { it.map { entity -> entity.toDomain() } }

    override fun getCharactersWithWeapons(): Flow<List<CharacterWithWeapons>> =
        characterDao.getCharactersWithWeapons().map { it.map { entity -> entity.toDomain() } }

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
        weaponDao.assignToCharacter(weaponId = weaponId, characterId = characterId)
        sessionRepository?.getActiveSession()?.let { session ->
            weaponRemoteDataSource?.updateWeaponAssignment(session.sessionId, weaponId, characterId)
        }
    }

    override suspend fun unassignWeaponFromCharacter(characterId: Long, weaponId: Long) {
        weaponDao.unassignFromCharacter(weaponId = weaponId)
        sessionRepository?.getActiveSession()?.let { session ->
            weaponRemoteDataSource?.updateWeaponAssignment(session.sessionId, weaponId, null)
        }
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
        )
    )

    private fun Character.toEntity() = CharacterEntity(
        id = id, name = name, race = race,
        strength = stats.strength, strengthModifier = stats.strengthModifier,
        dexterity = stats.dexterity, dexterityModifier = stats.dexterityModifier,
        constitution = stats.constitution, constitutionModifier = stats.constitutionModifier,
        intelligence = stats.intelligence, intelligenceModifier = stats.intelligenceModifier,
        wisdom = stats.wisdom, wisdomModifier = stats.wisdomModifier,
        charisma = stats.charisma, charismaModifier = stats.charismaModifier
    )

    private fun WeaponEntity.toDomain() = Weapon(
        id = id, name = name, type = type,
        damageDice = damageDice, damageType = damageType, modifier = modifier
    )

    private fun Weapon.toEntity(characterId: Long? = null) = WeaponEntity(
        id = id, name = name, type = type,
        damageDice = damageDice, damageType = damageType,
        modifier = modifier, characterId = characterId
    )

    private fun CharacterWithWeaponsEntity.toDomain() = CharacterWithWeapons(
        character = character.toDomain(),
        weapons = weapons.map { it.toDomain() }
    )
}

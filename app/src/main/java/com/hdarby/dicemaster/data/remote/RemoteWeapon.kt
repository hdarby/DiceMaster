package com.hdarby.dicemaster.data.remote

import com.hdarby.dicemaster.domain.model.Weapon

/**
 * Carries a [Weapon] together with its optional character assignment as returned from
 * the remote data source, where the relationship is stored as a field on the weapon
 * document rather than a foreign-key in a relational table.
 */
data class RemoteWeapon(val weapon: Weapon, val characterId: Long?)


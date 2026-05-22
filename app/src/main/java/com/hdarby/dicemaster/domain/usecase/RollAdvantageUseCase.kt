package com.hdarby.dicemaster.domain.usecase

import com.hdarby.dicemaster.domain.model.AdvantageMode
import com.hdarby.dicemaster.domain.model.AdvantageRollResult
import com.hdarby.dicemaster.domain.repository.DiceRepository

private const val D20_FACES = 20
private const val ADVANTAGE_DICE_COUNT = 2

/**
 * Use case for rolling two D20s under advantage or disadvantage rules.
 * Under advantage the higher roll is selected; under disadvantage the lower roll is selected.
 */
class RollAdvantageUseCase(private val repository: DiceRepository) {
    operator fun invoke(mode: AdvantageMode): AdvantageRollResult {
        val rolls = repository.rollDice(D20_FACES, ADVANTAGE_DICE_COUNT)
        return AdvantageRollResult(roll1 = rolls[0], roll2 = rolls[1], mode = mode)
    }
}


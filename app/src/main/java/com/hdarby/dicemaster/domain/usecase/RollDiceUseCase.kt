package com.hdarby.dicemaster.domain.usecase

import com.hdarby.dicemaster.domain.model.RollResult
import com.hdarby.dicemaster.domain.repository.DiceRepository

/**
 * Use case to handle rolling dice and sorting the results.
 * This encapsulates the business logic of getting random values and organizing them for display.
 */
class RollDiceUseCase(private val repository: DiceRepository) {
    /**
     * Rolls the specified number of dice with the given number of faces.
     * @param faces Number of faces on each die (e.g., 20 for D20).
     * @param quantity Number of dice to roll.
     * @param modifier An optional modifier to add to the sum of the rolls.
     * @return A RollResult containing individual rolls and the total.
     */
    operator fun invoke(faces: Int, quantity: Int, modifier: Int = 0): RollResult {
        val results = repository.rollDice(faces, quantity)
        return RollResult(
            rolls = results.sortedDescending(),
            modifier = modifier
        )
    }
}

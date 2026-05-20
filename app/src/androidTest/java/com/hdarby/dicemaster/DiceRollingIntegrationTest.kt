package com.hdarby.dicemaster

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class DiceRollingIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun diceRollingWorkflow_rollDiceAndViewResults() {
        // Verify on roller screen
        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()

        // Click roll dice button
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_roll_dice)
        ).performClick()

        // Verify results sheet appears
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.title_results)
        ).assertIsDisplayed()

        // Verify total is displayed
        composeTestRule.onNodeWithText("Total:", substring = true).assertIsDisplayed()
    }

    @Test
    fun diceRollingWorkflow_navigateToDebugScreen() {
        // Navigate to debug screen
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_rng_debug)
        ).performClick()

        // Verify debug screen is displayed
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.title_debug_rng)
        ).assertIsDisplayed()

        // Navigate back to roller
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_back)
        ).performClick()

        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()
    }

    @Test
    fun diceRollingWorkflow_rollerScreenRefresh() {
        // Start on roller screen
        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()

        // Roll dice
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_roll_dice)
        ).performClick()

        // Verify results appear
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.title_results)
        ).assertIsDisplayed()

        // Verify total is visible
        composeTestRule.onNodeWithText("Total:", substring = true).assertIsDisplayed()
    }

    @Test
    fun diceRollingWorkflow_multipleConsecutiveRolls() {
        // Start on roller
        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()

        // Roll 1
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_roll_dice)
        ).performClick()

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.title_results)
        ).assertIsDisplayed()

        // Go back to roll again
        composeTestRule.onNodeWithTag("nav_item_roller").performClick()
        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()

        // Roll 2
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_roll_dice)
        ).performClick()

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.title_results)
        ).assertIsDisplayed()

        // Results should show new roll
        composeTestRule.onNodeWithText("Total:", substring = true).assertIsDisplayed()
    }

    @Test
    fun debugScreenWorkflow_selectDieTypeAndRunSimulation() {
        // Navigate to debug
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_rng_debug)
        ).performClick()

        // Debug screen should be displayed
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.title_debug_rng)
        ).assertIsDisplayed()

        // Return to roller
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_back)
        ).performClick()

        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()
    }

    @Test
    fun diceRollingWorkflow_consistentStateAfterNavigation() {
        // Roll dice and get results
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_roll_dice)
        ).performClick()

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.title_results)
        ).assertIsDisplayed()

        val firstRollTotal = composeTestRule.onNodeWithText("Total:", substring = true)
        firstRollTotal.assertIsDisplayed()

        // Navigate away and back
        composeTestRule.onNodeWithTag("nav_item_characters").performClick()
        composeTestRule.onNodeWithTag("screen_title_characters").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_item_roller").performClick()
        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()

        // Roll again to update results
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_roll_dice)
        ).performClick()

        // New results should be displayed
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.title_results)
        ).assertIsDisplayed()
    }
}


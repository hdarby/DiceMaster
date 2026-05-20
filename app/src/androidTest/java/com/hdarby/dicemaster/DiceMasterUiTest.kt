package com.hdarby.dicemaster

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.hdarby.dicemaster.ui.screens.ResultsContent
import com.hdarby.dicemaster.ui.theme.DiceMasterTheme
import org.junit.Rule
import org.junit.Test

class DiceMasterUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun diceMasterScreen_initialState_displaysCorrectTitle() {
        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.button_roll_dice)).assertIsDisplayed()
    }

    @Test
    fun navigation_betweenScreens_works() {
        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_rng_debug)
        ).performClick()
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.title_debug_rng)
        ).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_back)
        ).performClick()

        composeTestRule.onNodeWithTag("nav_item_characters").performClick()
        composeTestRule.onNodeWithTag("screen_title_characters").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_add_character)
        ).assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_item_weapons").performClick()
        composeTestRule.onNodeWithTag("screen_title_weapons").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_add_weapon)
        ).assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_item_roller").performClick()
        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()
    }

    @Test
    fun diceMasterScreen_showResults_displaysResultsSheet() {
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.button_roll_dice)).performClick()

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.title_results)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Total:", substring = true).assertIsDisplayed()
    }

    @Test
    fun resultsContent_displaysDeterministicDiceValuesAndTotal() {
        composeTestRule.setContent {
            DiceMasterTheme {
                ResultsContent(
                    results = listOf(20, 12, 1),
                    total = 36,
                    faces = 20
                )
            }
        }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.title_results)).assertIsDisplayed()
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_total, 36, "")
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("20").assertIsDisplayed()
        composeTestRule.onNodeWithText("12").assertIsDisplayed()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
    }
}

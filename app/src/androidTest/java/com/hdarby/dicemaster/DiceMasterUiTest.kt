package com.hdarby.dicemaster

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.hdarby.dicemaster.ui.screens.DiceMasterScreen
import com.hdarby.dicemaster.ui.screens.DiceRollerScreen
import com.hdarby.dicemaster.ui.theme.DiceMasterTheme
import com.hdarby.dicemaster.viewmodel.DiceUiState
import org.junit.Rule
import org.junit.Test

class DiceMasterUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun diceMasterScreen_initialState_displaysCorrectTitle() {
        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()
        composeTestRule.onNodeWithText("ROLL DICE").assertIsDisplayed()
    }

    @Test
    fun navigation_betweenScreens_works() {
        // Start on Roller
        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()
        
        // Navigate to Debug screen
        composeTestRule.onNodeWithContentDescription("RNG Debug").performClick()
        composeTestRule.onNodeWithText("RNG Distribution Analysis").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        // Navigate to Characters using test tag to avoid ambiguity with TopAppBar title
        composeTestRule.onNode(hasTestTag("nav_item_characters")).performClick()
        
        // Verify we are on Characters screen
        composeTestRule.onNodeWithTag("screen_title_characters").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Character").assertIsDisplayed()
        
        // Navigate to Weapons
        composeTestRule.onNode(hasTestTag("nav_item_weapons")).performClick()
        composeTestRule.onNodeWithTag("screen_title_weapons").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Weapon").assertIsDisplayed()
        
        // Navigate back to Roller
        composeTestRule.onNode(hasTestTag("nav_item_roller")).performClick()
        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()
    }

    @Test
    fun diceMasterScreen_showResults_displaysResultsSheet() {
        // This tests the interaction in the actual app if possible, or we use setContent on a fresh activity
        // For now, let's just use the existing activity and perform a roll
        
        composeTestRule.onNodeWithText("ROLL DICE").performClick()
        
        // Results sheet should appear
        composeTestRule.onNodeWithText("Results").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total:", substring = true).assertIsDisplayed()
    }
}

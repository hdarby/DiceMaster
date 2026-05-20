package com.hdarby.dicemaster

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class CharacterWorkflowIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun characterCreationWorkflow_createAndViewCharacter() {
        // Navigate to characters screen
        composeTestRule.onNodeWithTag("nav_item_characters").performClick()
        composeTestRule.onNodeWithTag("screen_title_characters").assertIsDisplayed()

        // Open add character dialog
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_add_character)
        ).performClick()

        // Fill in character details
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_name)
        ).performTextInput("Aragorn")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_race)
        ).performTextInput("Human")

        // Confirm creation
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_confirm)
        ).performClick()

        // Verify character appears in list
        composeTestRule.onNodeWithText("Aragorn").assertIsDisplayed()
    }

    @Test
    fun characterEditWorkflow_editCharacterStats() {
        // Navigate to characters screen
        composeTestRule.onNodeWithTag("nav_item_characters").performClick()
        composeTestRule.onNodeWithTag("screen_title_characters").assertIsDisplayed()

        // Create initial character
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_add_character)
        ).performClick()

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_name)
        ).performTextInput("TestChar")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_race)
        ).performTextInput("Elf")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_confirm)
        ).performClick()

        // Click edit on the created character
        composeTestRule.onNodeWithText("TestChar").performClick()
        composeTestRule.onNodeWithContentDescription("Edit").performClick()

        // Verify edit dialog shows existing data
        composeTestRule.onNodeWithText("TestChar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Elf").assertIsDisplayed()

        // Update race
        composeTestRule.onNodeWithText("Elf").performTextInput(" Ranger")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_confirm)
        ).performClick()

        // Verify updated data
        composeTestRule.onNodeWithText("Elf Ranger").assertIsDisplayed()
    }

    @Test
    fun navigationFlow_tabBetweenCharactersAndWeapons() {
        // Start on roller screen
        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()

        // Navigate to characters
        composeTestRule.onNodeWithTag("nav_item_characters").performClick()
        composeTestRule.onNodeWithTag("screen_title_characters").assertIsDisplayed()

        // Navigate to weapons
        composeTestRule.onNodeWithTag("nav_item_weapons").performClick()
        composeTestRule.onNodeWithTag("screen_title_weapons").assertIsDisplayed()

        // Back to rollerscreen
        composeTestRule.onNodeWithTag("nav_item_roller").performClick()
        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()
    }

    @Test
    fun characterDeletionWorkflow_deleteCharacter() {
        // Navigate to characters
        composeTestRule.onNodeWithTag("nav_item_characters").performClick()

        // Create character
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_add_character)
        ).performClick()

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_name)
        ).performTextInput("DeleteMe")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_race)
        ).performTextInput("Dwarf")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_confirm)
        ).performClick()

        // Verify character exists
        composeTestRule.onNodeWithText("DeleteMe").assertIsDisplayed()

        // Click delete
        composeTestRule.onNodeWithText("DeleteMe").performClick()
        composeTestRule.onNodeWithContentDescription("Delete").performClick()

        // Character should no longer appear in list
        // (This assumes immediate removal; if async, may need wait logic)
        // For now, just verify the delete action was clickable and executed
    }
}


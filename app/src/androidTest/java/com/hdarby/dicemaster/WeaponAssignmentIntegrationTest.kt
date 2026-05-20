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

class WeaponAssignmentIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun weaponAssignmentWorkflow_createWeaponAndAssignToCharacter() {
        // First, create a character
        composeTestRule.onNodeWithTag("nav_item_characters").performClick()
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_add_character)
        ).performClick()

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_name)
        ).performTextInput("Thorin")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_race)
        ).performTextInput("Dwarf")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_confirm)
        ).performClick()

        // Now navigate to weapons
        composeTestRule.onNodeWithTag("nav_item_weapons").performClick()
        composeTestRule.onNodeWithTag("screen_title_weapons").assertIsDisplayed()

        // Create a weapon
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_add_weapon)
        ).performClick()

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_name)
        ).performTextInput("Battle Axe")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_weapon_type)
        ).performTextInput("Heavy")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_damage_dice)
        ).performTextInput("1d12")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_damage_type)
        ).performTextInput("Slashing")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_confirm)
        ).performClick()

        // Verify weapon is displayed
        composeTestRule.onNodeWithText("Battle Axe").assertIsDisplayed()

        // Click assign button on weapon
        composeTestRule.onNodeWithContentDescription("Assign").performClick()

        // Select the character to assign weapon to
        composeTestRule.onNodeWithText("Thorin").performClick()

        // Verify assignment was made (by going back to characters screen)
        composeTestRule.onNodeWithTag("nav_item_characters").performClick()

        // Verify Thorin has the Battle Axe listed
        composeTestRule.onNodeWithText("Thorin").assertIsDisplayed()
        composeTestRule.onNodeWithText("Battle Axe").assertIsDisplayed()
    }

    @Test
    fun weaponModificationWorkflow_updateWeaponModifier() {
        // Navigate to weapons
        composeTestRule.onNodeWithTag("nav_item_weapons").performClick()

        // Create weapon
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_add_weapon)
        ).performClick()

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_name)
        ).performTextInput("Shortsword")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_weapon_type)
        ).performTextInput("Light")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_damage_dice)
        ).performTextInput("1d6")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_damage_type)
        ).performTextInput("Piercing")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_modifier)
        ).performTextInput("1")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_confirm)
        ).performClick()

        // Verify weapon appears with modifier
        composeTestRule.onNodeWithText("Shortsword").assertIsDisplayed()
        composeTestRule.onNodeWithText("Modifier: 1").assertIsDisplayed()

        // Edit weapon to change modifier
        composeTestRule.onNodeWithText("Shortsword").performClick()
        composeTestRule.onNodeWithContentDescription("Edit").performClick()

        // Update modifier to 3
        composeTestRule.onNodeWithText("1").performTextInput("3")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_confirm)
        ).performClick()

        // Verify updated modifier
        composeTestRule.onNodeWithText("Modifier: 3").assertIsDisplayed()
    }

    @Test
    fun weaponDeletionWorkflow_deleteWeapon() {
        // Navigate to weapons
        composeTestRule.onNodeWithTag("nav_item_weapons").performClick()

        // Create weapon
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_add_weapon)
        ).performClick()

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_name)
        ).performTextInput("DeleteWeapon")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_weapon_type)
        ).performTextInput("Test")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_damage_dice)
        ).performTextInput("1d4")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_damage_type)
        ).performTextInput("Test")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_confirm)
        ).performClick()

        // Verify weapon exists
        composeTestRule.onNodeWithText("DeleteWeapon").assertIsDisplayed()

        // Click delete
        composeTestRule.onNodeWithText("DeleteWeapon").performClick()
        composeTestRule.onNodeWithContentDescription("Delete").performClick()

        // Weapon should be removed from list
    }

    @Test
    fun multipleWeaponAssignment_assignMultipleWeaponsSameCharacter() {
        // Create character
        composeTestRule.onNodeWithTag("nav_item_characters").performClick()
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_add_character)
        ).performClick()

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_name)
        ).performTextInput("MultiWeaponChar")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_race)
        ).performTextInput("Human")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_confirm)
        ).performClick()

        // Create first weapon
        composeTestRule.onNodeWithTag("nav_item_weapons").performClick()
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_add_weapon)
        ).performClick()

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_name)
        ).performTextInput("Weapon1")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_weapon_type)
        ).performTextInput("Type1")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_damage_dice)
        ).performTextInput("1d6")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_damage_type)
        ).performTextInput("Damage1")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_confirm)
        ).performClick()

        // Assign first weapon
        composeTestRule.onNodeWithContentDescription("Assign").performClick()
        composeTestRule.onNodeWithText("MultiWeaponChar").performClick()

        // Create second weapon
        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_add_weapon)
        ).performClick()

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_name)
        ).performTextInput("Weapon2")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_weapon_type)
        ).performTextInput("Type2")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_damage_dice)
        ).performTextInput("1d8")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_damage_type)
        ).performTextInput("Damage2")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_confirm)
        ).performClick()

        // Assign second weapon
        composeTestRule.onNodeWithContentDescription("Assign").performClick()
        composeTestRule.onNodeWithText("MultiWeaponChar").performClick()

        // Verify both weapons are assigned
        composeTestRule.onNodeWithTag("nav_item_characters").performClick()
        composeTestRule.onNodeWithText("MultiWeaponChar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weapon1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weapon2").assertIsDisplayed()
    }
}


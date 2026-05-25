package com.hdarby.dicemaster.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.hdarby.dicemaster.R
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.CharacterWithWeapons
import com.hdarby.dicemaster.domain.model.Stats
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.ui.theme.DiceMasterTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CharacterScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val testCharacter = Character(
        id = 1,
        name = "Grog",
        race = "Goliath",
        stats = Stats(
            strength = 20, strengthModifier = 5,
            dexterity = 12, dexterityModifier = 1,
            constitution = 18, constitutionModifier = 4,
            intelligence = 6, intelligenceModifier = -2,
            wisdom = 10, wisdomModifier = 0,
            charisma = 8, charismaModifier = -1
        )
    )

    private val testWeapon = Weapon(
        id = 1,
        name = "Greataxe",
        weaponType = com.hdarby.dicemaster.domain.model.WeaponType.MARTIAL_MELEE,
        damageDice = com.hdarby.dicemaster.domain.model.DamageDice.D12,
        damageType = com.hdarby.dicemaster.domain.model.DamageType.SLASHING,
        damageModifier = 2
    )

    @Test
    fun characterCard_displaysCharacterInfo() {
        composeTestRule.setContent {
            DiceMasterTheme {
                CharacterCard(
                    characterWithWeapons = CharacterWithWeapons(testCharacter, emptyList()),
                    onEdit = {},
                    onDelete = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Grog").assertIsDisplayed()
        composeTestRule.onNodeWithText("Goliath").assertIsDisplayed()
        composeTestRule.onNodeWithText("20").assertIsDisplayed() // STR
        composeTestRule.onNodeWithText("12").assertIsDisplayed() // DEX
    }

    @Test
    fun characterCard_displaysWeapons() {
        composeTestRule.setContent {
            DiceMasterTheme {
                CharacterCard(
                    characterWithWeapons = CharacterWithWeapons(testCharacter, listOf(testWeapon)),
                    onEdit = {},
                    onDelete = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Greataxe (Heavy)").assertIsDisplayed()
    }

    @Test
    fun characterCard_callsOnEditWhenEditClicked() {
        var editCalled = false
        var editedCharacter: Character? = null

        composeTestRule.setContent {
            DiceMasterTheme {
                CharacterCard(
                    characterWithWeapons = CharacterWithWeapons(testCharacter, emptyList()),
                    onEdit = {
                        editCalled = true
                        editedCharacter = it
                    },
                    onDelete = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_edit)
        ).performClick()

        assertTrue(editCalled)
        assertTrue(editedCharacter?.id == testCharacter.id)
    }

    @Test
    fun characterCard_callsOnDeleteWhenDeleteClicked() {
        var deleteCalled = false
        var deletedCharacter: Character? = null

        composeTestRule.setContent {
            DiceMasterTheme {
                CharacterCard(
                    characterWithWeapons = CharacterWithWeapons(testCharacter, emptyList()),
                    onEdit = {},
                    onDelete = {
                        deleteCalled = true
                        deletedCharacter = it
                    }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(
            composeTestRule.activity.getString(R.string.content_desc_delete)
        ).performClick()

        assertTrue(deleteCalled)
        assertTrue(deletedCharacter?.id == testCharacter.id)
    }

    @Test
    fun statItem_displaysAllValues() {
        composeTestRule.setContent {
            DiceMasterTheme {
                StatItem("STR", 20, 5)
            }
        }

        composeTestRule.onNodeWithText("STR").assertIsDisplayed()
        composeTestRule.onNodeWithText("20").assertIsDisplayed()
        composeTestRule.onNodeWithText("(+5)").assertIsDisplayed()
    }

    @Test
    fun statItem_displaysNegativeModifier() {
        composeTestRule.setContent {
            DiceMasterTheme {
                StatItem("INT", 6, -2)
            }
        }

        composeTestRule.onNodeWithText("INT").assertIsDisplayed()
        composeTestRule.onNodeWithText("6").assertIsDisplayed()
        composeTestRule.onNodeWithText("(-2)").assertIsDisplayed()
    }

    @Test
    fun addEditCharacterDialog_displaysTitleForAdd() {
        composeTestRule.setContent {
            DiceMasterTheme {
                AddEditCharacterDialog(
                    character = null,
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.title_add_character)
        ).assertIsDisplayed()
    }

    @Test
    fun addEditCharacterDialog_displaysTitleForEdit() {
        composeTestRule.setContent {
            DiceMasterTheme {
                AddEditCharacterDialog(
                    character = testCharacter,
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.title_edit_character)
        ).assertIsDisplayed()
    }

    @Test
    fun addEditCharacterDialog_populatesFieldsWhenEditing() {
        composeTestRule.setContent {
            DiceMasterTheme {
                AddEditCharacterDialog(
                    character = testCharacter,
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Grog").assertIsDisplayed()
        composeTestRule.onNodeWithText("Goliath").assertIsDisplayed()
    }

    @Test
    fun addEditCharacterDialog_callsConfirmWhenConfirmed() {
        var confirmCalled = false
        var confirmedCharacter: Character? = null

        composeTestRule.setContent {
            DiceMasterTheme {
                AddEditCharacterDialog(
                    character = null,
                    onDismiss = {},
                    onConfirm = {
                        confirmCalled = true
                        confirmedCharacter = it
                    }
                )
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_confirm)
        ).performClick()

        assertTrue(confirmCalled)
        assertTrue(confirmedCharacter?.name != null)
    }

    @Test
    fun addEditCharacterDialog_callsDismissWhenCancelled() {
        var dismissCalled = false

        composeTestRule.setContent {
            DiceMasterTheme {
                AddEditCharacterDialog(
                    character = null,
                    onDismiss = { dismissCalled = true },
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.button_cancel)
        ).performClick()

        assertTrue(dismissCalled)
    }

    @Test
    fun characterCard_callsOnWeaponClickWhenWeaponChipTapped() {
        var weaponClickCalled = false
        var clickedWeapon: Weapon? = null

        composeTestRule.setContent {
            DiceMasterTheme {
                CharacterCard(
                    characterWithWeapons = CharacterWithWeapons(testCharacter, listOf(testWeapon)),
                    onEdit = {},
                    onDelete = {},
                    onWeaponClick = {
                        weaponClickCalled = true
                        clickedWeapon = it
                    }
                )
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.format_weapon_chip_label, testWeapon.name, testWeapon.weaponType.displayName)
        ).performClick()

        assertTrue(weaponClickCalled)
        assertTrue(clickedWeapon?.id == testWeapon.id)
    }

    @Test
    fun characterCard_doesNotRenderWeaponSectionWhenNoWeaponsAssigned() {
        var weaponClickCalled = false

        composeTestRule.setContent {
            DiceMasterTheme {
                CharacterCard(
                    characterWithWeapons = CharacterWithWeapons(testCharacter, emptyList()),
                    onEdit = {},
                    onDelete = {},
                    onWeaponClick = { weaponClickCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_weapons_section)
        ).assertDoesNotExist()
        assertTrue(!weaponClickCalled)
    }
}


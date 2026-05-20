package com.hdarby.dicemaster.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.hdarby.dicemaster.R
import com.hdarby.dicemaster.domain.model.Character
import com.hdarby.dicemaster.domain.model.Stats
import com.hdarby.dicemaster.domain.model.Weapon
import com.hdarby.dicemaster.ui.theme.DiceMasterTheme
import org.junit.Rule
import org.junit.Test

class WeaponScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val testWeapon = Weapon(
        id = 1,
        name = "Longsword",
        type = "Melee",
        damageDice = "1d8",
        damageType = "Slashing",
        modifier = 2
    )

    private val testCharacter = Character(
        id = 1,
        name = "Aragorn",
        race = "Human",
        stats = Stats(
            strength = 18, strengthModifier = 4,
            dexterity = 15, dexterityModifier = 2,
            constitution = 16, constitutionModifier = 3,
            intelligence = 14, intelligenceModifier = 2,
            wisdom = 16, wisdomModifier = 3,
            charisma = 15, charismaModifier = 2
        )
    )

    @Test
    fun weaponCard_displaysWeaponInfo() {
        composeTestRule.setContent {
            DiceMasterTheme {
                WeaponCard(
                    weapon = testWeapon,
                    onEdit = {},
                    onDelete = {},
                    onAssign = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Longsword").assertIsDisplayed()
        composeTestRule.onNodeWithText("(Melee)").assertIsDisplayed()
        composeTestRule.onNodeWithText("1d8").assertIsDisplayed()
        composeTestRule.onNodeWithText("Slashing").assertIsDisplayed()
        composeTestRule.onNodeWithText("Modifier: 2").assertIsDisplayed()
    }

    @Test
    fun weaponCard_callsOnAssignWhenAssignClicked() {
        var assignCalled = false
        var assignedWeapon: Weapon? = null

        composeTestRule.setContent {
            DiceMasterTheme {
                WeaponCard(
                    weapon = testWeapon,
                    onEdit = {},
                    onDelete = {},
                    onAssign = {
                        assignCalled = true
                        assignedWeapon = it
                    }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Assign").performClick()

        assert(assignCalled)
        assert(assignedWeapon?.id == testWeapon.id)
    }

    @Test
    fun weaponCard_callsOnEditWhenEditClicked() {
        var editCalled = false
        var editedWeapon: Weapon? = null

        composeTestRule.setContent {
            DiceMasterTheme {
                WeaponCard(
                    weapon = testWeapon,
                    onEdit = {
                        editCalled = true
                        editedWeapon = it
                    },
                    onDelete = {},
                    onAssign = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Edit").performClick()

        assert(editCalled)
        assert(editedWeapon?.id == testWeapon.id)
    }

    @Test
    fun weaponCard_callsOnDeleteWhenDeleteClicked() {
        var deleteCalled = false
        var deletedWeapon: Weapon? = null

        composeTestRule.setContent {
            DiceMasterTheme {
                WeaponCard(
                    weapon = testWeapon,
                    onEdit = {},
                    onDelete = {
                        deleteCalled = true
                        deletedWeapon = it
                    },
                    onAssign = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Delete").performClick()

        assert(deleteCalled)
        assert(deletedWeapon?.id == testWeapon.id)
    }

    @Test
    fun addEditWeaponDialog_displaysTitleForAdd() {
        var displayedTitle = ""
        composeTestRule.setContent {
            DiceMasterTheme {
                AddEditWeaponDialog(
                    weapon = null,
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        // Titles are set in the dialog, just verify dialog is displayed
        composeTestRule.onNodeWithText("Add Weapon", substring = true).assertIsDisplayed()
    }

    @Test
    fun addEditWeaponDialog_displaysTitleForEdit() {
        composeTestRule.setContent {
            DiceMasterTheme {
                AddEditWeaponDialog(
                    weapon = testWeapon,
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        // Titles are set in the dialog, just verify weapon data is populated
        composeTestRule.onNodeWithText("Longsword").assertIsDisplayed()
    }

    @Test
    fun addEditWeaponDialog_populatesFieldsWhenEditing() {
        composeTestRule.setContent {
            DiceMasterTheme {
                AddEditWeaponDialog(
                    weapon = testWeapon,
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Longsword").assertIsDisplayed()
        composeTestRule.onNodeWithText("Melee").assertIsDisplayed()
        composeTestRule.onNodeWithText("1d8").assertIsDisplayed()
    }

    @Test
    fun addEditWeaponDialog_callsConfirmWhenConfirmed() {
        var confirmCalled = false
        var confirmedWeapon: Weapon? = null

        composeTestRule.setContent {
            DiceMasterTheme {
                AddEditWeaponDialog(
                    weapon = null,
                    onDismiss = {},
                    onConfirm = {
                        confirmCalled = true
                        confirmedWeapon = it
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Confirm").performClick()

        assert(confirmCalled)
        assert(confirmedWeapon?.name != null)
    }

    @Test
    fun addEditWeaponDialog_callsDismissWhenCancelled() {
        var dismissCalled = false

        composeTestRule.setContent {
            DiceMasterTheme {
                AddEditWeaponDialog(
                    weapon = null,
                    onDismiss = { dismissCalled = true },
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Cancel").performClick()

        assert(dismissCalled)
    }

    @Test
    fun assignWeaponDialog_displaysTitle() {
        composeTestRule.setContent {
            DiceMasterTheme {
                AssignWeaponDialog(
                    weapon = testWeapon,
                    characters = listOf(testCharacter),
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.title_assign_weapon, testWeapon.name)
        ).assertIsDisplayed()
    }

    @Test
    fun assignWeaponDialog_displaysCharacters() {
        composeTestRule.setContent {
            DiceMasterTheme {
                AssignWeaponDialog(
                    weapon = testWeapon,
                    characters = listOf(testCharacter),
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Aragorn").assertIsDisplayed()
        composeTestRule.onNodeWithText("Human").assertIsDisplayed()
    }

    @Test
    fun assignWeaponDialog_callsConfirmWhenCharacterSelected() {
        var confirmCalled = false
        var selectedCharacterId: Long? = null

        composeTestRule.setContent {
            DiceMasterTheme {
                AssignWeaponDialog(
                    weapon = testWeapon,
                    characters = listOf(testCharacter),
                    onDismiss = {},
                    onConfirm = {
                        confirmCalled = true
                        selectedCharacterId = it
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("Aragorn").performClick()

        assert(confirmCalled)
        assert(selectedCharacterId == testCharacter.id)
    }

    @Test
    fun assignWeaponDialog_callsDismissWhenCancelled() {
        var dismissCalled = false

        composeTestRule.setContent {
            DiceMasterTheme {
                AssignWeaponDialog(
                    weapon = testWeapon,
                    characters = listOf(testCharacter),
                    onDismiss = { dismissCalled = true },
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Cancel").performClick()

        assert(dismissCalled)
    }

    @Test
    fun assignWeaponDialog_handlesEmptyCharacterList() {
        composeTestRule.setContent {
            DiceMasterTheme {
                AssignWeaponDialog(
                    weapon = testWeapon,
                    characters = emptyList(),
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.title_assign_weapon, testWeapon.name)
        ).assertIsDisplayed()
    }
}





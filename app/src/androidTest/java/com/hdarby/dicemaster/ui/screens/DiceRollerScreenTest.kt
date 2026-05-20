package com.hdarby.dicemaster.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.hdarby.dicemaster.domain.model.RollResult
import com.hdarby.dicemaster.ui.theme.DiceMasterTheme
import com.hdarby.dicemaster.viewmodel.DiceUiState
import org.junit.Rule
import org.junit.Test

class DiceRollerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // region DiceMasterScreen

    @Test
    fun diceMasterScreen_initialState_displaysScreenTitle() {
        composeTestRule.setContent {
            DiceMasterTheme {
                DiceMasterScreen(
                    uiState = DiceUiState(),
                    onUpdateFaces = {},
                    onUpdateQuantity = {},
                    onUpdateModifier = {},
                    onRollDice = {},
                    onDismissResults = {},
                    onNavigateToDebug = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()
    }

    @Test
    fun diceMasterScreen_initialState_displaysRollDiceButton() {
        composeTestRule.setContent {
            DiceMasterTheme {
                DiceMasterScreen(
                    uiState = DiceUiState(),
                    onUpdateFaces = {},
                    onUpdateQuantity = {},
                    onUpdateModifier = {},
                    onRollDice = {},
                    onDismissResults = {},
                    onNavigateToDebug = {}
                )
            }
        }

        composeTestRule.onNodeWithText("ROLL DICE").assertIsDisplayed()
    }

    @Test
    fun diceMasterScreen_withResults_displaysResultsSheet() {
        val rollResult = RollResult(rolls = listOf(15, 8, 3), modifier = 0)
        composeTestRule.setContent {
            DiceMasterTheme {
                DiceMasterScreen(
                    uiState = DiceUiState(showResults = true, rollResult = rollResult, faces = 20),
                    onUpdateFaces = {},
                    onUpdateQuantity = {},
                    onUpdateModifier = {},
                    onRollDice = {},
                    onDismissResults = {},
                    onNavigateToDebug = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Results").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total:", substring = true).assertIsDisplayed()
    }

    @Test
    fun diceMasterScreen_withResults_displaysIndividualRolls() {
        val rollResult = RollResult(rolls = listOf(7, 14, 2), modifier = 0)
        composeTestRule.setContent {
            DiceMasterTheme {
                DiceMasterScreen(
                    uiState = DiceUiState(showResults = true, rollResult = rollResult, faces = 6),
                    onUpdateFaces = {},
                    onUpdateQuantity = {},
                    onUpdateModifier = {},
                    onRollDice = {},
                    onDismissResults = {},
                    onNavigateToDebug = {}
                )
            }
        }

        composeTestRule.onNodeWithText("7").assertIsDisplayed()
        composeTestRule.onNodeWithText("14").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
    }

    @Test
    fun diceMasterScreen_showResultsFalse_doesNotDisplayResultsSheet() {
        composeTestRule.setContent {
            DiceMasterTheme {
                DiceMasterScreen(
                    uiState = DiceUiState(showResults = false, rollResult = null),
                    onUpdateFaces = {},
                    onUpdateQuantity = {},
                    onUpdateModifier = {},
                    onRollDice = {},
                    onDismissResults = {},
                    onNavigateToDebug = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("screen_title_roller").assertIsDisplayed()
        composeTestRule.onNodeWithText("Results").assertDoesNotExist()
    }

    // endregion

    // region DiceConfigurationSection

    @Test
    fun diceConfigurationSection_displaysDiceFacesLabel() {
        composeTestRule.setContent {
            DiceMasterTheme {
                DiceConfigurationSection(
                    faces = 20,
                    quantity = 1,
                    modifierValue = 0,
                    onUpdateFaces = {},
                    onUpdateQuantity = {},
                    onUpdateModifier = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Dice Faces").assertIsDisplayed()
        composeTestRule.onNodeWithText("D20").assertIsDisplayed()
    }

    @Test
    fun diceConfigurationSection_displaysQuantityLabel() {
        composeTestRule.setContent {
            DiceMasterTheme {
                DiceConfigurationSection(
                    faces = 6,
                    quantity = 3,
                    modifierValue = 0,
                    onUpdateFaces = {},
                    onUpdateQuantity = {},
                    onUpdateModifier = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Quantity").assertIsDisplayed()
        composeTestRule.onNodeWithText("D6").assertIsDisplayed()
    }

    @Test
    fun diceConfigurationSection_displaysModifierLabel() {
        composeTestRule.setContent {
            DiceMasterTheme {
                DiceConfigurationSection(
                    faces = 20,
                    quantity = 1,
                    modifierValue = 0,
                    onUpdateFaces = {},
                    onUpdateQuantity = {},
                    onUpdateModifier = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Modifier").assertIsDisplayed()
    }

    @Test
    fun diceConfigurationSection_nonZeroModifier_displaysModifierValue() {
        composeTestRule.setContent {
            DiceMasterTheme {
                DiceConfigurationSection(
                    faces = 20,
                    quantity = 1,
                    modifierValue = 5,
                    onUpdateFaces = {},
                    onUpdateQuantity = {},
                    onUpdateModifier = {}
                )
            }
        }

        composeTestRule.onNodeWithText("5").assertIsDisplayed()
    }

    // endregion

    // region ResultsContent

    @Test
    fun resultsContent_withNoModifier_displaysPlainTotal() {
        composeTestRule.setContent {
            DiceMasterTheme {
                ResultsContent(
                    results = listOf(10),
                    total = 10,
                    faces = 20,
                    modifier = 0
                )
            }
        }

        composeTestRule.onNodeWithText("Total: 10", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
    }

    @Test
    fun resultsContent_withPositiveModifier_displaysModifierText() {
        composeTestRule.setContent {
            DiceMasterTheme {
                ResultsContent(
                    results = listOf(10, 5),
                    total = 17,
                    faces = 6,
                    modifier = 2
                )
            }
        }

        composeTestRule.onNodeWithText("Total: 17 (+2)", substring = true).assertIsDisplayed()
    }

    @Test
    fun resultsContent_withNegativeModifier_displaysModifierText() {
        composeTestRule.setContent {
            DiceMasterTheme {
                ResultsContent(
                    results = listOf(10, 5),
                    total = 13,
                    faces = 6,
                    modifier = -2
                )
            }
        }

        composeTestRule.onNodeWithText("Total: 13 (-2)", substring = true).assertIsDisplayed()
    }

    @Test
    fun resultsContent_multipleRolls_allValuesDisplayed() {
        composeTestRule.setContent {
            DiceMasterTheme {
                ResultsContent(
                    results = listOf(20, 12, 1),
                    total = 33,
                    faces = 20
                )
            }
        }

        composeTestRule.onNodeWithText("Results").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total: 33", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("12").assertIsDisplayed()
    }

    // endregion

    // region ResultItem

    @Test
    fun resultItem_displaysValueForD6() {
        composeTestRule.setContent {
            DiceMasterTheme {
                ResultItem(value = 4, faces = 6)
            }
        }

        composeTestRule.onNodeWithText("4").assertIsDisplayed()
    }

    @Test
    fun resultItem_d20CriticalHit_displaysValue() {
        composeTestRule.setContent {
            DiceMasterTheme {
                ResultItem(value = 20, faces = 20)
            }
        }

        composeTestRule.onNodeWithText("20").assertIsDisplayed()
    }

    @Test
    fun resultItem_d20CriticalMiss_displaysValue() {
        composeTestRule.setContent {
            DiceMasterTheme {
                ResultItem(value = 1, faces = 20)
            }
        }

        composeTestRule.onNodeWithText("1").assertIsDisplayed()
    }

    @Test
    fun resultItem_d3_displaysValue() {
        composeTestRule.setContent {
            DiceMasterTheme {
                ResultItem(value = 2, faces = 3)
            }
        }

        composeTestRule.onNodeWithText("2").assertIsDisplayed()
    }

    @Test
    fun resultItem_d4_displaysValue() {
        composeTestRule.setContent {
            DiceMasterTheme {
                ResultItem(value = 3, faces = 4)
            }
        }

        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun resultItem_d8_displaysValue() {
        composeTestRule.setContent {
            DiceMasterTheme {
                ResultItem(value = 6, faces = 8)
            }
        }

        composeTestRule.onNodeWithText("6").assertIsDisplayed()
    }

    @Test
    fun resultItem_d10_displaysValue() {
        composeTestRule.setContent {
            DiceMasterTheme {
                ResultItem(value = 7, faces = 10)
            }
        }

        composeTestRule.onNodeWithText("7").assertIsDisplayed()
    }

    @Test
    fun resultItem_d12_displaysValue() {
        composeTestRule.setContent {
            DiceMasterTheme {
                ResultItem(value = 9, faces = 12)
            }
        }

        composeTestRule.onNodeWithText("9").assertIsDisplayed()
    }

    @Test
    fun resultItem_d100_displaysValue() {
        composeTestRule.setContent {
            DiceMasterTheme {
                ResultItem(value = 77, faces = 100)
            }
        }

        composeTestRule.onNodeWithText("77").assertIsDisplayed()
    }

    @Test
    fun resultItem_d20NormalRoll_displaysValue() {
        composeTestRule.setContent {
            DiceMasterTheme {
                ResultItem(value = 15, faces = 20)
            }
        }

        composeTestRule.onNodeWithText("15").assertIsDisplayed()
    }

    // endregion
}


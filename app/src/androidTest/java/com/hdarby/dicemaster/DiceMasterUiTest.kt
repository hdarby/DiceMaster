package com.hdarby.dicemaster

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.hdarby.dicemaster.ui.theme.DiceMasterTheme
import com.hdarby.dicemaster.viewmodel.DiceUiState
import org.junit.Rule
import org.junit.Test

class DiceMasterUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun diceMasterScreen_initialState_displaysCorrectTitle() {
        composeTestRule.setContent {
            DiceMasterTheme {
                DiceMasterScreen(
                    uiState = DiceUiState(),
                    onUpdateFaces = {},
                    onUpdateQuantity = {},
                    onRollDice = {},
                    onDismissResults = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Dice Master").assertIsDisplayed()
        composeTestRule.onNodeWithText("ROLL DICE").assertIsDisplayed()
    }

    @Test
    fun diceMasterScreen_showResultsTrue_displaysResultsSheet() {
        val results = listOf(5, 10, 15)
        composeTestRule.setContent {
            DiceMasterTheme {
                DiceMasterScreen(
                    uiState = DiceUiState(showResults = true, rollResults = results),
                    onUpdateFaces = {},
                    onUpdateQuantity = {},
                    onRollDice = {},
                    onDismissResults = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Results").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total: 30").assertIsDisplayed()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithText("10").assertIsDisplayed()
        composeTestRule.onNodeWithText("15").assertIsDisplayed()
    }

    @Test
    fun diceMasterScreen_clickRoll_callsOnRollDice() {
        var rollClicked = false
        composeTestRule.setContent {
            DiceMasterTheme {
                DiceMasterScreen(
                    uiState = DiceUiState(),
                    onUpdateFaces = {},
                    onUpdateQuantity = {},
                    onRollDice = { rollClicked = true },
                    onDismissResults = {}
                )
            }
        }

        composeTestRule.onNodeWithText("ROLL DICE").performClick()
        assert(rollClicked)
    }

    @Test
    fun diceMasterScreen_selectors_displayCurrentValues() {
        composeTestRule.setContent {
            DiceMasterTheme {
                DiceMasterScreen(
                    uiState = DiceUiState(faces = 20, quantity = 5),
                    onUpdateFaces = {},
                    onUpdateQuantity = {},
                    onRollDice = {},
                    onDismissResults = {}
                )
            }
        }

        composeTestRule.onNodeWithText("D20").assertIsDisplayed()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
    }
}

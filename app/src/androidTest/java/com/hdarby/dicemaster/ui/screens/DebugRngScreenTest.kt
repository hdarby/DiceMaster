package com.hdarby.dicemaster.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.hdarby.dicemaster.R
import com.hdarby.dicemaster.ui.theme.DiceMasterTheme
import com.hdarby.dicemaster.viewmodel.DieStats
import org.junit.Rule
import org.junit.Test

class DebugRngScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val testD6Stats = DieStats(
        faces = 6,
        frequencies = mapOf(1 to 170, 2 to 165, 3 to 168, 4 to 172, 5 to 163, 6 to 162),
        mean = 3.50,
        stdDev = 1.71,
        min = 1,
        max = 6,
        totalRolls = 1000
    )

    private val testD20Stats = DieStats(
        faces = 20,
        frequencies = (1..20).associateWith { 1000 },
        mean = 10.5,
        stdDev = 5.77,
        min = 1,
        max = 20,
        totalRolls = 20000
    )

    // region DieStatsCard

    @Test
    fun dieStatsCard_d6_displaysDistributionTitle() {
        composeTestRule.setContent {
            DiceMasterTheme {
                DieStatsCard(stats = testD6Stats)
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_die_distribution, 6)
        ).assertIsDisplayed()
    }

    @Test
    fun dieStatsCard_d20_displaysDistributionTitle() {
        composeTestRule.setContent {
            DiceMasterTheme {
                DieStatsCard(stats = testD20Stats)
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_die_distribution, 20)
        ).assertIsDisplayed()
    }

    @Test
    fun dieStatsCard_displaysMinValue() {
        composeTestRule.setContent {
            DiceMasterTheme {
                DieStatsCard(stats = testD6Stats)
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_min, 1)
        ).assertIsDisplayed()
    }

    @Test
    fun dieStatsCard_displaysMaxValue() {
        composeTestRule.setContent {
            DiceMasterTheme {
                DieStatsCard(stats = testD6Stats)
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_max, 6)
        ).assertIsDisplayed()
    }

    @Test
    fun dieStatsCard_displaysTotalRolls() {
        composeTestRule.setContent {
            DiceMasterTheme {
                DieStatsCard(stats = testD6Stats)
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_rolls, 1000)
        ).assertIsDisplayed()
    }

    @Test
    fun dieStatsCard_displaysMean() {
        composeTestRule.setContent {
            DiceMasterTheme {
                DieStatsCard(stats = testD6Stats)
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_mean, "3.50")
        ).assertIsDisplayed()
    }

    @Test
    fun dieStatsCard_displaysStdDev() {
        composeTestRule.setContent {
            DiceMasterTheme {
                DieStatsCard(stats = testD6Stats)
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_std_dev, "1.71")
        ).assertIsDisplayed()
    }

    // endregion

    // region StatsGrid

    @Test
    fun statsGrid_displaysAllStatLabels() {
        composeTestRule.setContent {
            DiceMasterTheme {
                StatsGrid(stats = testD6Stats)
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_min, 1)
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_max, 6)
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_rolls, 1000)
        ).assertIsDisplayed()
    }

    @Test
    fun statsGrid_displaysMeanAndStdDev() {
        composeTestRule.setContent {
            DiceMasterTheme {
                StatsGrid(stats = testD6Stats)
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_mean, "3.50")
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_std_dev, "1.71")
        ).assertIsDisplayed()
    }

    @Test
    fun statsGrid_d20_displaysCorrectValues() {
        composeTestRule.setContent {
            DiceMasterTheme {
                StatsGrid(stats = testD20Stats)
            }
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_min, 1)
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_max, 20)
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.label_rolls, 20000)
        ).assertIsDisplayed()
    }

    // endregion
}



package com.hdarby.dicemaster.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector
import com.hdarby.dicemaster.R

sealed class Screen(val route: String, @StringRes val labelRes: Int, val icon: ImageVector) {
    data object SessionSetup : Screen("session_setup", R.string.nav_label_session_setup, Icons.Default.Groups)
    data object Roller : Screen("roller", R.string.nav_label_roller, Icons.Default.Casino)
    data object Characters : Screen("characters", R.string.nav_label_characters, Icons.Default.Person)
    data object Weapons : Screen("weapons", R.string.nav_label_weapons, Icons.Default.Shield)
    data object Items : Screen("items", R.string.nav_label_items, Icons.Default.Inventory)
    data object Debug : Screen("debug", R.string.nav_label_debug, Icons.AutoMirrored.Filled.Help)
}

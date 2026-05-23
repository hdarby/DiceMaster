package com.hdarby.dicemaster.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Roller : Screen("roller", "Roller", Icons.Default.Casino)
    data object Characters : Screen("characters", "Characters", Icons.Default.Person)
    data object Weapons : Screen("weapons", "Weapons", Icons.Default.Shield)
    data object Items : Screen("items", "Items", Icons.Default.Inventory)
    data object Debug : Screen("debug", "Debug", Icons.AutoMirrored.Filled.Help)
}

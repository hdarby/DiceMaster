package com.hdarby.dicemaster.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Roller : Screen("roller", "Roller", Icons.Default.Casino)
    object Characters : Screen("characters", "Characters", Icons.Default.Person)
    object Weapons : Screen("weapons", "Weapons", Icons.Default.Shield)
}

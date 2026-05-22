package com.hdarby.dicemaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hdarby.dicemaster.ui.navigation.Screen
import com.hdarby.dicemaster.ui.screens.CharacterScreen
import com.hdarby.dicemaster.ui.screens.DebugRngScreen
import com.hdarby.dicemaster.ui.screens.DiceRollerScreen
import com.hdarby.dicemaster.ui.screens.WeaponScreen
import com.hdarby.dicemaster.ui.theme.DiceMasterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiceMasterTheme {
                MainContainer()
            }
        }
    }
}

@Composable
fun MainContainer() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Roller,
        Screen.Characters,
        Screen.Weapons
    )

    Scaffold(
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("bottom_navigation")) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        modifier = Modifier.testTag("nav_item_${screen.route}"),
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any {
                                it.route?.substringBefore("?") == screen.route
                            } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Roller.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Roller.route) { 
                DiceRollerScreen(
                    onNavigateToDebug = { navController.navigate(Screen.Debug.route) }
                ) 
            }
            composable(Screen.Characters.route) {
                CharacterScreen(
                    onNavigateToEditWeapon = { weapon ->
                        navController.navigate("${Screen.Weapons.route}?editWeaponId=${weapon.id}") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                )
            }
            composable(
                route = "${Screen.Weapons.route}?editWeaponId={editWeaponId}",
                arguments = listOf(
                    navArgument("editWeaponId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val rawEditId = backStackEntry.arguments?.getLong("editWeaponId") ?: -1L
                WeaponScreen(editWeaponId = rawEditId.takeIf { it != -1L })
            }
            composable(Screen.Debug.route) {
                DebugRngScreen(onBack = { navController.popBackStack() }) 
            }
        }
    }
}

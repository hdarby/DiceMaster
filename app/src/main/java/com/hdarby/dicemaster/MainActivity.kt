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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hdarby.dicemaster.domain.model.UserRole
import com.hdarby.dicemaster.ui.navigation.Screen
import com.hdarby.dicemaster.ui.screens.CharacterScreen
import com.hdarby.dicemaster.ui.screens.DebugRngScreen
import com.hdarby.dicemaster.ui.screens.DiceRollerScreen
import com.hdarby.dicemaster.ui.screens.ItemScreen
import com.hdarby.dicemaster.ui.screens.SessionSetupScreen
import com.hdarby.dicemaster.ui.screens.WeaponScreen
import com.hdarby.dicemaster.ui.theme.DiceMasterTheme
import com.hdarby.dicemaster.viewmodel.SessionViewModel
import org.koin.androidx.compose.koinViewModel

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
fun MainContainer(sessionViewModel: SessionViewModel = koinViewModel()) {
    val navController = rememberNavController()
    val sessionUiState by sessionViewModel.uiState.collectAsState()

    val allBottomNavItems = listOf(
        Screen.Roller,
        Screen.Characters,
        Screen.Weapons,
        Screen.Items
    )

    val bottomNavItems = when (sessionUiState.currentSession?.role) {
        is UserRole.Player -> allBottomNavItems.filterNot { it == Screen.Items }
        else -> allBottomNavItems
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomNav = currentRoute != Screen.SessionSetup.route

    // Navigate to SessionSetup if session is lost while on another screen
    LaunchedEffect(sessionUiState.isCheckingSession, sessionUiState.currentSession) {
        if (sessionUiState.isCheckingSession) return@LaunchedEffect
        val onSetupScreen = navController.currentDestination?.route == Screen.SessionSetup.route
        if (sessionUiState.currentSession == null && !onSetupScreen) {
            navController.navigate(Screen.SessionSetup.route) {
                popUpTo(Screen.Roller.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(modifier = Modifier.testTag("bottom_navigation")) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            modifier = Modifier.testTag("nav_item_${screen.route}"),
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(stringResource(screen.labelRes)) },
                            selected = navBackStackEntry?.destination?.hierarchy?.any {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.SessionSetup.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.SessionSetup.route) {
                SessionSetupScreen(
                    viewModel = sessionViewModel,
                    onContinueSession = {
                        navController.navigate(Screen.Roller.route) {
                            popUpTo(Screen.SessionSetup.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Roller.route) {
                DiceRollerScreen(
                    onNavigateToDebug = { navController.navigate(Screen.Debug.route) },
                    onLeaveSession = { sessionViewModel.onLeaveSession() }
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
                    },
                    onLeaveSession = { sessionViewModel.onLeaveSession() }
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
                WeaponScreen(
                    editWeaponId = rawEditId.takeIf { it != -1L },
                    onLeaveSession = { sessionViewModel.onLeaveSession() }
                )
            }
            composable(Screen.Debug.route) {
                DebugRngScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Items.route) {
                ItemScreen(onLeaveSession = { sessionViewModel.onLeaveSession() })
            }
        }
    }
}

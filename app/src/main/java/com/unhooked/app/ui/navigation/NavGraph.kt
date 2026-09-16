package com.unhooked.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unhooked.app.ui.block.BlockScreen
import com.unhooked.app.ui.focus.FocusScreen
import com.unhooked.app.ui.home.HomeScreen
import com.unhooked.app.ui.insights.InsightsScreen
import com.unhooked.app.ui.permissions.PermissionsScreen
import com.unhooked.app.ui.settings.AntiBypassScreen
import com.unhooked.app.ui.settings.SettingsScreen

@Composable
fun MainNavHost(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelDestination = Screen.bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isTopLevelDestination) {
                UnhookedBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToFocus = { navController.navigate(Screen.Focus.route) },
                    onNavigateToBlock = { navController.navigate(Screen.Block.route) },
                    onNavigateToPermissions = { navController.navigate(Screen.Permissions.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(Screen.Focus.route) {
                FocusScreen()
            }

            composable(Screen.Block.route) {
                BlockScreen()
            }

            composable(Screen.Insights.route) {
                InsightsScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToPermissions = { navController.navigate(Screen.Permissions.route) },
                    onNavigateToAntiBypass = { navController.navigate(Screen.AntiBypass.route) }
                )
            }

            composable(Screen.Permissions.route) {
                PermissionsScreen()
            }

            composable(Screen.AntiBypass.route) {
                AntiBypassScreen()
            }
        }
    }
}

package com.unhooked.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unhooked.app.UnhookedApp
import com.unhooked.app.ui.block.BlockScreen
import com.unhooked.app.ui.focus.FocusScreen
import com.unhooked.app.ui.home.HomeScreen
import com.unhooked.app.ui.insights.InsightsScreen
import com.unhooked.app.ui.onboarding.OnboardingScreen
import com.unhooked.app.ui.permissions.PermissionsScreen
import com.unhooked.app.ui.settings.AntiBypassScreen
import com.unhooked.app.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun MainNavHost(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelDestination = Screen.bottomNavItems.any { it.route == currentRoute }

    // Check onboarding state
    val prefs = UnhookedApp.repository?.preferences
    val isOnboarded by (prefs?.isOnboardedFlow
        ?: kotlinx.coroutines.flow.flowOf(true)).collectAsState(initial = true)

    val startDestination = if (isOnboarded) Screen.Home.route else Screen.Onboarding.route
    val scope = rememberCoroutineScope()

    Scaffold(
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
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = { goal ->
                        scope.launch {
                            prefs?.setOnboarded(true)
                        }
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

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

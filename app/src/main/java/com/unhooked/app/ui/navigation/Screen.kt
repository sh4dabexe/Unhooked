package com.unhooked.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    object Home : Screen("home", "Home", Icons.Rounded.Home)
    object Focus : Screen("focus", "Focus", Icons.Rounded.HourglassTop)
    object Block : Screen("block", "Block", Icons.Rounded.Block)
    object Insights : Screen("insights", "Insights", Icons.Rounded.Insights)
    object Settings : Screen("settings", "Settings", Icons.Rounded.Settings)

    // Additional sub-screens
    object Permissions : Screen("permissions", "Permissions Center")
    object AntiBypass : Screen("anti_bypass", "Anti-Bypass Protection", Icons.Rounded.Security)
    object Onboarding : Screen("onboarding", "Welcome")

    // Emergency unlock screens
    object QrScanner : Screen("qr_scanner/{scheduleId}", "QR Scanner") {
        fun createRoute(scheduleId: Long) = "qr_scanner/$scheduleId"
    }
    object PassphraseEntry : Screen("passphrase_entry/{scheduleId}", "Passphrase Entry") {
        fun createRoute(scheduleId: Long) = "passphrase_entry/$scheduleId"
    }

    companion object {
        val bottomNavItems = listOf(Home, Focus, Block, Insights, Settings)
    }
}

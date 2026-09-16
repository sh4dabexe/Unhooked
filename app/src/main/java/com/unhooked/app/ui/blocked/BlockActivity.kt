package com.unhooked.app.ui.blocked

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.addCallback
import com.unhooked.app.ui.theme.UnhookedTheme

class BlockActivity : ComponentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_REASON = "extra_reason"
        const val EXTRA_UNLOCK_TIME = "extra_unlock_time"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "Distracting App"
        val rawReason = intent.getStringExtra(EXTRA_REASON) ?: "Focus rule active"
        val unlockTime = intent.getLongExtra(EXTRA_UNLOCK_TIME, 0L)

        val displayReason = when (rawReason) {
            "DAILY_LIMIT" -> "Daily usage limit reached"
            "OVERALL_LIMIT" -> "Overall daily allowance exhausted"
            "SCHEDULED" -> "Scheduled block active"
            "FOCUS_SESSION" -> "Focus session in progress"
            "INSTANT_BLOCK" -> "Manual block active"
            else -> "Blocked by Unhooked"
        }

        // Intercept back button: Always navigate safely to Home rather than revealing the blocked app
        onBackPressedDispatcher.addCallback(this) {
            goToHomeScreen()
        }

        setContent {
            UnhookedTheme {
                BlockedScreen(
                    appName = appName,
                    reason = displayReason,
                    unlockTimeMs = unlockTime,
                    onGoHome = { goToHomeScreen() }
                )
            }
        }
    }

    private fun goToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}

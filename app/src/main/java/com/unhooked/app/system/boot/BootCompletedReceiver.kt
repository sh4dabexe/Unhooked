package com.unhooked.app.system.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.unhooked.app.UnhookedApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.d("BootCompletedReceiver", "Boot completed: verifying active rules and commitments")
            val repository = UnhookedApp.repository ?: return
            CoroutineScope(Dispatchers.IO).launch {
                // Ensure active schedules and commitments remain intact after reboot
                val activeSession = repository.getActiveFocusSession()
                if (activeSession != null && activeSession.endAt <= System.currentTimeMillis()) {
                    repository.completeFocusSession(activeSession.id)
                }
            }
        }
    }
}

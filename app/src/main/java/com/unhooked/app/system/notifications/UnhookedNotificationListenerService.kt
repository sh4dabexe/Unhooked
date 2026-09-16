package com.unhooked.app.system.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.unhooked.app.UnhookedApp
import com.unhooked.app.domain.whitelist.WhitelistHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class UnhookedNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: return

        // Never intercept notifications from critical / emergency apps
        if (WhitelistHelper.isCriticalApp(packageName)) {
            return
        }

        val repository = UnhookedApp.repository ?: return

        serviceScope.launch {
            val activeFocus = repository.getActiveFocusSession()
            if (activeFocus != null && activeFocus.endAt > System.currentTimeMillis()) {
                val shouldBlock = activeFocus.targetPackages.isEmpty() ||
                        activeFocus.targetPackages.contains(packageName)
                if (shouldBlock) {
                    cancelNotification(sbn.key)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

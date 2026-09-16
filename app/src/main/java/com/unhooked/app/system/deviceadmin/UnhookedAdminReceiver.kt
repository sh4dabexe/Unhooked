package com.unhooked.app.system.deviceadmin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.unhooked.app.UnhookedApp
import com.unhooked.app.domain.model.ProtectionMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UnhookedAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "Unhooked Admin Protection Enabled", Toast.LENGTH_SHORT).show()
        UnhookedApp.repository?.let { repo ->
            CoroutineScope(Dispatchers.IO).launch {
                repo.preferences.setProtectionMode(ProtectionMode.ADMIN)
            }
        }
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "Unhooked Admin Protection Disabled", Toast.LENGTH_SHORT).show()
        UnhookedApp.repository?.let { repo ->
            CoroutineScope(Dispatchers.IO).launch {
                repo.preferences.setProtectionMode(ProtectionMode.NORMAL)
            }
        }
    }
}

package com.helper.watchbridge

import android.app.Activity
import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Start local service
        val svc = Intent(this, LocalHttpService::class.java)
        startService(svc)

        findViewById<Button>(R.id.btn_usage)?.setOnClickListener {
            if (!hasUsageAccess()) {
                showUsageAccessDialog()
            } else {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        }
        findViewById<Button>(R.id.btn_notifications)?.setOnClickListener {
            if (!hasNotificationAccess()) {
                showNotificationAccessDialog()
            } else {
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
        }
    }

    private fun hasUsageAccess(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun hasNotificationAccess(): Boolean {
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(packageName) == true
    }

    private fun showUsageAccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Usage Access Needed")
            .setMessage("Projectivy Helper needs Usage Access permission to read app usage data. Please grant it in the next screen.")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNotificationAccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notification Access Needed")
            .setMessage("Projectivy Helper needs Notification Access permission to read media playback notifications. Please grant it in the next screen.")
            .setPositiveButton("Open Settings") { _, _ ->
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

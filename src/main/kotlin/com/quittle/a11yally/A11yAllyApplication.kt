package com.quittle.a11yally

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.net.Uri
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager

private const val TAG = "A11yAllyApplication"

class A11yAllyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializePreferences()
        initializePermissions()
    }

    private fun initializePreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }

    private fun initializePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !Settings.canDrawOverlays(this)) {
            startActivityIntent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, true)
        }

        val serviceEnabled = (getSystemService(Context.ACCESSIBILITY_SERVICE) as
                        AccessibilityManager)
                .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK)
                .map(AccessibilityServiceInfo::getId)
                .any("$packageName/.${OverlayService::class.simpleName}"::equals)

        if (!serviceEnabled) {
            try {
                startActivityIntent(Settings.ACTION_ACCESSIBILITY_SETTINGS, true)
            } catch (e: ActivityNotFoundException) {
                try {
                    startActivityIntent(Settings.ACTION_ACCESSIBILITY_SETTINGS, false)
                } catch (e: ActivityNotFoundException) {
                    Log.w(TAG, "Unable to show accessibility settings")
                }
            }
        }
    }

    private fun startActivityIntent(action: String, targetPackage: Boolean) {
        val intent = if (targetPackage) {
            Intent(action, Uri.parse("package:$packageName"))
        } else {
            Intent(action)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}

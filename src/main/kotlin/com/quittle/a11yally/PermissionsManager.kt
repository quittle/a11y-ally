package com.quittle.a11yally

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import com.quittle.a11yally.analyzer.A11yAllyAccessibilityAnalyzer
import java.lang.RuntimeException

class PermissionsManagerInitializationException(msg: String, cause: Throwable) :
        RuntimeException(msg, cause)

class PermissionsManager(context: Context) {
    private companion object {
        private val ANALYZER_CLASS_NAME = A11yAllyAccessibilityAnalyzer::class.simpleName
    }

    @Suppress("TooGenericExceptionCaught")
    private val mContext: Context = run {
        try {
            context.applicationContext
        } catch (e: NullPointerException) {
            throw PermissionsManagerInitializationException(
                    "Unable to initialize Permissions Manager so soon in the application " +
                    "lifecycle. This usually occurs when trying to construct the class before " +
                    "the application has fully initialized. Instead initialize this lazily or " +
                    "during the onCreate() of your activity or application.", e)
        }
    }
    private val mAccessibilityManager =
            mContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    private val mPackageName = mContext.packageName

    /**
     * Checks if the app has permission to draw over other apps.
     * @return true if it does
     */
    fun hasDrawOverlaysPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(mContext)
    }

    /**
     * Checks if the app is set up and enabled as an accessibility service.
     * @return true if the app is set up as an accessibility service.
     */
    fun hasAccessibilityServicePermission(): Boolean {
        return mAccessibilityManager
                .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK)
                .map(AccessibilityServiceInfo::getId)
                .any("$mPackageName/.analyzer.$ANALYZER_CLASS_NAME"::equals)
    }

    /**
     * Checks that all necessary permissions are available for the app to run correctly.
     * @return true if it is set up fully
     */
    fun hasAllPermissions(): Boolean {
        return hasDrawOverlaysPermission() && hasAccessibilityServicePermission()
    }
}

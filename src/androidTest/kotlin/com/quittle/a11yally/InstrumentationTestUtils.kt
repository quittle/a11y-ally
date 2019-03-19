package com.quittle.a11yally

import android.Manifest
import android.app.Activity
import android.app.UiAutomation
import android.os.ParcelFileDescriptor
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.intent.Intents
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.runner.permission.PermissionRequester
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

/**
 * Wrapper for auto-releasing the espress [Intents] recorder.
 */
fun recordingIntents(r: () -> Unit) {
    Intents.init()
    try {
        r.invoke()
    } finally {
        Intents.release()
    }
}

/**
 * Gets the current, foreground activity
 */
fun getCurrentActivity(): Activity {
    var currentActivity: Activity? = null

    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        currentActivity = ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(Stage.RESUMED)
                .iterator()
                .next()
    }

    return currentActivity!!
}

/**
 * Grants all the permissions to the application under test
 */
@Suppress("SpreadOperator")
fun grantPermissions(vararg permissions: String) {
    PermissionRequester().apply {
        addPermissions(*permissions)
        requestPermissions()
    }
}

/**
 * Revokes all the permissions from the application under test
 */
fun revokePermissions(vararg permissions: String) {
    val packageName = getPackageName()
    permissions.forEach { permission ->
        runShellCommand("pm revoke $packageName $permission")

        val simplePermission = permission.replaceFirst("android.permission.", "")
        runShellCommand("appops set $packageName $simplePermission default")
    }
}

/**
 * Enables the app to run as an accessibility service
 */
fun enableAccessibilityService() {
    runShellCommand("settings put secure enabled_accessibility_services " +
            getPackageName() + "/.analyzer.A11yAllyAccessibilityAnalyzer")
}

/**
 * Disallows the app from running as an accessibility service
 */
fun disableAccessibilityService() {
    runShellCommand("settings delete secure enabled_accessibility_services")
}

/**
 * Sets up {@code A11yAlly} with all required permissions. Helpful for tests that expect the app to
 * be ready to go.
 */
fun fullySetUpPermissions() {
    grantPermissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
    enableAccessibilityService()
}

/**
 * Blocks the thread until a file has been fully created.
 */
fun waitForFile(file: File) {
    while (!isFileClosed(file)) {
        onIdle()
    }
    onIdle()
}

private fun isFileClosed(file: File): Boolean {
    try {
        RandomAccessFile(file, "rws").close()
        return true
    } catch (e: IOException) {
        return false
    }
}

private fun runShellCommand(command: String) {
    val pfd: ParcelFileDescriptor = InstrumentationRegistry.getInstrumentation()
            .getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
            .executeShellCommand(command)

    // If the file descriptor isn't read and closed, an I/O error crashes the tests so it must
    // be read fully and closed after. This has the added benefit of ensuring the command runs
    // completely before this function returns.
    ParcelFileDescriptor.AutoCloseInputStream(pfd).use {
        val bytes = ByteArray(1024)
        do while (it.read(bytes) != -1)
    }
}

private fun getPackageName(): String {
    return InstrumentationRegistry.getInstrumentation().targetContext.packageName
}
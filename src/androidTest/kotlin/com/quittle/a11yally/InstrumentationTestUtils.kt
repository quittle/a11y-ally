package com.quittle.a11yally

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.UiAutomation
import android.content.Intent
import android.os.ParcelFileDescriptor
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.intent.Intents
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.runner.permission.PermissionRequester
import org.json.JSONArray
import org.json.JSONException
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import kotlin.reflect.KClass

/**
 * Sets up and tears down permissions around a test
 */
class PermissionsRule : TestRule {
    override fun apply(base: Statement?, description: Description?): Statement =
            object : Statement() {
        override fun evaluate() {
            try {
                fullySetUpPermissions()
                base?.evaluate()
            } finally {
                fullyTearDownPermissions()
            }
        }
    }
}

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
    onIdle()
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
 * Disables all the permission granted to {@code A11yAlly}. Helpful for tests that need to start in
 * a clean state.
 */
fun fullyTearDownPermissions() {
    revokePermissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
    disableAccessibilityService()
}

/**
 * Wipes all shared preferences of the application under test
 */
// Setting preferences of another context is restricted
@SuppressLint("RestrictedApi", "ApplySharedPref")
fun clearSharedPreferences() {
    val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    PreferenceManager(targetContext).sharedPreferences.edit()
            .clear()
            .commit()
}

/**
 * Launches an activity in the app under test
 */
fun <T : Activity> launchActivity(clazz: KClass<T>): T {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val intent = Intent(instrumentation.targetContext, clazz.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    @Suppress("unchecked_cast")
    return instrumentation.startActivitySync(intent) as T
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

fun waitForJsonArrayFile(file: File) {
    waitForFile(file)

    while (!isFileJsonArray(file)) {
        onIdle()
    }
}

private fun isFileJsonArray(file: File): Boolean {
    @Suppress("EmptyCatchBlock")
    try {
        JSONArray(file.readText())
        return true
    } catch (e: IOException) {
    } catch (e: JSONException) {
    }
    return false
}

private fun isFileClosed(file: File): Boolean {
    try {
        RandomAccessFile(file, "rws").close()
        return true
    } catch (e: IOException) {
        return false
    }
}

fun runShellCommand(command: String) {
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

/**
 * Extension function to launch an activity without parameters
 */
fun <T : Activity> ActivityTestRule<T>.launchActivity(): T {
    return this.launchActivity(Intent())
}

/**
 * Extension function to stop and re-start the activity of a rule
 */
fun <T : Activity> ActivityTestRule<T>.relaunchActivity(): T {
    this.activity.finish()
    return this.launchActivity()
}

/**
 * An [ActivityTestRule] that does not launch the activity at startup
 * @param activity The activity to launch
 */
class DelayedActivityTestRule<T : Activity>(activity: KClass<T>) :
        ActivityTestRule<T>(activity.java, true, false)

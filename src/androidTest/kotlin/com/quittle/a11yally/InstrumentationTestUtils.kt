package com.quittle.a11yally

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.UiAutomation
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat.getSystemService
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.runner.permission.PermissionRequester
import androidx.test.uiautomator.Configurator
import androidx.test.uiautomator.UiDevice
import com.quittle.a11yally.analyzer.A11yAllyAccessibilityAnalyzer
import com.quittle.a11yally.preferences.PreferenceProvider
import com.quittle.a11yally.preferences.withPreferenceProvider
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.json.JSONArray
import org.json.JSONException
import org.junit.Assume.assumeThat
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.lang.RuntimeException
import java.lang.Thread.sleep
import java.nio.charset.StandardCharsets
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
 * Wrapper for auto-releasing the Espresso [Intents] recorder.
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
    assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(24))
    runShellCommand("settings put secure enabled_accessibility_services " +
            getPackageName() + "/.analyzer.A11yAllyAccessibilityAnalyzer")

    // Depending on the OS version, enabling the service may be asynchronous
    val maxWaitMs = 1000
    val pollingDelayMs = 100L
    for (i in 0..(maxWaitMs / pollingDelayMs)) {
        if (isA11yAllyAccessibilityAnalyzerRunning()) {
            return
        } else {
            sleep(pollingDelayMs)
            onIdle()
        }
    }

    throw RuntimeException("Unable to start accessibility service")
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
@SuppressLint("ApplySharedPref")
fun clearSharedPreferences() {
    sharedPreferences().edit()
            .clear()
            .commit()
}

fun sharedPreferences(): SharedPreferences {
    val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    return PreferenceManager.getDefaultSharedPreferences(targetContext)
}

class ViewActionCheck(private val check: (view: View) -> Unit) : ViewAction {
    override fun getDescription(): String {
        return "Performs checks as an action"
    }

    override fun getConstraints(): Matcher<View> {
        return Matchers.any(View::class.java)
    }

    override fun perform(uiController: UiController?, view: View) {
        check(view)
    }
}

/**
 * Launches an activity in the app under test
 * @param activity The activity to launch
 * @return The activity instance created
 */
fun <T : Activity> launchActivity(clazz: KClass<T>): T {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val intent = Intent(instrumentation.targetContext, clazz.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    // On API level 24, even though it technically supports instrumentation.getUiAutomation(int),
    // it does not work so this workaround is necssary to allow it to start accessibility services.
    // See https://stackoverflow.com/a/55636900/1554990.
    if (Build.VERSION.SDK_INT == 24) {
        val flags = UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES
        Configurator.getInstance().uiAutomationFlags = flags

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val output = device.executeShellCommand(command)

        if (output.isNotEmpty()) {
            Log.d("cmd", output)
        }
    } else {
        val uiAutomation = if (Build.VERSION.SDK_INT < 24) {
            // Commands to enable an accessibility service below API version 24 won't work.
            instrumentation.uiAutomation
        } else {
            instrumentation.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
        }
        val pfd: ParcelFileDescriptor = uiAutomation.executeShellCommand(command)

        // If the file descriptor isn't read and closed, an I/O error crashes the tests so it must
        // be read fully and closed after. This has the added benefit of ensuring the command runs
        // completely before this function returns.
        ParcelFileDescriptor.AutoCloseInputStream(pfd).use {
            val bytes = ByteArray(1024)
            while (it.read(bytes) != -1) {
                Log.d("cmd", bytes.toString(StandardCharsets.UTF_8))
            }
        }
    }
}

/**
 * @return True if the service is running, otherwise false.
 */
fun isA11yAllyAccessibilityAnalyzerRunning(): Boolean {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    @Suppress("deprecation")
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (A11yAllyAccessibilityAnalyzer::class.java.name == service.service.className) {
            return true
        }
    }
    return false
}

fun withPreferenceProvider(block: PreferenceProvider.() -> Unit) {
    withPreferenceProvider(InstrumentationRegistry.getInstrumentation().targetContext) {
        block(this)
    }
}

private fun getPackageName(): String {
    return InstrumentationRegistry.getInstrumentation().targetContext.packageName
}

private fun getTestPackageName(): String {
    return InstrumentationRegistry.getInstrumentation().context.packageName
}

fun hasTextColorFromAttribute(@AttrRes attrId: Int): Matcher<View> {
    return object : BoundedMatcher<View, TextView>(TextView::class.java) {
        private var context: Context? = null

        override fun matchesSafely(textView: TextView): Boolean {
            context = textView.context
            val textViewColor = textView.currentTextColor
            val expectedColor = context?.resolveAttributeResourceValue(attrId)
            return textViewColor == expectedColor
        }

        override fun describeTo(description: org.hamcrest.Description) {
            val colorId = context?.resources?.getResourceName(attrId)?.orElse(attrId.toString())
            description.appendText("View with text color matching resource id $colorId")
        }
    }
}

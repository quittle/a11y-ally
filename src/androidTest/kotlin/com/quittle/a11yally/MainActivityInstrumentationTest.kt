package com.quittle.a11yally

import android.Manifest
import android.app.Activity
import android.app.UiAutomation
import android.os.ParcelFileDescriptor
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.intent.Intents
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.runner.permission.PermissionRequester
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.times
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.junit.After
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentationTest {
    private companion object {
        private const val SETTINGS_PACKAGE_NAME = "com.android.settings"
    }

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @After
    @Before
    fun clearPermissions() {
        disableAccessibilityService()
        revokePermissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
    }

    @Test
    fun pressToggleButtons() {
        arrayOf(R.id.toggle_highlight_issues,
                R.id.toggle_display_content_descriptions).forEach { id ->
            onView(withId(id))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .check(matches(isClickable()))
                    .perform(click())
                    .check(matches(isDisplayed()))
                    .check(matches(isClickable()))
                    .perform(click())

            assertSame(MainActivity::class.java, getCurrentActivity().javaClass)
        }
    }

    @Test
    fun pressPermissionsCheck_none() {
        recordingIntents {
            onView(withId(R.id.permissions_check))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .check(matches(isClickable()))
                    .perform(click())

            intended(toPackage(SETTINGS_PACKAGE_NAME), times(2))
        }
    }

    @Test
    fun pressPermissionsCheck_hasSystemAlertWindowPermission() {
        grantPermissions(Manifest.permission.SYSTEM_ALERT_WINDOW)

        recordingIntents {
            onView(withId(R.id.permissions_check))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .check(matches(isClickable()))
                    .perform(click())

            intended(toPackage(SETTINGS_PACKAGE_NAME))
        }
    }

    @Test
    fun pressPermissionsCheck_hasAccessibilityPermission() {
        enableAccessibilityService()

        recordingIntents {
            onView(withId(R.id.permissions_check))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .check(matches(isClickable()))
                    .perform(click())

            intended(toPackage(SETTINGS_PACKAGE_NAME))
        }
    }

    @Test
    fun pressPermissionsCheck_hasAllPermissions() {
        grantPermissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
        enableAccessibilityService()

        recordingIntents {
            onView(withId(R.id.permissions_check))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .check(matches(isClickable()))
                    .perform(click())
            Intents.assertNoUnverifiedIntents()
        }
    }

    @Test
    fun pressUnfiendlyActivityButton() {
        onView(withId(R.id.open_unfriendly_activity_button))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .check(matches(isClickable()))
                .perform(click())

        assertSame(UnfriendlyActivity::class.java, getCurrentActivity().javaClass)
    }

    private fun recordingIntents(r: () -> Unit) {
        Intents.init()
        try {
            r.invoke()
        } finally {
            Intents.release()
        }
    }

    private fun getCurrentActivity(): Activity {
        var currentActivity: Activity? = null

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            currentActivity = ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(Stage.RESUMED)
                    .iterator()
                    .next()
        }

        return currentActivity!!
    }

    private fun grantPermissions(vararg permissions: String) {
        PermissionRequester().apply {
            addPermissions(*permissions)
            requestPermissions()
        }
    }

    private fun revokePermissions(vararg permissions: String) {
        val packageName = getPackageName()
        permissions.forEach { permission ->
            runShellCommand("pm revoke $packageName $permission")

            val simplePermission = permission.replaceFirst("android.permission.", "")
            runShellCommand("appops set $packageName $simplePermission default")
        }
    }

    private fun enableAccessibilityService() {
        runShellCommand("settings put secure enabled_accessibility_services " +
                getPackageName() + "/.analyzer.A11yAllyAccessibilityAnalyzer")
    }

    private fun disableAccessibilityService() {
        runShellCommand("settings delete secure enabled_accessibility_services")
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
}

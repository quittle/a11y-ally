package com.quittle.a11yally

import android.Manifest
import android.annotation.TargetApi
import android.app.Instrumentation
import android.view.View
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.internal.inject.InstrumentationContext
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.quittle.a11yally.preferences.PreferenceProvider
import org.hamcrest.Matchers.any
import org.junit.After
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
@TargetApi(18)
class ScreenshotInstrumentationTest {
    companion object {
        @BeforeClass
        @JvmStatic fun setUpClass() {
            Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        }
    }

    @get:Rule
    val mDisableAnimationsRule = DisableAnimationsRule()

    @get:Rule
    val mGrantPermissionsRule =
            GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)

//    @get:Rule
//    val mFlavorRule = FlavorRule("screenshot")

    @After
    fun tearDown() {
        fullyTearDownPermissions()
    }

    @Test
    fun permissionsActivity() {
        grantPermissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
        launchActivity(PermissionsActivity::class)

        onIdle()
        onView(withText(R.string.permissions_activity_status_ok))
        onIdle()

        Screengrab.screenshot("permissions_activity")
    }

    @Test
    fun mainActivity() {
        fullySetUpPermissions()
        launchActivity(MainActivity::class)

        onIdle()
        onView(withId(R.id.service_check_box))
        onIdle()

        Screengrab.screenshot("main_activity")
    }

    @Test
    fun playgroundActivity() {
        fullySetUpPermissions()
        launchActivity(MainActivity::class)

        onIdle()
        onView(withText(R.string.explore_unfriendly_activity))
        onIdle()

        Screengrab.screenshot("playground_activity")

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val p = PreferenceProvider(context)
        p.onResume()
        p.setServiceEnabled(true)
        p.setHighlightIssues(true)
        onIdle()

        // Even though you can't see the overlay, I suspect it's on because you don't see it during the logger test
        launchActivity(UnfriendlyActivity::class)
//        val sharedPreferences = PreferenceManager(context).sharedPreferences
//        sharedPreferences.edit()
//                .putBoolean(context.getString(R.string.pref_service_enabled), true)
//                .putBoolean(context.getString(R.string.pref_highlight_issues), false)
//                .putBoolean(
//                        context.getString(R.string.pref_highlight_small_touch_targets), true)
//                .commit()
//        sleep(100)
//        onIdle()
//        sharedPreferences.edit()
//                .putBoolean(context.getString(R.string.pref_highlight_issues), true)
//                .commit()

        onView(withText(R.string.explore_unfriendly_activity))
                .perform(swipeUp())

        onIdle()
        sleep(5000)
        onIdle()

        Screengrab.screenshot("playground_activity_highlight_issues")
    }
}

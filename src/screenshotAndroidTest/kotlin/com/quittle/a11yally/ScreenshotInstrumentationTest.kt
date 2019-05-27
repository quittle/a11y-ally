package com.quittle.a11yally

import android.Manifest
import android.annotation.TargetApi
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.After
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy

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

    @get:Rule
    val mFlavorRule = FlavorRule("screenshot")

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
        launchActivity(UnfriendlyActivity::class)

        onIdle()
        onView(withText(R.string.explore_unfriendly_activity))
        onIdle()

        Screengrab.screenshot("playground_activity")
    }
}

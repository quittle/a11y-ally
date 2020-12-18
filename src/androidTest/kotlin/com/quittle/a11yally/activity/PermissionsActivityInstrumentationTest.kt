package com.quittle.a11yally.activity

import android.Manifest
import android.os.Build
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.PositionAssertions.isBottomAlignedWith
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyRightOf
import androidx.test.espresso.assertion.PositionAssertions.isTopAlignedWith
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.SdkSuppress
import com.quittle.a11yally.R
import com.quittle.a11yally.base.ifElse
import com.quittle.a11yally.enableAccessibilityService
import com.quittle.a11yally.fullySetUpPermissions
import com.quittle.a11yally.fullyTearDownPermissions
import com.quittle.a11yally.getCurrentActivity
import com.quittle.a11yally.grantPermissions
import com.quittle.a11yally.hasTextColorFromAttribute
import com.quittle.a11yally.launchActivity
import com.quittle.a11yally.recordingIntents
import com.quittle.a11yally.withPreferenceProvider
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.lang.Thread.sleep

class PermissionsActivityInstrumentationTest {
    @After
    fun tearDown() {
        fullyTearDownPermissions()
    }

    @Before
    fun setUp() {
        fullyTearDownPermissions()

        withPreferenceProvider {
            setShowTutorial(false)
        }

        launchActivity(PermissionsActivity::class)
    }

    @Test
    fun continueButtonStatus() {
        onView(withId(R.id.continue_button))
            .perform(scrollTo())
            .perform(swipeUp())
            .check(matches(isCompletelyDisplayed()))
            .check(matches(not(isEnabled())))
            .perform(click())
        onIdle()

        assertEquals(PermissionsActivity::class.java, getCurrentActivity().javaClass)

        fullySetUpPermissions()

        launchActivity(PermissionsActivity::class)

        sleep(1500)

        onView(withId(R.id.continue_button))
            .perform(scrollTo())
            .perform(swipeUp())
            .check(matches(isCompletelyDisplayed()))
            .check(matches(isEnabled()))
            .perform(click())

        onIdle()

        assertEquals(MainActivity::class.java, getCurrentActivity().javaClass)
    }

    @Test
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.LOLLIPOP_MR1)
    fun overlayStatus_androidBelowM() {
        verifyStatusViews(
            textViewId = R.id.permission_overlay_text,
            imageViewId = R.id.permission_overlay_image,
            statusViewId = R.id.permission_overlay_status,
            statusOk = true
        )
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.M)
    fun overlayStatus_androidMOrHigher() {
        verifyStatusViews(
            textViewId = R.id.permission_overlay_text,
            imageViewId = R.id.permission_overlay_image,
            statusViewId = R.id.permission_overlay_status,
            statusOk = false
        )

        recordingIntents {
            onView(withId(R.id.permission_overlay_status))
                .perform(click())

            Intents.intended(
                allOf(
                    toPackage("com.android.settings"),
                    hasAction("android.settings.action.MANAGE_OVERLAY_PERMISSION"),
                    hasData("package:com.quittle.a11yally")
                ),
                Intents.times(1)
            )
        }

        grantPermissions(Manifest.permission.SYSTEM_ALERT_WINDOW)

        launchActivity(PermissionsActivity::class)

        // Wait for the permission to propagate
        sleep(2000)

        verifyStatusViews(
            textViewId = R.id.permission_overlay_text,
            imageViewId = R.id.permission_overlay_image,
            statusViewId = R.id.permission_overlay_status,
            statusOk = true
        )
    }

    @Test
    fun serviceStatus() {
        verifyStatusViews(
            textViewId = R.id.permission_service_text,
            imageViewId = R.id.permission_service_image,
            statusViewId = R.id.permission_service_status,
            statusOk = false
        )

        recordingIntents {
            onView(withId(R.id.permission_service_status))
                .perform(click())

            Intents.intended(
                allOf(
                    hasAction("android.settings.ACCESSIBILITY_SETTINGS"),
                    hasData("package:com.quittle.a11yally")
                ),
                Intents.times(1)
            )
            Intents.intended(
                allOf(
                    toPackage("com.android.settings"),
                    hasAction("android.settings.ACCESSIBILITY_SETTINGS")
                ),
                Intents.times(1)
            )
        }

        enableAccessibilityService()

        launchActivity(PermissionsActivity::class)

        verifyStatusViews(
            textViewId = R.id.permission_service_text,
            imageViewId = R.id.permission_service_image,
            statusViewId = R.id.permission_service_status,
            statusOk = true
        )
    }

    /**
     * Verifies that the status views are displaying correctly and do nothing if [statusOk] is true.
     */
    private fun verifyStatusViews(
        textViewId: Int,
        imageViewId: Int,
        statusViewId: Int,
        statusOk: Boolean
    ) {
        val textView = withId(textViewId)
        val imageView = withId(imageViewId)
        val statusView = withId(statusViewId)

        onView(withChild(textView))
            .perform(scrollTo())
            .check(matches(isCompletelyDisplayed()))
            .check(matches(statusOk.ifElse(not(isEnabled()), isEnabled())))
            .check(matches(isClickable()))
        onView(textView)
            .check(matches(isCompletelyDisplayed()))
        onView(imageView)
            .check(matches(isCompletelyDisplayed()))
            .check(isCompletelyRightOf(textView))
            .check(isTopAlignedWith(textView))
            .check(isBottomAlignedWith(textView))
        onView(statusView)
            .check(matches(isCompletelyDisplayed()))
            .check(
                matches(
                    withText(
                        statusOk.ifElse(
                            R.string.permissions_activity_status_ok,
                            R.string.permissions_activity_status_fix
                        )
                    )
                )
            )
            .check(
                matches(
                    hasTextColorFromAttribute(
                        statusOk.ifElse(
                            R.attr.primary_action_disabled_text,
                            R.attr.primary_action_enabled_text
                        )
                    )
                )
            )
            .check(isCompletelyRightOf(imageView))
            .check(isTopAlignedWith(imageView))
            .check(isBottomAlignedWith(imageView))

        if (statusOk) {
            verifyViewInert(textViewId)
            verifyViewInert(imageViewId)
            verifyViewInert(statusViewId)
        }
    }

    /**
     * Clicks a view and verifies that clicking it doesn't leave the activity
     */
    private fun verifyViewInert(viewId: Int) {
        onView(withId(viewId))
            .perform(click())
        onIdle()
        assertEquals(PermissionsActivity::class.java, getCurrentActivity().javaClass)
    }
}

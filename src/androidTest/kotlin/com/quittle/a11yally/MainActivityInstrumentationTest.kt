package com.quittle.a11yally

import android.Manifest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.intent.Intents
import androidx.test.rule.ActivityTestRule
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.times
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.hamcrest.Matchers.not
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
    fun pressHighlightIssuesButton() {
        onView(withId(R.id.toggle_highlight_issues))
                .perform(scrollTo())
                .perform(click())

        onView(withId(R.id.hero_image))
                .check(matches(isDisplayed()))
                .check(matches(not(isClickable())))

        onView(withId(R.id.settings_holder))
                .check(matches(isDisplayed()))
                .check(matches(not(isClickable())))

        assertSame(FeatureSettingsActivity::class.java, getCurrentActivity().javaClass)
    }

    @Test
    fun pressToggleButtons() {
        arrayOf(R.id.toggle_display_content_descriptions,
                R.id.toggle_service_enable).forEach { id ->
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
}

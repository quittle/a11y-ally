package com.quittle.a11yally.activity

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quittle.a11yally.DisableAnimationsRule
import com.quittle.a11yally.PermissionsRule
import com.quittle.a11yally.R
import com.quittle.a11yally.clearSharedPreferences
import com.quittle.a11yally.fullyTearDownPermissions
import com.quittle.a11yally.getCurrentActivity
import com.quittle.a11yally.launchActivity
import com.quittle.a11yally.withPreferenceProvider
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentationTest {
    @get:Rule
    val mPermissionsRule = PermissionsRule()

    @get:Rule
    val mDisableAnimationsRule = DisableAnimationsRule()

    @Test
    fun firstTimeLaunch() {
        clearSharedPreferences()
        fullyTearDownPermissions()

        onIdle()

        ActivityScenario.launch(MainActivity::class.java).use {
            onIdle()

            assertSame(WelcomeActivity::class.java, getCurrentActivity().javaClass)
        }
    }

    @Test
    fun launchWithoutPermissionsAfterTutorial() {
        fullyTearDownPermissions()
        disableTutorial()

        ActivityScenario.launch(MainActivity::class.java).use {
            onIdle()

            assertSame(PermissionsActivity::class.java, getCurrentActivity().javaClass)
        }
    }

    @Test
    fun pressHighlightIssuesButton() {
        disableTutorial()

        launchActivity(MainActivity::class)

        onView(withId(R.id.highlight_issues))
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
        disableTutorial()

        launchActivity(MainActivity::class)

        arrayOf(R.id.display_content_descriptions, R.id.highlight_issues).forEach { id ->
            onView(withId(id))
                    .perform(scrollTo())
            onView(allOf(withId(R.id.switch_compat), isDescendantOfA(withId(id))))
                    .perform(scrollTo())
                    .check(matches(isCompletelyDisplayed()))
                    .check(matches(isClickable()))
                    .perform(click())
                    .check(matches(isCompletelyDisplayed()))
                    .check(matches(isClickable()))
                    .perform(click())

            assertSame(MainActivity::class.java, getCurrentActivity().javaClass)
        }
    }

    @Test
    fun pressSelectAppsToInspectActivityButton() {
        disableTutorial()
        launchActivity(MainActivity::class)
        onView(withId(R.id.toggle_app_selection))
                .perform(scrollTo())
                .check(matches(isCompletelyDisplayed()))
                .check(matches(isClickable()))
                .perform(click())

        assertSame(MultiAppSelectionActivity::class.java, getCurrentActivity().javaClass)
    }

    @Test
    fun pressUnfriendlyActivityButton() {
        disableTutorial()

        launchActivity(MainActivity::class)

        onView(withId(R.id.open_unfriendly_activity_button))
                .perform(scrollTo())
                .check(matches(isCompletelyDisplayed()))
                .check(matches(isClickable()))
                .perform(click())

        assertSame(UnfriendlyActivity::class.java, getCurrentActivity().javaClass)
    }

    private fun disableTutorial() {
        withPreferenceProvider {
            setShowTutorial(false)
        }
    }
}

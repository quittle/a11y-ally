package com.quittle.a11yally

import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentationTest {
    @get:Rule
    val mActivityRule = DelayedActivityTestRule(MainActivity::class)

    @get:Rule
    val mPermissionsRule = PermissionsRule()

    @Test
    fun launchWithoutPermissions() {
        fullyTearDownPermissions()
        mActivityRule.launchActivity()

        onIdle()

        assertSame(PermissionsActivity::class.java, getCurrentActivity().javaClass)
    }

    @Test
    fun pressHighlightIssuesButton() {
        mActivityRule.launchActivity()

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
        mActivityRule.launchActivity()

        arrayOf(R.id.highlight_issues,
                R.id.display_content_descriptions).forEach { id ->
            onView(allOf(withId(R.id.switch_compat), isDescendantOfA(withId(id))))
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
    fun pressUnfriendlyActivityButton() {
        mActivityRule.launchActivity()

        onView(withId(R.id.open_unfriendly_activity_button))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .check(matches(isClickable()))
                .perform(click())

        assertSame(UnfriendlyActivity::class.java, getCurrentActivity().javaClass)
    }
}

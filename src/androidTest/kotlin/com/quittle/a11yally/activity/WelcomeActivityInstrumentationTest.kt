package com.quittle.a11yally.activity

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.quittle.a11yally.R
import com.quittle.a11yally.clearSharedPreferences
import com.quittle.a11yally.fullyTearDownPermissions
import com.quittle.a11yally.getCurrentActivity
import com.quittle.a11yally.launchActivity
import org.junit.Assert.assertEquals
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class WelcomeActivityInstrumentationTest {
    @get:Rule
    val mActivityRule = ActivityTestRule(WelcomeActivity::class.java)

    @Before
    fun setUp() {
        fullyTearDownPermissions()
    }

    @Test
    fun testGetStartedButton() {
        onView(withId(R.id.get_started))
                .perform(scrollTo())
                .check(matches(isCompletelyDisplayed()))
                .perform(click())
        assertEquals(PermissionsActivity::class.java, getCurrentActivity().javaClass)
    }

    @Test
    fun testLearnMoreButton() {
        onView(withId(R.id.learn_more))
                .perform(scrollTo())
                .check(matches(isCompletelyDisplayed()))
                .perform(click())
        assertEquals(LearnMoreActivity::class.java, getCurrentActivity().javaClass)
    }

    @Test
    fun testGetStartedDoesNotReappearAfterGettingStarted() {
        clearSharedPreferences()

        launchActivity(MainActivity::class)

        assertEquals(WelcomeActivity::class.java, getCurrentActivity().javaClass)

        onView(withId(R.id.get_started))
                .perform(scrollTo(), click())

        assertEquals(PermissionsActivity::class.java, getCurrentActivity().javaClass)

        launchActivity(MainActivity::class)

        assertEquals(PermissionsActivity::class.java, getCurrentActivity().javaClass)
    }
}

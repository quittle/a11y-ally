package com.quittle.a11yally.activity.welcome

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quittle.a11yally.R
import com.quittle.a11yally.activity.LearnMoreActivity
import com.quittle.a11yally.activity.MainActivity
import com.quittle.a11yally.activity.PermissionsActivity
import com.quittle.a11yally.clearSharedPreferences
import com.quittle.a11yally.fullyTearDownPermissions
import com.quittle.a11yally.getCurrentActivity
import com.quittle.a11yally.launchActivity
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WelcomeActivityInstrumentationTest {
    @Before
    fun setUp() {
        fullyTearDownPermissions()
        launchActivity(WelcomeActivity::class)
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
    @Ignore(
        "Does not apply with Welcome 2 Activity as the official activity. " +
            "Will be removed in a subsequent commit."
    )
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

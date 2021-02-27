package com.quittle.a11yally.activity

import android.annotation.SuppressLint
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quittle.a11yally.DisableAnimationsRule
import com.quittle.a11yally.R
import com.quittle.a11yally.activity.welcome.Welcome2Activity
import com.quittle.a11yally.clearSharedPreferences
import com.quittle.a11yally.fullySetUpPermissions
import com.quittle.a11yally.fullyTearDownPermissions
import com.quittle.a11yally.getCurrentActivity
import com.quittle.a11yally.launchActivity
import com.quittle.a11yally.withPreferenceProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith

@SuppressLint("PrivateResource")
@RunWith(AndroidJUnit4::class)
class LearnMoreActivityInstrumentationTest {
    @get:Rule
    val mDisableAnimationsRule = DisableAnimationsRule()

    @Before
    fun setUp() {
        fullyTearDownPermissions()
        clearSharedPreferences()
        launchActivity(LearnMoreActivity::class)
    }

    @Test
    fun getStartedButtonWorks() {
        onView(withId(R.id.get_started))
            .perform(scrollTo())
            .check(matches(isCompletelyDisplayed()))
            .perform(click())

        assertEquals(Welcome2Activity::class.java, getCurrentActivity().javaClass)

        // Check in list view
        onView(withText(R.string.welcome2_activity_pick_subtitle))
    }

    @Test
    fun getStartedButtonHiddenAfterTutorial() {
        withPreferenceProvider {
            setShowTutorial(false)
        }

        launchActivity(LearnMoreActivity::class)

        onView(withId(R.id.get_started))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    @Test
    fun backButtonWorksFromWelcomeActivity() {
        launchActivity(Welcome2Activity::class)

        onView(withId(R.id.learn_more))
            .perform(scrollTo(), click())

        onView(withContentDescription(R.string.abc_action_bar_up_description))
            .check(matches(isCompletelyDisplayed()))
            .perform(click())

        assertEquals(Welcome2Activity::class.java, getCurrentActivity().javaClass)

        // Check in default view
        onView(withText(R.string.welcome2_activity_subtitle))
    }

    @Test
    fun backButtonWorksFromMainActivity() {
        fullySetUpPermissions()

        launchActivity(MainActivity::class)

        onView(withContentDescription(R.string.abc_action_menu_overflow_description))
            .perform(click())

        onView(withText(R.string.show_learn_more))
            .perform(click())

        assertEquals(LearnMoreActivity::class.java, getCurrentActivity().javaClass)

        onView(withContentDescription(R.string.abc_action_bar_up_description))
            .perform(click())

        assertEquals(MainActivity::class.java, getCurrentActivity().javaClass)
    }
}

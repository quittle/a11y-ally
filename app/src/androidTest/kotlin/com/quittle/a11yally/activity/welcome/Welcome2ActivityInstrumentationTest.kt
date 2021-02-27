package com.quittle.a11yally.activity.welcome

import android.transition.TransitionInflater
import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.assertion.ViewAssertions.selectedDescendantsMatch
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.transition.TransitionManager
import com.quittle.a11yally.DisableAnimationsRule
import com.quittle.a11yally.R
import com.quittle.a11yally.activity.LearnMoreActivity
import com.quittle.a11yally.activity.MainActivity
import com.quittle.a11yally.activity.PermissionsActivity
import com.quittle.a11yally.between
import com.quittle.a11yally.clearSharedPreferences
import com.quittle.a11yally.fullyTearDownPermissions
import com.quittle.a11yally.getCurrentActivity
import com.quittle.a11yally.instanceOf
import com.quittle.a11yally.launchActivity
import com.quittle.a11yally.runBlockingOnUiThread
import com.quittle.a11yally.targetContext
import com.quittle.a11yally.testPackageName
import com.quittle.a11yally.withWidthAndHeight
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import java.lang.Thread.sleep
import com.quittle.a11yally.test.BuildConfig as TestBuildConfig

@RunWith(AndroidJUnit4::class)
class Welcome2ActivityInstrumentationTest {
    @get:Rule
    val mDisableAnimationsRule = DisableAnimationsRule()

    lateinit var activity: Welcome2Activity

    companion object {
        val transitionDuration by lazy {
            TransitionInflater.from(targetContext())
                .inflateTransition(R.transition.welcome_transition)
                .duration
        }
    }

    @Before
    fun setUp() {
        fullyTearDownPermissions()
        activity = launchActivity(Welcome2Activity::class)
    }

    @Test
    fun testTransitionToListViewHappyCase() {
        onView(withId(R.id.title_banner))
            .check(matches(isCompletelyDisplayed()))

        val banner = activity.findViewById<View>(R.id.title_banner)
        val origBannerWidth = banner.width
        val origBannerHeight = banner.height

        onView(withId(R.id.get_started))
            .perform(scrollTo())
            .check(matches(isCompletelyDisplayed()))
            .perform(click())
            .check(doesNotExist())

        waitForTransitionToEnd()

        onView(withId(R.id.title_banner))
            .check(matches(isCompletelyDisplayed()))
            .check(
                matches(
                    withWidthAndHeight(
                        equalTo(origBannerWidth),
                        between(origBannerHeight / 2, origBannerHeight)
                    )
                )
            )

        // Verify in list view
        onView(withText(R.string.welcome2_activity_pick_subtitle))
            .check(matches(isCompletelyDisplayed()))

        onView(withId(R.id.next))
            .check(matches(allOf(isCompletelyDisplayed(), not(isEnabled()))))

        onView(withId(R.id.app_list))
            .check(matches(isCompletelyDisplayed()))
            .check(selectedDescendantsMatch(withText(R.string.app_label), isCompletelyDisplayed()))
            .check(
                selectedDescendantsMatch(
                    withText(TestBuildConfig.APPLICATION_ID), isCompletelyDisplayed()
                )
            )
            .perform(click(), swipeUp(), click())

        onView(withId(R.id.next))
            .check(matches(allOf(isCompletelyDisplayed(), isEnabled())))
            .perform(click())
            .check(doesNotExist())

        assertThat(getCurrentActivity(), instanceOf(PermissionsActivity::class))
    }

    @Test
    fun listViewIsStickyOnRecreation() {
        onView(withId(R.id.get_started))
            .perform(scrollTo(), click())
            .check(doesNotExist())

        onView(withText(R.string.welcome2_activity_pick_subtitle))
            .check(matches(isCompletelyDisplayed()))

        // Simulate rotation or other app recreation
        activity.runBlockingOnUiThread {
            activity.recreate()
        }

        onView(withText(R.string.welcome2_activity_pick_subtitle))
            .check(matches(isCompletelyDisplayed()))

        onView(withId(R.id.get_started))
            .check(doesNotExist())
    }

    @Test
    fun respectsListViewIntent() {
        launchActivity(Welcome2Activity::class) { intent ->
            intent.putExtra(Welcome2Activity.INTENT_LAUNCH_IN_LIST_VIEW, true)
        }

        onView(withText(R.string.welcome2_activity_pick_subtitle))
            .check(matches(isCompletelyDisplayed()))

        // Simulate rotation or other app recreation
        activity.runBlockingOnUiThread {
            activity.recreate()
        }

        onView(withText(R.string.welcome2_activity_pick_subtitle))
            .check(matches(isCompletelyDisplayed()))

        Espresso.pressBack()

        onView(withText(R.string.welcome2_activity_subtitle))
            .check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun testLearnMoreButton() {
        onView(withId(R.id.learn_more))
            .perform(scrollTo())
            .check(matches(isCompletelyDisplayed()))
            .perform(click())

        assertThat(getCurrentActivity(), instanceOf(LearnMoreActivity::class))
    }

    @Test
    fun testWelcome2DoesNotReappearAfterGettingStarted() {
        clearSharedPreferences()

        launchActivity(MainActivity::class)

        assertThat(getCurrentActivity(), instanceOf(Welcome2Activity::class))

        onView(withId(R.id.get_started))
            .perform(scrollTo(), click())

        waitForTransitionToEnd()

        onView(withText(testPackageName())).perform(click())

        onView(withText(R.string.welcome2_activity_next)).perform(click())

        assertEquals(PermissionsActivity::class.java, getCurrentActivity().javaClass)

        Espresso.pressBack()
        onIdle()

        assertEquals(PermissionsActivity::class.java, getCurrentActivity().javaClass)

        launchActivity(MainActivity::class)
        onIdle()

        assertEquals(PermissionsActivity::class.java, getCurrentActivity().javaClass)
    }

    private fun waitForTransitionToEnd() {
        TransitionManager.endTransitions(activity.findViewById(R.id.wrapper))
        // Forcing transition ending doesn't work on all devices
        sleep(transitionDuration * 2)
    }
}

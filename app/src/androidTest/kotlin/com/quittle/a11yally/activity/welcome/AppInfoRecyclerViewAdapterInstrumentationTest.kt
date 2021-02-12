package com.quittle.a11yally.activity.welcome

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isFocusable
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withChild
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withParentIndex
import com.quittle.a11yally.activity.UnfriendlyActivity
import com.quittle.a11yally.base.RefreshableWeakReference
import com.quittle.a11yally.launchActivity
import com.quittle.a11yally.runBlockingOnUiThread
import com.quittle.a11yally.test.R
import com.quittle.a11yally.testContext
import com.quittle.a11yally.withWidthAndHeight
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.lessThan
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test

class AppInfoRecyclerViewAdapterInstrumentationTest {
    private lateinit var activity: Activity
    private lateinit var recyclerView: RecyclerView

    @Before
    fun setUp() {
        activity = launchActivity(UnfriendlyActivity::class)

        activity.runBlockingOnUiThread {
            val rootView = activity.window.decorView.rootView as ViewGroup
            rootView.removeAllViews()
            LayoutInflater.from(testContext())
                .inflate(R.layout.app_info_recycler_view_adapter_activity, rootView)
            recyclerView = rootView.findViewById(R.id.recycler_view)
            recyclerView.layoutManager = LinearLayoutManager(activity)
        }
    }

    @Test
    fun emptyRecyclerView() {
        activity.runBlockingOnUiThread {
            recyclerView.adapter = AppInfoRecyclerViewAdapter(activity, listOf())
        }

        onIdle()
        assertThat(recyclerView.childCount, `is`(0))
    }

    @Test
    fun singleItemRecyclerView() {
        activity.runBlockingOnUiThread {
            recyclerView.adapter = AppInfoRecyclerViewAdapter(
                activity,
                listOf(
                    CheckableAppInfo(
                        AppInfo(
                            "label",
                            "package name",
                            0,
                            RefreshableWeakReference {
                                ContextCompat.getDrawable(
                                    activity, android.R.drawable.sym_def_app_icon
                                )!!
                            }
                        ),
                        true
                    )
                )
            )
        }
        onIdle()
        assertThat(recyclerView.childCount, `is`(1))

        onFirstRecyclerViewEntry()
            .check(matches(allOf(isChecked(), isFocusable(), isCompletelyDisplayed())))
            .perform(click())
            .check(matches(allOf(isNotChecked(), isFocusable(), isCompletelyDisplayed())))
    }

    @Test
    fun manyItemsRecyclerView() {
        activity.runBlockingOnUiThread {
            recyclerView.adapter = AppInfoRecyclerViewAdapter(
                activity,
                MutableList(100) { index ->
                    CheckableAppInfo(
                        AppInfo(
                            "App #$index",
                            "package.name.$index",
                            0,
                            RefreshableWeakReference {
                                ContextCompat.getDrawable(
                                    activity, android.R.drawable.sym_def_app_icon
                                )!!
                            }
                        ),
                        false
                    )
                }
            )
        }
        onIdle()
        assertThat(
            "Significantly fewer children than the size of the list",
            recyclerView.childCount, allOf(greaterThan(1), lessThan(20))
        )

        onFirstRecyclerViewEntry()
            .check(matches(isNotChecked()))
            .perform(click())
            .check(matches(allOf(isChecked(), isFocusable())))
        onView(withId(R.id.recycler_view))
            .perform(ViewActions.swipeUp())
        onIdle()

        onFirstRecyclerViewEntry()
            .check(matches(isNotChecked()))
        onView(withId(R.id.recycler_view))
            .perform(swipeDown(), swipeDown())
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(0))

        onIdle()

        onFirstRecyclerViewEntry()
            .check(matches(isChecked()))
    }

    /**
     * Gets the first view in the recycler view under test, performing many checks to validate
     * how it works.
     */
    private fun onFirstRecyclerViewEntry(): ViewInteraction {
        val isOfReasonableSize = withWidthAndHeight(greaterThan(10), greaterThan(10))
        return onView(allOf(withParent(withId(R.id.recycler_view)), withParentIndex(0)))
            .check(
                matches(
                    allOf(
                        isFocusable(), isClickable(), isDisplayed(), isEnabled(), isOfReasonableSize
                    )
                )
            )
            .check(matches(not(withChild(anyOf(isFocusable(), isClickable())))))
            .check(matches(withChild(allOf(isDisplayed(), isOfReasonableSize))))
    }
}

package com.quittle.a11yally.activity

import android.content.Context
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.PerformException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressKey
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.assertion.ViewAssertions.selectedDescendantsMatch
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quittle.a11yally.DisableAnimationsRule
import com.quittle.a11yally.PermissionsRule
import com.quittle.a11yally.R
import com.quittle.a11yally.ViewActionCheck
import com.quittle.a11yally.adapter.CheckboxAdapter.Companion.CheckboxViewHolder
import com.quittle.a11yally.clearSharedPreferences
import com.quittle.a11yally.withPreferenceProvider
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MultiAppSelectionActivityInstrumentationTest {
    @get:Rule
    val mPermissionsRule = PermissionsRule()

    @get:Rule
    val mDisableAnimationsRule = DisableAnimationsRule()

    private val recyclerView = withId(R.id.recycler_view)
    private val title = withId(R.id.title)
    private val subtitle = withId(R.id.subtitle)
    private val isCheckbox = isAssignableFrom(AppCompatCheckBox::class.java)

    @After
    fun tearDown() {
        clearSharedPreferences()
    }

    @Before
    fun setUp() {
        clearSharedPreferences()

        ActivityScenario.launch(MultiAppSelectionActivity::class.java)
    }

    @Test
    fun selectAll() {
        onView(recyclerView)
            .check(selectedDescendantsMatch(isCheckbox, allOf(isChecked(), not(isEnabled()))))
            .check(selectedDescendantsMatch(title, not(isEnabled())))
            .check(selectedDescendantsMatch(subtitle, not(isEnabled())))

        withPreferenceProvider { assertTrue(getInspectAllAppsEnabled()) }

        onView(withId(R.id.select_all))
            .check(matches(isCompletelyDisplayed()))
            .check(matches(isChecked()))
            .perform(click())
            .check(matches(isNotChecked()))

        withPreferenceProvider { assertFalse(getInspectAllAppsEnabled()) }

        onView(recyclerView)
            .check(selectedDescendantsMatch(isCheckbox, allOf(isNotChecked(), isEnabled())))
            .check(selectedDescendantsMatch(title, isEnabled()))
            .check(selectedDescendantsMatch(subtitle, isEnabled()))

        onView(withId(R.id.recycler_view))
            .perform(swipeUp())

        onIdle()

        onView(recyclerView)
            .check(selectedDescendantsMatch(isCheckbox, allOf(isNotChecked(), isEnabled())))
            .check(selectedDescendantsMatch(title, isEnabled()))
            .check(selectedDescendantsMatch(subtitle, isEnabled()))
    }

    @Test
    fun checkboxStateMaintained() {
        withPreferenceProvider { setInspectAllAppsEnabled(false) }

        onView(recyclerView)
            .check(selectedDescendantsMatch(isCheckbox, allOf(isNotChecked(), isEnabled())))

        withPreferenceProvider { assertTrue(getAppsToInspect().isEmpty()) }

        lateinit var firstAppId: CharSequence

        withNthEntry(0) { checkbox, _, subtitle ->
            firstAppId = subtitle.text
            assertFalse(checkbox.isChecked)
            checkbox.performClick()
            assertTrue(checkbox.isChecked)
        }

        withPreferenceProvider { assertEquals(setOf(firstAppId), getAppsToInspect()) }

        withNthEntry(60) { checkbox, _, subtitle ->
            assertFalse(checkbox.isChecked)
            assertNotEquals(firstAppId, subtitle.text)
        }

        withNthEntry(0) { checkbox, _, subtitle ->
            assertTrue(checkbox.isChecked)
            assertEquals(firstAppId, subtitle.text)
        }

        withPreferenceProvider { assertEquals(setOf(firstAppId), getAppsToInspect()) }
    }

    @Test
    fun searchBox() {
        val expectedTitle = "System UI"
        val expectedAppId = "com.android.systemui"
        val inputMethodManager = InstrumentationRegistry.getInstrumentation().targetContext
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // Check that it doesn't match
        onView(withId(R.id.recycler_view))
            .check(matches(not(hasDescendant(withText(expectedTitle)))))
            .check(matches(not(hasDescendant(withText(expectedAppId)))))

        // When clicking the search box, the keyboard pops up
        assertFalse(inputMethodManager.isAcceptingText)

        onView(withId(R.id.search))
            .check(matches(isCompletelyDisplayed()))
            .perform(click())

        assertTrue(inputMethodManager.isAcceptingText)

        // Typing the app id searches for the app
        onView(isAssignableFrom(EditText::class.java))
            .perform(typeText("roid.systemu"))

        onView(withId(R.id.recycler_view))
            .check(matches(hasDescendant(withText(expectedTitle))))
            .check(matches(hasDescendant(withText(expectedAppId))))

        // Clearing the search restores the entries
        onView(isAssignableFrom(EditText::class.java))
            .perform(*Array(15) { pressKey(KeyEvent.KEYCODE_DEL) })

        onView(withId(R.id.recycler_view))
            .check(matches(not(hasDescendant(withText(expectedTitle)))))
            .check(matches(not(hasDescendant(withText(expectedAppId)))))

        // Searching the app name also works
        onView(isAssignableFrom(EditText::class.java))
            .perform(typeTextIntoFocusedView("tem ui"))

        onView(withId(R.id.recycler_view))
            .check(matches(hasDescendant(withText(expectedTitle))))
            .check(matches(hasDescendant(withText(expectedAppId))))
    }

    @Test
    fun testAppsSortedByAppName() {
        val entries = Array(100) { i ->
            var ret: CharSequence? = null
            try {
                withNthEntry(i) { _, title, _ -> ret = title.text }
            } catch (e: PerformException) {
                // Not enough apps installed
            }
            ret
        }.filterNotNull().toTypedArray()

        val sortedEntries = entries.clone().apply { sort() }

        assertArrayEquals(sortedEntries, entries)
    }

    @Test
    fun testEntireEntryTogglesCheckbox() {
        withPreferenceProvider {
            setInspectAllAppsEnabled(false)
        }

        withNthEntry(0) { checkbox, title, subtitle ->
            assertFalse(checkbox.isChecked)

            title.performClick()
            assertTrue(checkbox.isChecked)

            subtitle.performClick()
            assertFalse(checkbox.isChecked)

            checkbox.performClick()
            assertTrue(checkbox.isChecked)
        }
    }

    private fun withNthEntry(
        position: Int,
        handler: (
            checkbox: AppCompatCheckBox,
            title: TextView,
            subtitle: TextView
        ) -> Unit
    ) {
        onView(withId(R.id.recycler_view))
            .perform(scrollToPosition<CheckboxViewHolder>(position))
            .perform(
                actionOnItemAtPosition<CheckboxViewHolder>(
                    position,
                    ViewActionCheck { view ->
                        handler(
                            view.findViewById(R.id.checkbox),
                            view.findViewById(R.id.title),
                            view.findViewById(R.id.subtitle)
                        )
                    }
                )
            )
    }
}

package com.quittle.a11yally

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class A11yAllyApplicationTest {
    lateinit var context: Context
    lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Test
    fun testDefaultPreferenceInitialization() {
        assertEquals(
                "If this fails, A11yAllyApplication likely is not initializing all preferences",
                A11yAllyApplication.PREFERENCE_RESOURCES,
                R.xml::class.members
                        .filter { member -> member.name.endsWith("preferences") }
                        .map { member -> member.call() }
                        .toSet())
    }

    @Test
    fun testDefaultPreferenceValues() {
        assertEquals(
                mapOf(
                        Pair("pref_highlight_issues", true),
                        Pair("pref_highlight_missing_labels", true),
                        Pair("pref_highlight_small_touch_targets", true),
                        Pair("pref_small_touch_target_size", "48"),
                        Pair("pref_display_content_descriptions", false)
                ),
                sharedPreferences.all)
    }
}

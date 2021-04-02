package com.quittle.a11yally

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.quittle.a11yally.preferences.PreferenceProvider
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@SuppressLint("ApplySharedPref")
class A11yAllyApplicationTest {
    lateinit var context: Context
    lateinit var sharedPreferences: SharedPreferences
    lateinit var preferenceProvider: PreferenceProvider

    @get:Rule
    val firebaseRule = FirebaseRule()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferenceProvider = PreferenceProvider(context)
        preferenceProvider.onResume()
    }

    @Test
    fun testDefaultPreferenceInitialization() {
        assertEquals(
            A11yAllyApplication.PREFERENCE_RESOURCES,
            R.xml::class.members
                .filter { member -> member.name.endsWith("preferences") }
                .map { member -> member.call() }
                .toSet(),
            "If this fails, A11yAllyApplication likely is not initializing all preferences"
        )
    }

    @Test
    fun testDefaultPreferenceValues() {
        assertEquals(
            mapOf(
                Pair("pref_service_enabled", true),
                Pair("pref_highlight_issues", false),
                Pair("pref_highlight_missing_labels", true),
                Pair("pref_highlight_small_touch_targets", true),
                Pair("pref_small_touch_target_size", "48"),
                Pair("pref_display_content_descriptions", false),
                Pair("pref_linear_navigation_enabled", false),
                Pair("pref_enable_all_apps", true),
                Pair("pref_show_tutorial", true)
            ),
            sharedPreferences.all
        )
    }

    @Test
    fun testPreferenceControls() {
        assertFalse(preferenceProvider.getHighlightIssues())
        assertFalse(preferenceProvider.getDisplayContentDescription())
        assertFalse(preferenceProvider.getLinearNavigationEnabled())

        enableHighlightIssues()

        assertTrue(preferenceProvider.getHighlightIssues())
        assertFalse(preferenceProvider.getDisplayContentDescription())
        assertFalse(preferenceProvider.getLinearNavigationEnabled())

        enableDisplayContentDescriptions()

        assertFalse(preferenceProvider.getHighlightIssues())
        assertTrue(preferenceProvider.getDisplayContentDescription())
        assertFalse(preferenceProvider.getLinearNavigationEnabled())

        enableLinearNavigation()

        assertFalse(preferenceProvider.getHighlightIssues())
        assertFalse(preferenceProvider.getDisplayContentDescription())
        assertTrue(preferenceProvider.getLinearNavigationEnabled())

        enableHighlightIssues()

        assertTrue(preferenceProvider.getHighlightIssues())
        assertFalse(preferenceProvider.getDisplayContentDescription())
        assertFalse(preferenceProvider.getLinearNavigationEnabled())
    }

    private fun enableHighlightIssues() {
        sharedPreferences.edit()
            .putBoolean(context.getString(R.string.pref_highlight_issues), true)
            .commit()
    }

    private fun enableDisplayContentDescriptions() {
        sharedPreferences.edit()
            .putBoolean(context.getString(R.string.pref_display_content_descriptions), true)
            .commit()
    }

    private fun enableLinearNavigation() {
        sharedPreferences.edit()
            .putBoolean(context.getString(R.string.pref_linear_navigation_enabled), true)
            .commit()
    }
}

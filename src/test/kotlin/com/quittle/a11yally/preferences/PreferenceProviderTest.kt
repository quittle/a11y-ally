package com.quittle.a11yally.preferences

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.quittle.a11yally.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberFunctions

@RunWith(RobolectricTestRunner::class)
@SuppressLint("ApplySharedPref")
// This must not be the real application to prevent
// A11yAllyApplication::initializePreferenceController
@Config(application = Application::class)
class PreferenceProviderTest {
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var preferenceProvider: PreferenceProvider

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        preferenceProvider = PreferenceProvider(context)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Test
    fun testDefaultGetters() {
        assertFalse(preferenceProvider.getServiceEnabled())
        assertFalse(preferenceProvider.getDisplayContentDescription())
        assertFalse(preferenceProvider.getLinearNavigationEnabled())
        assertFalse(preferenceProvider.getHighlightIssues())
        assertFalse(preferenceProvider.getHighlightMissingLabels())
        assertFalse(preferenceProvider.getHighlightSmallTouchTargets())
        assertEquals(0, preferenceProvider.getSmallTouchTargetSize())

        assertKnownGetters(7)
    }

    @Test
    fun testGetValues() {
        preferenceProvider.onResume()
        sharedPreferences.edit()
                .putBoolean(context.getString(R.string.pref_service_enabled), true)
                .putBoolean(context.getString(R.string.pref_display_content_descriptions), true)
                .putBoolean(context.getString(R.string.pref_linear_navigation_enabled), true)
                .putBoolean(context.getString(R.string.pref_highlight_issues), true)
                .putBoolean(context.getString(R.string.pref_highlight_missing_labels), true)
                .putBoolean(context.getString(R.string.pref_highlight_small_touch_targets), true)
                .putString(context.getString(R.string.pref_small_touch_target_size), "123")
                .commit()
        assertTrue(preferenceProvider.getServiceEnabled())
        assertTrue(preferenceProvider.getDisplayContentDescription())
        assertTrue(preferenceProvider.getLinearNavigationEnabled())
        assertTrue(preferenceProvider.getHighlightIssues())
        assertTrue(preferenceProvider.getHighlightMissingLabels())
        assertTrue(preferenceProvider.getHighlightSmallTouchTargets())
        assertEquals(123, preferenceProvider.getSmallTouchTargetSize())

        assertKnownGetters(7)
    }

    @Test
    fun testPutValues() {
        preferenceProvider.onResume()
        assertFalse(preferenceProvider.getServiceEnabled())
        assertFalse(preferenceProvider.getDisplayContentDescription())
        assertFalse(preferenceProvider.getLinearNavigationEnabled())
        assertFalse(preferenceProvider.getHighlightIssues())

        preferenceProvider.setServiceEnabled(true)
        preferenceProvider.setDisplayContentDescription(true)
        preferenceProvider.setLinearNavigationEnabled(true)
        preferenceProvider.setHighlightIssues(true)

        assertTrue(preferenceProvider.getServiceEnabled())
        assertTrue(preferenceProvider.getDisplayContentDescription())
        assertTrue(preferenceProvider.getLinearNavigationEnabled())
        assertTrue(preferenceProvider.getHighlightIssues())

        PreferenceProvider(context).run {
            onResume()
            assertTrue(getServiceEnabled())
            assertTrue(getDisplayContentDescription())
            assertTrue(getLinearNavigationEnabled())
            assertTrue(getHighlightIssues())
        }

        assertTrue(sharedPreferences.getBoolean(
                context.getString(R.string.pref_service_enabled), false))
        assertTrue(sharedPreferences.getBoolean(
                context.getString(R.string.pref_display_content_descriptions), false))
        assertTrue(sharedPreferences.getBoolean(
                context.getString(R.string.pref_linear_navigation_enabled), false))
        assertTrue(sharedPreferences.getBoolean(
                context.getString(R.string.pref_highlight_issues), false))

        assertKnownSetters(4)
    }

    @Test
    fun testValueUpdate() {
        // False by default
        assertFalse(preferenceProvider.getDisplayContentDescription())

        sharedPreferences.edit()
                .putBoolean(context.getString(R.string.pref_display_content_descriptions), true)
                .commit()
        // Still false because it hasn't been resumed
        assertFalse(preferenceProvider.getDisplayContentDescription())

        preferenceProvider.onResume()
        assertTrue(preferenceProvider.getDisplayContentDescription())

        sharedPreferences.edit()
                .putBoolean(context.getString(R.string.pref_display_content_descriptions), false)
                .commit()

        // Back to false due to listener
        assertFalse(preferenceProvider.getDisplayContentDescription())

        preferenceProvider.onPause()

        sharedPreferences.edit()
                .putBoolean(context.getString(R.string.pref_display_content_descriptions), true)
                .commit()

        // Not responsive due to listener being paused
        assertFalse(preferenceProvider.getDisplayContentDescription())

        preferenceProvider.onResume()

        // Preferences updated after resuming
        assertTrue(preferenceProvider.getDisplayContentDescription())
    }

    @Test
    fun testDuplicateLifecycleCallsAreSafe() {
        preferenceProvider.onPause()
        preferenceProvider.onPause()

        preferenceProvider.onResume()
        preferenceProvider.onResume()

        preferenceProvider.onPause()
        preferenceProvider.onPause()
    }

    private fun assertKnownSetters(curKnownGetters: Int) {
        assertEquals("When this test fails, you should update it with the new method added",
                curKnownGetters,
                PreferenceProvider::class.memberFunctions
                        .filter { it.visibility == KVisibility.PUBLIC }
                        .filter { it.name.startsWith("set") }
                        .count())
    }

    private fun assertKnownGetters(curKnownGetters: Int) {
        assertEquals("When this test fails, you should update it with the new method added",
                curKnownGetters,
                PreferenceProvider::class.memberFunctions
                        .filter { it.visibility == KVisibility.PUBLIC }
                        .filter { it.name.startsWith("get") }
                        .count())
    }
}

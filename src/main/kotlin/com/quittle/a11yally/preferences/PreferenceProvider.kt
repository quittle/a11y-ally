package com.quittle.a11yally.preferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.quittle.a11yally.R
import java.util.Arrays.asList

/**
 * Provides easy, lazy access to preferences. Consumers must call the [onResume] and [onPause]
 * events when appropriate in the consumer.
 */
class PreferenceProvider(context: Context) {
    private val preferenceProviderMembers = asList(
            PreferenceProviderBooleanMember(context, R.string.pref_display_content_descriptions),
            PreferenceProviderBooleanMember(context, R.string.pref_highlight_issues),
            PreferenceProviderBooleanMember(context, R.string.pref_highlight_missing_labels),
            PreferenceProviderBooleanMember(context, R.string.pref_highlight_empty_views),
            PreferenceProviderBooleanMember(context, R.string.pref_highlight_small_touch_targets),
            PreferenceProviderIntMember(context, R.string.pref_small_touch_target_size)
    )

    /**
     * This method may throw for a number of configuration issues. This is to simplify the logic
     * required in the getters and should be unit tested as part of this class by calling all the
     * getters. It is the responsibility of the caller to ensure the preference is mapped and is the
     * correct type.
     */
    private fun <T> getPreferenceProviderByPrefKey(prefKey: Int): T {
        val member = preferenceProviderMembers.find {
            it.getPrefKeyId() == prefKey
        }

        @Suppress("unchecked_cast")
        return member!!.getValue() as T
    }

    private val mListener =
            SharedPreferences.OnSharedPreferenceChangeListener {
                    sharedPreferences: SharedPreferences, key: String ->
                preferenceProviderMembers.forEach {
                    it.possiblyUpdateValue(sharedPreferences, key)
                }
            }

    private val mContext = context

    fun onResume() {
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        sharedPref.registerOnSharedPreferenceChangeListener(mListener)

        preferenceProviderMembers.forEach {
            it.updateValue(sharedPref)
        }
    }

    fun onPause() {
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        sharedPref.unregisterOnSharedPreferenceChangeListener(mListener)
    }

    fun getDisplayContentDescription(): Boolean {
        return getPreferenceProviderByPrefKey(R.string.pref_display_content_descriptions)
    }

    fun getHighlightIssues(): Boolean {
        return getPreferenceProviderByPrefKey(R.string.pref_highlight_issues)
    }

    fun getHighlightMissingLabels(): Boolean {
        return getPreferenceProviderByPrefKey(R.string.pref_highlight_missing_labels)
    }

    fun getHighlightEmptyViews(): Boolean {
        return getPreferenceProviderByPrefKey(R.string.pref_highlight_empty_views)
    }

    fun getHighlightSmallTouchTargets(): Boolean {
        return getPreferenceProviderByPrefKey(R.string.pref_highlight_small_touch_targets)
    }

    fun getSmallTouchTargetSize(): Int {
        return getPreferenceProviderByPrefKey(R.string.pref_small_touch_target_size)
    }
}

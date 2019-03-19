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
    private val mContext = context.applicationContext
    private val preferenceProviderMembers = asList(
            PreferenceProviderBooleanMember(mContext, R.string.pref_service_enabled),
            PreferenceProviderBooleanMember(mContext, R.string.pref_display_content_descriptions),
            PreferenceProviderBooleanMember(mContext, R.string.pref_highlight_issues),
            PreferenceProviderBooleanMember(mContext, R.string.pref_highlight_missing_labels),
            PreferenceProviderBooleanMember(mContext, R.string.pref_highlight_small_touch_targets),
            PreferenceProviderStringIntMember(mContext, R.string.pref_small_touch_target_size)
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

    private fun <T> putPreferenceProviderByPrefKey(prefKey: Int, value: T) {
        val member = preferenceProviderMembers.find {
            it.getPrefKeyId() == prefKey
        }

        @Suppress("unchecked_cast")
        (member as PreferenceProviderMember<T>).setValue(sharedPreferences, value)
    }

    private val mListener =
            SharedPreferences.OnSharedPreferenceChangeListener {
                    sharedPreferences: SharedPreferences, key: String ->
                preferenceProviderMembers.forEach {
                    it.possiblyUpdateValue(sharedPreferences, key)
                }
            }

    fun onResume() {
        sharedPreferences.registerOnSharedPreferenceChangeListener(mListener)

        preferenceProviderMembers.forEach {
            it.updateValue(sharedPreferences)
        }
    }

    fun onPause() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener)
    }

    val sharedPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(mContext)

    fun getServiceEnabled(): Boolean {
        return getPreferenceProviderByPrefKey(R.string.pref_service_enabled)
    }

    fun setServiceEnabled(enabled: Boolean) {
        putPreferenceProviderByPrefKey(R.string.pref_service_enabled, enabled)
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

    fun getHighlightSmallTouchTargets(): Boolean {
        return getPreferenceProviderByPrefKey(R.string.pref_highlight_small_touch_targets)
    }

    fun getSmallTouchTargetSize(): Int {
        return getPreferenceProviderByPrefKey(R.string.pref_small_touch_target_size)
    }
}

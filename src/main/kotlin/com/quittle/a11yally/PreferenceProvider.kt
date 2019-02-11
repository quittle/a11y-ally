package com.quittle.a11yally

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

/**
 * Provides easy, lazy access to preferences. Consumers must call the [onResume] and [onPause]
 * events when appropriate in the consumer.
 */
class PreferenceProvider(context: Context) {
    private val mListener =
            SharedPreferences.OnSharedPreferenceChangeListener {
                    sharedPreferences: SharedPreferences, key: String ->
        when (key) {
            mDisplayContentDescriptionsPrefKey -> mDisplayContentDescription =
                    sharedPreferences.getBoolean(key, mDisplayContentDescription)
            mDisplayAccessibilityIssuesPrefKey -> mDisplayAccessibilityIssues =
                    sharedPreferences.getBoolean(key, mDisplayAccessibilityIssues)
        }
    }

    private val mContext = context
    private var mDisplayContentDescription: Boolean = false
    private var mDisplayAccessibilityIssues: Boolean = false

    private val mDisplayContentDescriptionsPrefKey by lazy {
        mContext.getString(R.string.pref_display_content_descriptions)
    }
    private val mDisplayAccessibilityIssuesPrefKey by lazy {
        mContext.getString(R.string.pref_highlight_accessibility_issues)
    }

    fun onResume() {
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        sharedPref.registerOnSharedPreferenceChangeListener(mListener)

        mDisplayContentDescription = sharedPref.getBoolean(
                mDisplayContentDescriptionsPrefKey,
                mDisplayContentDescription)

        mDisplayAccessibilityIssues = sharedPref.getBoolean(
                mDisplayAccessibilityIssuesPrefKey,
                mDisplayAccessibilityIssues)
    }

    fun onPause() {
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        sharedPref.unregisterOnSharedPreferenceChangeListener(mListener)
    }

    fun getDisplayContentDescription(): Boolean {
        return mDisplayContentDescription
    }

    fun getDisplayAccessibilityIssues(): Boolean {
        return mDisplayAccessibilityIssues
    }
}

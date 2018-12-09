package com.quittle.a11yally.analyzer

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.preference.PreferenceManager
import com.quittle.a11yally.R

/**
 * App-specific implementation of an {@link AccessibilityAnalyzer}. It implements
 * {@link LifecycleOwner} for {@link AccessibilityOverlay} to consume its events for drawing, using
 * this service's context.
 */
class A11yAllyAccessibilityAnalyzer : AccessibilityAnalyzer(), OnSharedPreferenceChangeListener {
    private val prefServiceEnabled by lazy { getString(R.string.pref_service_enabled) }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (prefServiceEnabled == key) {
            if (sharedPreferences.getBoolean(prefServiceEnabled, false)) {
                resumeListeners()
            } else {
                pauseListeners()
            }
        }
    }

    override fun getAppWhitelist(): Iterable<String>? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)
    }
    /**
     * {@inheritDoc}
     *
     * This must be lazy because the constructor for {@link AccessibilityOverlay} must run after
     * {@link AccessibilityAnalyzer} is initialized fully.
     */
    override val listeners: Collection<AccessibilityItemEventListener> by lazy { setOf(
            AccessibilityItemLogger(),
            AccessibilityOverlay(this)
    ) }
}

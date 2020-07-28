package com.quittle.a11yally.analyzer

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.preference.PreferenceManager
import com.quittle.a11yally.R
import com.quittle.a11yally.analyzer.listeners.AccessibilityItemLogger
import com.quittle.a11yally.analyzer.listeners.ContentDescriptionOverlay
import com.quittle.a11yally.analyzer.listeners.HighlighterAccessibilityOverlay

/**
 * App-specific implementation of an {@link AccessibilityAnalyzer}. It implements
 * {@link LifecycleOwner} for {@link AccessibilityOverlay}s to consume its events for drawing, using
 * this service's context.
 */
class A11yAllyAccessibilityAnalyzer : AccessibilityAnalyzer(), OnSharedPreferenceChangeListener {
    private val prefServiceEnabled by lazy { getString(R.string.pref_service_enabled) }
    private val prefEnableAllApps by lazy { getString(R.string.pref_enable_all_apps) }
    private val prefEnableAllAppsDefault by lazy {
        resources.getBoolean(R.bool.pref_enable_all_apps_default)
    }
    private val prefEnabledApps by lazy { getString(R.string.pref_enabled_apps) }
    private val mAccessibilityItemLogger by lazy { AccessibilityItemLogger(applicationContext) }

    private var whitelistedApps: Iterable<String>? = null

    companion object {
        private var sServiceInstance: A11yAllyAccessibilityAnalyzer? = null

        fun getInstance(): A11yAllyAccessibilityAnalyzer? {
            return sServiceInstance
        }
    }

    fun startRecording() {
        mAccessibilityItemLogger.startRecording()
    }

    fun stopRecording() {
        mAccessibilityItemLogger.stopRecording()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            prefServiceEnabled -> {
                if (sharedPreferences.getBoolean(prefServiceEnabled, false)) {
                    resumeListeners()
                } else {
                    pauseListeners()
                }
            }
            prefEnabledApps, prefEnableAllApps -> updateAppWhitelist(sharedPreferences)
        }
    }

    override fun getAppWhitelist(): Iterable<String>? {
        return whitelistedApps
    }

    override fun onCreate() {
        super.onCreate()
        PreferenceManager.getDefaultSharedPreferences(this).apply {
            registerOnSharedPreferenceChangeListener(this@A11yAllyAccessibilityAnalyzer)
            updateAppWhitelist(this)
            if (getBoolean(prefServiceEnabled, false)) {
                resumeListeners()
            } else {
                pauseListeners()
            }
        }

        sServiceInstance = this
    }

    override fun onDestroy() {
        sServiceInstance = null

        super.onDestroy()
    }

    /**
     * {@inheritDoc}
     *
     * This must be lazy because the constructor for [AccessibilityOverlay]s must run after
     * [AccessibilityAnalyzer] is initialized fully.
     */
    override val listeners: Collection<AccessibilityItemEventListener> by lazy {
        val highlighterAccessibilityOverlay = HighlighterAccessibilityOverlay(this)

        val rootListener =
                fanOutIssueListeners(highlighterAccessibilityOverlay, mAccessibilityItemLogger)

        setOf(
                AccessibilityNodeIssueAnalyzer(this, rootListener),
                ContentDescriptionOverlay(this),
                LinearNavigationAccessibilityOverlay(this)
        )
    }

    private fun updateAppWhitelist(preferences: SharedPreferences) {
        whitelistedApps = if (preferences.getBoolean(prefEnableAllApps, prefEnableAllAppsDefault)) {
            null
        } else {
            preferences.getStringSet(prefEnabledApps, mutableSetOf())
        }
    }

    /**
     * Generates a new listener that will generate callbacks to invoke all the [targetListeners].
     * @param targetListeners The listeners to invoke when the returned listener is invoked.
     * @return a new listener that delegates callbacks to all the [targetListeners]
     */
    private fun fanOutIssueListeners(
        vararg targetListeners: AccessibilityIssueListener
    ): AccessibilityIssueListener {
        return object : AccessibilityIssueListener {
            override fun onInvalidateIssues() {
                targetListeners.forEach(AccessibilityIssueListener::onInvalidateIssues)
            }

            override fun onIssues(issues: Collection<AccessibilityIssue>) {
                targetListeners.forEach { listener -> listener.onIssues(issues) }
            }
        }
    }
}

package com.quittle.a11yally

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.preference.PreferenceManager
import android.os.StrictMode
import com.quittle.a11yally.analyzer.A11yAllyAccessibilityAnalyzer
import com.quittle.a11yally.preferences.PreferenceProvider

class A11yAllyApplication : Application(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        val PREFERENCE_RESOURCES = setOf(
                R.xml.preferences,
                R.xml.highlight_issues_preferences,
                R.xml.content_description_preferences,
                R.xml.linear_navigation_preferences)
    }

    private val prefServiceEnabled by lazy { getString(R.string.pref_service_enabled) }
    private var mPreferenceProvider: PreferenceProvider? = null

    init {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    // .penaltyDeath()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    // .penaltyDeath()
                    .build())
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (sharedPreferences.isNotNull() && key.isNotNull() &&
                prefServiceEnabled == key && sharedPreferences.getBoolean(key, false)) {
            applicationContext.startService(
                    Intent(applicationContext, A11yAllyAccessibilityAnalyzer::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        initializePreferences()
        initializePreferenceController()
    }

    private fun initializePreferences() {
        PREFERENCE_RESOURCES.forEach {
            PreferenceManager.setDefaultValues(this, it, true)
        }

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)
    }

    private fun initializePreferenceController() {
        // Hold on to the reference to prevent it from being garbage collected. The reference is
        // otherwise unused.
        mPreferenceProvider = PreferenceProvider(this).apply {
            onHighlightIssuesUpdate { enabled ->
                if (enabled) {
                    setDisplayContentDescription(false)
                    setLinearNavigationEnabled(false)
                }
            }
            onDisplayContentDescriptionUpdate { enabled ->
                if (enabled) {
                    setHighlightIssues(false)
                    setLinearNavigationEnabled(false)
                }
            }
            onLinearNavigationEnabledUpdate { enabled ->
                if (enabled) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        DialogActivity.show(
                                this@A11yAllyApplication,
                                R.string.linear_navigation_overlay_unavailable_dialog_message)
                        setLinearNavigationEnabled(false)
                    } else {
                        setHighlightIssues(false)
                        setDisplayContentDescription(false)
                    }
                }
            }

            // Start listening
            onResume()
        }
    }
}

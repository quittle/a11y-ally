package com.quittle.a11yally

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.StrictMode
import androidx.preference.PreferenceManager
import com.quittle.a11yally.activity.DialogActivity
import com.quittle.a11yally.analytics.firebaseAnalytics
import com.quittle.a11yally.analytics.logPreferenceChange
import com.quittle.a11yally.analytics.logPreferenceRemoval
import com.quittle.a11yally.analyzer.A11yAllyAccessibilityAnalyzer
import com.quittle.a11yally.base.ifNotNull
import com.quittle.a11yally.base.isNull
import com.quittle.a11yally.crashlytics.crashlytics
import com.quittle.a11yally.crashlytics.recordException
import com.quittle.a11yally.preferences.PreferenceProvider

class A11yAllyApplication : Application(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        val PREFERENCE_RESOURCES = setOf(
            R.xml.preferences,
            R.xml.highlight_issues_preferences,
            R.xml.content_description_preferences,
            R.xml.linear_navigation_preferences
        )
    }

    private val prefServiceEnabled by lazy { getString(R.string.pref_service_enabled) }
    private var mPreferenceProvider: PreferenceProvider? = null

    init {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    // .penaltyDeath()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    // .penaltyDeath()
                    .build()
            )
        }
    }

    override fun onSharedPreferenceChanged(
        nullableSharedPreferences: SharedPreferences?,
        key: String?
    ) {
        nullableSharedPreferences.ifNotNull { sharedPreferences ->
            if (key.isNull()) {
                return
            }

            val curValue = sharedPreferences.all[key]
            when {
                curValue is Set<*> -> {
                    firebaseAnalytics.logPreferenceChange(key, curValue.joinToString())
                }
                curValue is String -> {
                    firebaseAnalytics.logPreferenceChange(key, curValue)
                }
                curValue is Number -> {
                    firebaseAnalytics.logPreferenceChange(key, curValue)
                }
                curValue is Boolean -> {
                    firebaseAnalytics.logPreferenceChange(key, curValue)
                }
                curValue.isNull() -> {
                    firebaseAnalytics.logPreferenceRemoval(key)
                }
                else -> {
                    val clazz = curValue.javaClass.name
                    crashlytics.recordException("Unsupported shared preferences type: $clazz")
                }
            }

            if (prefServiceEnabled == key && sharedPreferences.getBoolean(key, false)) {
                applicationContext.startService(
                    Intent(applicationContext, A11yAllyAccessibilityAnalyzer::class.java)
                )
            }
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
                            R.string.linear_navigation_overlay_unavailable_dialog_message
                        )
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

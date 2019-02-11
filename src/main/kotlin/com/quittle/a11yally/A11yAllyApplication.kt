package com.quittle.a11yally

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.os.StrictMode
import com.quittle.a11yally.analyzer.A11yAllyAccessibilityAnalyzer

class A11yAllyApplication : Application(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val prefServiceEnabled by lazy { getString(R.string.pref_service_enabled) }

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
    }

    private fun initializePreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)
    }
}

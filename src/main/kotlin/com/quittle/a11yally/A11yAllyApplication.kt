package com.quittle.a11yally

import android.app.Application
import android.preference.PreferenceManager
import android.os.StrictMode

class A11yAllyApplication : Application() {

    init {
        if (BuildConfig.DEBUG && false) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build())
        }
    }

    override fun onCreate() {
        super.onCreate()
        initializePreferences()
    }

    private fun initializePreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }
}

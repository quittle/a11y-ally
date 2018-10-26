package com.quittle.a11yally

import android.app.Application
import android.preference.PreferenceManager

public class A11yAllyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }
}

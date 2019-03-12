package com.quittle.a11yally.preferences

import android.content.Context
import android.content.SharedPreferences

internal class PreferenceProviderStringIntMember(context: Context, prefKeyId: Int) :
        PreferenceProviderMember<Int>(context, prefKeyId, 0, ::parseSharedPreferences) {
    private companion object {
        /**
         * Because the preferences may be stored as strings if picking from a [ListPreference], the
         * value must be converted to an int
         */
        private fun parseSharedPreferences(
                sharedPreferences: SharedPreferences, prefKey: String, defaultValue: Int): Int {
            return sharedPreferences.getString(prefKey, defaultValue.toString())?.toInt()
                    ?: defaultValue
        }
    }
}

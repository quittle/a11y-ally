package com.quittle.a11yally.preferences

import android.content.Context
import android.content.SharedPreferences

internal class PreferenceProviderStringSetMember(context: Context, prefKeyId: Int) :
        PreferenceProviderMember<Set<String>>(context, prefKeyId, setOf(),
                ::getStringSet, SharedPreferences.Editor::putStringSet)

/** Kotlin can't handle SharedPreferences returning a MutableSet instead of just a Set */
fun getStringSet(
    sharedPrefs: SharedPreferences,
    prefKey: String,
    defaultValue: Set<String>
): Set<String> {
    return sharedPrefs.getStringSet(prefKey, defaultValue) as Set<String>
}

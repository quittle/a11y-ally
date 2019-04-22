package com.quittle.a11yally.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

/**
 * Abstraction of a specific preference intended to be consumed only by [PreferenceProvider].
 * Subclasses are intended to provide the implementation call to retrieve the preferences.
 * @param context The Android application context
 * @param prefKeyId The string resource representing the preference key id
 * @param defaultValue The default value for the preference if not known or initialized
 * @param getPrefValue The method reference on [SharedPreferences] for getting a preference. e.g.
 *   [SharedPreferences.getString].
 */
internal abstract class PreferenceProviderMember<T>(
        context: Context,
        private val prefKeyId: Int,
        defaultValue: T,
        private val getPrefValue: (sharedPreferences: SharedPreferences,
                                   prefKey: String,
                                   defaultValue: T) -> T,
        private val putPrefValue: (sharedPreferences: SharedPreferences.Editor,
                                   prefKey: String,
                                   value: T) -> SharedPreferences.Editor) {
    private val mPrefKey: String by lazy { context.getString(prefKeyId) }
    private var mValue: T = defaultValue
    private val mListeners: MutableList<(T) -> Unit> = mutableListOf()

    fun getValue(): T {
        return mValue
    }

    // This needs to be suppressed because Lint will not recognize the invocation as part of
    // the chain and will think the changes were not committed or applied.
    @SuppressLint("CommitPrefEdits")
    fun setValue(sharedPreferences: SharedPreferences, value: T) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        putPrefValue.invoke(editor, mPrefKey, value)
        editor.apply()
    }

    fun addListener(listener: (T) -> Unit) {
        mListeners.add(listener)
    }

    fun getPrefKeyId(): Int {
        return prefKeyId
    }

    fun updateValue(sharedPref: SharedPreferences) {
        mValue = getPrefValue(sharedPref, mPrefKey, mValue)
        mListeners.forEach { it(mValue) }
    }

    fun possiblyUpdateValue(sharedPref: SharedPreferences, prefKey: String) {
        if (prefKey == mPrefKey) {
            updateValue(sharedPref)
        }
    }
}

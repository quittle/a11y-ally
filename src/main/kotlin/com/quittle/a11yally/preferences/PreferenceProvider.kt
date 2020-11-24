package com.quittle.a11yally.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.quittle.a11yally.R

/**
 * Provides easy, lazy access to preferences. Consumers must call the [onResume] and [onPause]
 * events when appropriate in the consumer.
 */
class PreferenceProvider(context: Context, resumeOnConstruction: Boolean = false) {
    private val mContext = context.applicationContext
    private val preferenceProviderMembers = listOf(
            PreferenceProviderBooleanMember(mContext, R.string.pref_service_enabled),
            PreferenceProviderBooleanMember(mContext, R.string.pref_display_content_descriptions),
            PreferenceProviderBooleanMember(mContext, R.string.pref_highlight_issues),
            PreferenceProviderBooleanMember(mContext, R.string.pref_highlight_missing_labels),
            PreferenceProviderBooleanMember(mContext, R.string.pref_highlight_small_touch_targets),
            PreferenceProviderStringIntMember(mContext, R.string.pref_small_touch_target_size),
            PreferenceProviderBooleanMember(mContext, R.string.pref_linear_navigation_enabled),
            PreferenceProviderBooleanMember(mContext, R.string.pref_enable_all_apps, true),
            PreferenceProviderStringSetMember(mContext, R.string.pref_enabled_apps),
            PreferenceProviderBooleanMember(mContext, R.string.pref_show_tutorial, true)
    )

    init {
        if (resumeOnConstruction) {
            this.onResume()
        }
    }

    /**
     * This method may throw for a number of configuration issues. This is to simplify the logic
     * required in the getters and should be unit tested as part of this class by calling all the
     * getters. It is the responsibility of the caller to ensure the preference is mapped and is the
     * correct type.
     */
    private fun <T> getPreferenceProviderValueByPrefKey(prefKey: Int): T {
        return getPreferenceProviderMemberByPrefKey<T>(prefKey).getValue()
    }

    private fun <T> getPreferenceProviderMemberByPrefKey(prefKey: Int):
            PreferenceProviderMember<T> {
        @Suppress("unchecked_cast")
        return preferenceProviderMembers.find {
            it.getPrefKeyId() == prefKey
        } as PreferenceProviderMember<T>
    }

    private fun <T> putPreferenceProviderByPrefKey(prefKey: Int, value: T) {
        getPreferenceProviderMemberByPrefKey<T>(prefKey).setValue(sharedPreferences, value)
    }

    private val mListener =
            SharedPreferences.OnSharedPreferenceChangeListener {
                    sharedPreferences: SharedPreferences, key: String? ->
                preferenceProviderMembers.forEach {
                    // Starting with SDK level 30, when storage is cleared, the key may be null
                    // indicating the key was deleted.
                    if (key == null) {
                        it.updateValue(sharedPreferences)
                    } else {
                        it.possiblyUpdateValue(sharedPreferences, key)
                    }
                }
            }

    fun onResume() {
        sharedPreferences.registerOnSharedPreferenceChangeListener(mListener)

        preferenceProviderMembers.forEach {
            it.updateValue(sharedPreferences)
        }
    }

    fun onPause() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener)
    }

    val sharedPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(mContext)

    fun getServiceEnabled(): Boolean {
        return getPreferenceProviderValueByPrefKey(R.string.pref_service_enabled)
    }

    fun getServiceEnabledLiveData(): LiveData<Boolean> {
        return booleanPreferenceLiveData(R.string.pref_service_enabled)
    }

    fun setServiceEnabled(enabled: Boolean) {
        putPreferenceProviderByPrefKey(R.string.pref_service_enabled, enabled)
    }

    fun getDisplayContentDescription(): Boolean {
        return getPreferenceProviderValueByPrefKey(R.string.pref_display_content_descriptions)
    }

    fun getDisplayContentDescriptionLiveData(): LiveData<Boolean> {
        return booleanPreferenceLiveData(R.string.pref_display_content_descriptions)
    }

    fun setDisplayContentDescription(enabled: Boolean) {
        putPreferenceProviderByPrefKey(R.string.pref_display_content_descriptions, enabled)
    }

    fun getHighlightIssues(): Boolean {
        return getPreferenceProviderValueByPrefKey(R.string.pref_highlight_issues)
    }

    fun getHighlightIssuesLiveData(): LiveData<Boolean> {
        return booleanPreferenceLiveData(R.string.pref_highlight_issues)
    }

    fun setHighlightIssues(enabled: Boolean) {
        putPreferenceProviderByPrefKey(R.string.pref_highlight_issues, enabled)
    }

    fun getHighlightMissingLabels(): Boolean {
        return getPreferenceProviderValueByPrefKey(R.string.pref_highlight_missing_labels)
    }

    fun getHighlightSmallTouchTargets(): Boolean {
        return getPreferenceProviderValueByPrefKey(R.string.pref_highlight_small_touch_targets)
    }

    fun getSmallTouchTargetSize(): Int {
        return getPreferenceProviderValueByPrefKey(R.string.pref_small_touch_target_size)
    }

    fun getLinearNavigationEnabled(): Boolean {
        return getPreferenceProviderValueByPrefKey(R.string.pref_linear_navigation_enabled)
    }

    fun setLinearNavigationEnabled(enabled: Boolean) {
        putPreferenceProviderByPrefKey(R.string.pref_linear_navigation_enabled, enabled)
    }

    fun getInspectAllAppsEnabled(): Boolean {
        return getPreferenceProviderValueByPrefKey(R.string.pref_enable_all_apps)
    }

    fun setInspectAllAppsEnabled(enabled: Boolean) {
        putPreferenceProviderByPrefKey(R.string.pref_enable_all_apps, enabled)
    }

    fun getAppsToInspect(): Set<String> {
        return getPreferenceProviderValueByPrefKey(R.string.pref_enabled_apps)
    }

    fun setAppsToInspect(apps: Set<String>) {
        putPreferenceProviderByPrefKey(R.string.pref_enabled_apps, apps)
    }

    fun getShowTutorial(): Boolean {
        return getPreferenceProviderValueByPrefKey(R.string.pref_show_tutorial)
    }

    fun setShowTutorial(enabled: Boolean) {
        putPreferenceProviderByPrefKey(R.string.pref_show_tutorial, enabled)
    }

    fun onServiceEnabledUpdate(callback: (Boolean) -> Unit) {
        getPreferenceProviderMemberByPrefKey<Boolean>(R.string.pref_service_enabled)
                .addListener(callback)
    }

    fun onLinearNavigationEnabledUpdate(callback: (Boolean) -> Unit) {
        getPreferenceProviderMemberByPrefKey<Boolean>(R.string.pref_linear_navigation_enabled)
                .addListener(callback)
    }

    fun onHighlightIssuesUpdate(callback: (Boolean) -> Unit) {
        getPreferenceProviderMemberByPrefKey<Boolean>(R.string.pref_highlight_issues)
                .addListener(callback)
    }

    fun onDisplayContentDescriptionUpdate(callback: (Boolean) -> Unit) {
        getPreferenceProviderMemberByPrefKey<Boolean>(R.string.pref_display_content_descriptions)
                .addListener(callback)
    }

    fun onInspectAllAppsUpdate(callback: (Boolean) -> Unit) {
        getPreferenceProviderMemberByPrefKey<Boolean>(R.string.pref_enable_all_apps)
                .addListener(callback)
    }

    fun getLinearNavigationLiveData(): LiveData<Boolean> {
        return booleanPreferenceLiveData(R.string.pref_linear_navigation_enabled)
    }

    private fun booleanPreferenceLiveData(@StringRes id: Int): LiveData<Boolean> {
        val pref = getPreferenceProviderMemberByPrefKey<Boolean>(id)
        return PreferenceLiveData(pref)
    }
}

/**
 * Hold onto the reference of pref to ensure it doesn't get garbage collected
 */
internal class PreferenceLiveData<T>(private val pref: PreferenceProviderMember<T>) :
        MutableLiveData<T>(pref.getValue()) {
    init {
        pref.addListener { v ->
            setValue(v)
        }
    }
}

fun withPreferenceProvider(context: Context, block: PreferenceProvider.() -> Unit) {
    PreferenceProvider(context).apply {
        onResume()
        block(this)
        onPause()
    }
}

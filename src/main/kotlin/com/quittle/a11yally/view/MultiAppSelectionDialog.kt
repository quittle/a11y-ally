package com.quittle.a11yally.view

import android.app.Dialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.fragment.app.DialogFragment
import com.quittle.a11yally.R

class MultiAppSelectionDialog : DialogFragment() {
    private companion object {
        val NO_OP_ON_CLICK_LISTENER: (DialogInterface, Int) -> Unit =
                { _: DialogInterface, _: Int -> }
    }
    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }
    private val prefKeyEnabledApps by lazy { getString(R.string.pref_enabled_apps) }
    private val prefKeyEnableAllApps by lazy { getString(R.string.pref_enable_all_apps) }
    private val prefEnableAllAppsDefault by lazy {
        resources.getBoolean(R.bool.pref_enable_all_apps_default)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val currentlyEnabledApps = preferences.getStringSet(prefKeyEnabledApps, emptySet())!!
        val installedApplicationNames = getAllInstalledApplicationNames()
        val installedApplicationStatus =
                installedApplicationNames.map(currentlyEnabledApps::contains).toBooleanArray()

        var selectAllEnabled =
                preferences.getBoolean(prefKeyEnableAllApps, prefEnableAllAppsDefault)

        // Initialize all the checkboxes to be checked if select all is enabled. Enabling them
        // immediately after during onShow is too early in the lifecycle
        val initialInstalledApplicationStatus = if (selectAllEnabled) {
            BooleanArray(installedApplicationStatus.size) { true }
        } else {
            installedApplicationStatus
        }

        val titleView = View.inflate(context, R.layout.app_picker_custom_title, null)
        val dialog = AlertDialog.Builder(context!!, R.style.MainTheme)
                .setCustomTitle(titleView)
                .setMultiChoiceItems(installedApplicationNames, initialInstalledApplicationStatus) {
                        _, index: Int, checked: Boolean ->
                    installedApplicationStatus[index] = checked
                }
                .setNegativeButton(R.string.cancel, NO_OP_ON_CLICK_LISTENER)
                .setPositiveButton(R.string.confirm) { _: DialogInterface, _: Int ->
                    val enabledApps = installedApplicationNames
                            .filterIndexed { index: Int, _ -> installedApplicationStatus[index] }
                            .toSet()
                    preferences.edit()
                            .putStringSet(prefKeyEnabledApps, enabledApps)
                            .putBoolean(prefKeyEnableAllApps, selectAllEnabled)
                            .apply()
                }
                .create()

        setListState(dialog, selectAllEnabled, installedApplicationStatus)
        // This may not render correctly if disabled and set before being shown, hence the need to
        // set it twice
        dialog.setOnShowListener {
            setListState(dialog, selectAllEnabled, installedApplicationStatus)
        }

        titleView.findViewById<CheckBox>(R.id.select_all)?.apply {
            isChecked = selectAllEnabled
            setOnCheckedChangeListener { _, checked: Boolean ->
                selectAllEnabled = checked
                setListState(dialog, checked, installedApplicationStatus)
            }
        }
        return dialog
    }

    private fun getAllInstalledApplicationNames(): Array<out String> {
        return getAllInstalledApplications()
                ?.map(ApplicationInfo::packageName)
                ?.sorted()
                ?.toTypedArray()
                .orEmpty()
    }

    private fun getAllInstalledApplications(): Collection<ApplicationInfo>? {
        return context?.packageManager?.getInstalledApplications(0)
    }

    private fun setListState(dialog: AlertDialog,
                             selectAllEnabled: Boolean,
                             currentEnabledPreference: BooleanArray) {
        val list = dialog.listView
        list.isEnabled = !selectAllEnabled

        for (i in 0 until list.childCount) {
            (list.getChildAt(i) as AppCompatCheckedTextView).apply {
                isChecked = selectAllEnabled || currentEnabledPreference[i]
                isEnabled = !selectAllEnabled
            }
        }
    }
}

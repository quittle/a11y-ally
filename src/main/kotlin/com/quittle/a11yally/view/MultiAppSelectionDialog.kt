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
        val appNames = getApps()
                ?.map(ApplicationInfo::packageName)
                ?.sorted()
                ?.toTypedArray()
                .orEmpty()

        val enabledAppsSet: MutableSet<String> = run {
            val emptySet = mutableSetOf<String>()
            preferences.getStringSet(prefKeyEnabledApps, emptySet)?.toMutableSet() ?: emptySet
        }
        val enabledAppsArray = BooleanArray(appNames.size) { i ->
            enabledAppsSet.contains(appNames[i])
        }

        var selectAllEnabled =
                preferences.getBoolean(prefKeyEnableAllApps, prefEnableAllAppsDefault)

        val titleView = View.inflate(context, R.layout.app_picker_custom_title, null)
        val dialog = AlertDialog.Builder(context!!, R.style.MainTheme)
                .setCustomTitle(titleView)
                .setMultiChoiceItems(appNames, enabledAppsArray) { _, i: Int, checked: Boolean ->
                    val appName = appNames[i]
                    if (checked) {
                        enabledAppsSet.add(appName)
                    } else {
                        enabledAppsSet.remove(appName)
                    }
                }
                .setNegativeButton(R.string.cancel, NO_OP_ON_CLICK_LISTENER)
                .setPositiveButton(R.string.confirm) { _: DialogInterface, _: Int ->
                    preferences.edit()
                            .putStringSet(prefKeyEnabledApps, enabledAppsSet)
                            .putBoolean(prefKeyEnableAllApps, selectAllEnabled)
                            .apply()
                }
                .create()

        setListState(dialog, !selectAllEnabled)
        // This may not render correctly if disabled and set before being shown, hence the need to
        // set it twice
        dialog.setOnShowListener {
            setListState(dialog, !selectAllEnabled)
        }

        titleView.findViewById<CheckBox>(R.id.select_all)?.apply {
            isChecked = selectAllEnabled
            setOnCheckedChangeListener { _, checked: Boolean ->
                selectAllEnabled = checked
                setListState(dialog, !checked)
            }
        }
        return dialog
    }

    private fun getApps(): Collection<ApplicationInfo>? {
        return context?.packageManager?.getInstalledApplications(0)
    }

    private fun setListState(dialog: AlertDialog, enabled: Boolean) {
        val list = dialog.listView
        list.isEnabled = enabled
        for (i in 0 until list.childCount) {
            list.getChildAt(i).isEnabled = enabled
        }
    }
}

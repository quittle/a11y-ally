package com.quittle.a11yally.view

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.Menu
import android.widget.ScrollView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quittle.a11yally.R
import com.quittle.a11yally.adapter.CheckboxAdapter
import com.quittle.a11yally.adapter.CheckboxAdapter.Companion.CheckboxAdapterEntry
import com.quittle.a11yally.adapter.CheckboxAdapter.Companion.CheckboxAdapterState
import com.quittle.a11yally.preferences.PreferenceProvider

class MultiAppSelectionActivity : FixedContentActivity() {
    private class CustomQueryTextListener(private val mCheckboxAdapter: CheckboxAdapter) :
            SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            mCheckboxAdapter.run {
                stateArray.forEach { state ->
                    val entry = state.checkboxAdapterEntry
                    state.visible =
                            entry.subtitle.contains(newText, true) ||
                            entry.title.contains(newText, true)
                }
                notifyDataSetChanged()
            }
            return true
        }
    }

    private var mList: RecyclerView? = null
    private var mCheckboxAdapter: CheckboxAdapter? = null
    private val mPreferenceProvider by lazy { PreferenceProvider(this, true) }

    override val layoutId = R.layout.multi_app_selection_activity

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.multi_app_selection, menu)

        (menu.findItem(R.id.search).actionView as SearchView).run {
            setOnQueryTextListener(CustomQueryTextListener(mCheckboxAdapter!!))
        }

        (menu.findItem(R.id.select_all).actionView as AppCompatCheckBox).run {
            isChecked = mPreferenceProvider.getInspectAllAppsEnabled()
            setOnCheckedChangeListener { _, isChecked ->
                mPreferenceProvider.setInspectAllAppsEnabled(isChecked)
            }
        }

        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentlyEnabledApps = mPreferenceProvider.getAppsToInspect()
        val installedApplicationInfos = getAllInstalledApplicationInfos()
        val installedApplicationStatus =
                installedApplicationInfos
                        .map(CheckboxAdapterEntry::subtitle)
                        .map(currentlyEnabledApps::contains)
                        .toBooleanArray()

        val selectAllEnabled = mPreferenceProvider.getInspectAllAppsEnabled()

        val state = installedApplicationInfos.map { info ->
            CheckboxAdapterState(
                    checkboxAdapterEntry = info,
                    checked = selectAllEnabled || currentlyEnabledApps.contains(info.subtitle),
                    enabled = !selectAllEnabled,
                    visible = true)
        }.toTypedArray()

        mCheckboxAdapter = CheckboxAdapter(state) { index: Int, isChecked: Boolean ->
            installedApplicationStatus[index] = isChecked

            val apps = installedApplicationInfos
                    .filterIndexed { i, _ -> installedApplicationStatus[i] }
                    .map(CheckboxAdapterEntry::subtitle)
                    .toSet()
            mPreferenceProvider.setAppsToInspect(apps)
        }

        val listScrollView = findViewById<ScrollView>(R.id.list_scroll_view)

        mList = findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(false)

            layoutManager = LinearLayoutManager(this@MultiAppSelectionActivity)

            adapter = mCheckboxAdapter
        }

        mPreferenceProvider.onInspectAllAppsUpdate { enabled ->
            mCheckboxAdapter?.let {
                it.stateArray.forEachIndexed { index, state ->
                    state.checked = enabled || installedApplicationStatus[index]
                    state.enabled = !enabled
                }

                it.notifyDataSetChanged()
            }
            mList?.isEnabled = !enabled
            listScrollView.isEnabled = !enabled
        }
    }

    override fun onResume() {
        super.onResume()
        mPreferenceProvider.onResume()
    }

    override fun onPause() {
        super.onPause()
        mPreferenceProvider.onPause()
    }

    private fun getAllInstalledApplicationInfos(): Array<out CheckboxAdapterEntry> {
        return getAllInstalledApplications().asSequence()
                .map { applicationInfo -> CheckboxAdapterEntry(
                        title = packageManager.getApplicationLabel(applicationInfo).toString(),
                        subtitle = applicationInfo.packageName)
                }
                .sorted()
                .toList()
                .toTypedArray()
    }

    private fun getAllInstalledApplications(): Collection<ApplicationInfo> {
        return packageManager.getInstalledApplications(0)
    }
}

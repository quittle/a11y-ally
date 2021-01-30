package com.quittle.a11yally.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import com.quittle.a11yally.R

/**
 * [RecyclerView.Adapter] for displaying checkboxes with titles and subtitles. To update the
 * entries, manipulate the values of the state, then call [notifyDataSetChanged] on the recycler
 * view to update the views. The checked state is handled by the checkbox update by the adapter.
 */
class CheckboxAdapter(
    val stateArray: Array<CheckboxAdapterState>,
    private val onCheckedChangeListener: (index: Int, isChecked: Boolean) -> Unit
) :
    RecyclerView.Adapter<CheckboxAdapter.Companion.CheckboxViewHolder>() {
    companion object {
        class CheckboxViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        data class CheckboxAdapterState(
            val checkboxAdapterEntry: CheckboxAdapterEntry,
            var checked: Boolean,
            var enabled: Boolean,
            var visible: Boolean
        )

        data class CheckboxAdapterEntry(val title: String, val subtitle: String) :
            Comparable<CheckboxAdapterEntry> {
            override fun compareTo(other: CheckboxAdapterEntry): Int {
                return COMPARATOR.compare(this, other)
            }

            private companion object {
                private val COMPARATOR = compareBy<CheckboxAdapterEntry>(
                    { it.title },
                    { it.subtitle }
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return stateArray.size
    }

    override fun onBindViewHolder(holder: CheckboxViewHolder, position: Int) {
        holder.view.run {
            val state = stateArray[position]
            isEnabled = state.enabled
            visibility = if (state.visible) { View.VISIBLE } else { View.GONE }
            layoutParams = if (state.visible) {
                RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            } else {
                RecyclerView.LayoutParams(0, 0)
            }

            findViewById<AppCompatCheckBox>(R.id.checkbox).run {
                // First clear any potential, previous, check-change listeners
                setOnCheckedChangeListener(null)
                isChecked = state.checked
                isEnabled = state.enabled
                setOnCheckedChangeListener { _, isChecked: Boolean ->
                    state.checked = isChecked
                    onCheckedChangeListener(position, isChecked)
                }
            }
            findViewById<TextView>(R.id.title).run {
                isEnabled = state.enabled
                text = state.checkboxAdapterEntry.title
            }
            findViewById<TextView>(R.id.subtitle).run {
                isEnabled = state.enabled
                text = state.checkboxAdapterEntry.subtitle
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckboxViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.checkbox_adapter_entry, parent, false).apply {
                val checkbox = findViewById<View>(R.id.checkbox)
                findViewById<View>(R.id.title).setOnClickListener { checkbox.performClick() }
                findViewById<View>(R.id.subtitle).setOnClickListener { checkbox.performClick() }
            }
        return CheckboxViewHolder(view)
    }
}

package com.quittle.a11yally.lifecycle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

/**
 * An amalgam of LiveData whose value is true if all its sources are true. Otherwise it's value is
 * false. If no sources are specified, then it resolves to false
 */
class AllTrueLiveData(vararg sources: LiveData<Boolean>) : MediatorLiveData<Boolean>() {
    private val dataMap: Map<LiveData<Boolean>, Boolean>
    init {
        this.dataMap = mutableMapOf()
        for (source in sources) {
            this.addSource(source) { v ->
                this.dataMap[source] = v

                this.value = calculateValue()
            }
            this.dataMap[source] = source.value!!
        }
        this.value = calculateValue()
    }

    private fun calculateValue(): Boolean {
        if (this.dataMap.isEmpty()) {
            return false
        }

        return !this.dataMap.containsValue(false)
    }
}

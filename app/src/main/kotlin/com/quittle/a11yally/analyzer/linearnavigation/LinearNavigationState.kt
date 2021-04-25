package com.quittle.a11yally.analyzer.linearnavigation

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Represents a scroll offset
 */
data class LinearNavigationScrollOffset(val x: Int = 0, val y: Int = 0)

data class LinearNavigationEntry(val text: String, val node: AccessibilityNodeInfo)

data class LinearNavigationState(
    val entries: MutableList<LinearNavigationEntry> = mutableListOf(),
    val prevEntries: Iterable<LinearNavigationEntry> = listOf(),
    val prevOffset: LinearNavigationScrollOffset = LinearNavigationScrollOffset()
) {
    private companion object {
        const val ENTRY_DIVERGENCE_THRESHOLD = 0.8
    }

    fun next(offset: LinearNavigationScrollOffset): LinearNavigationState {
        return LinearNavigationState(prevEntries = this.entries, prevOffset = offset)
    }

    fun haveEntriesDiverged(): Boolean {
        val curText = entries.map(LinearNavigationEntry::text)
        val prevText = prevEntries.map(LinearNavigationEntry::text)
        val intersection = curText.intersect(prevText)
        if (curText.isEmpty()) {
            return prevText.isNotEmpty()
        }
        val similarity = intersection.size.toDouble() / curText.distinct().size
        return similarity < ENTRY_DIVERGENCE_THRESHOLD
    }
}

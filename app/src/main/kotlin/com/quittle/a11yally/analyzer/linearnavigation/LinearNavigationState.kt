package com.quittle.a11yally.analyzer.linearnavigation

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Represents a scroll offset in the linear navigation mode
 * @param x The scroll x-offset
 * @param y The scroll y-offset
 */
data class LinearNavigationScrollOffset(val x: Int = 0, val y: Int = 0)

/**
 * Represents all the data needed for rendering a row in linear navigation mode
 * @param text The text to render for the entry
 * @param node The node to forward accessibility events to. e.g. performClick()
 */
data class LinearNavigationEntry(val text: String, val node: AccessibilityNodeInfo)

/**
 * Simple abstraction of the linear navigation state.
 */
data class LinearNavigationState(
    val entries: MutableList<LinearNavigationEntry> = mutableListOf(),
    val prevEntries: Iterable<LinearNavigationEntry> = listOf(),
    val prevOffset: LinearNavigationScrollOffset = LinearNavigationScrollOffset()
) {
    private companion object {
        const val ENTRY_DIVERGENCE_THRESHOLD = 0.8
    }

    /**
     * Creates a new navigation state based on the current one
     * @param offset The new offset to set
     * @return A new instance, based on the current state
     */
    fun next(offset: LinearNavigationScrollOffset): LinearNavigationState {
        return LinearNavigationState(prevEntries = this.entries, prevOffset = offset)
    }

    /**
     * Uses heuristics to determine if the the current entries have significantly diverged from the
     * previous entries. Expected minor divergences include status text creation or checkbox checked
     * status updates.
     * @return true if the current entries differs "significantly" from the previous entries.
     */
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

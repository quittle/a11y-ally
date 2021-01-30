package com.quittle.a11yally.analyzer

import android.graphics.Rect

/**
 * Types of issues A11y Ally can report
 */
enum class IssueType {
    UnlabeledNode,
    SmallTouchTarget,
}

/**
 * Represents a single accessibility issue at a point in time
 * @property type A human readable name for the type of issue represented
 * @property area The area of the screen where the issue occurred
 * @property info Additional metadata relevant to the issue. This map may be complex and nested but
 *                must be trivially serializable.
 */
data class AccessibilityIssue(
    val type: IssueType,
    val area: Rect,
    val info: Map<String, Any>
)

/**
 * Listener for issues being detected by the framework
 */
interface AccessibilityIssueListener {
    /**
     * Called when new issues are detected. This may be called multiple times in a row without calls
     * to [onInvalidateIssues], which should still count as invalidating the old issues, with the
     * new ones replacing them
     * @param issues - The issues reported
     */
    fun onIssues(issues: Collection<AccessibilityIssue>)

    /**
     * Called when previously reported issues are no longer pertinent
     */
    fun onInvalidateIssues()
}

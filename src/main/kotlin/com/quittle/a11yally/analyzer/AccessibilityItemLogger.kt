package com.quittle.a11yally.analyzer

import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

private const val TAG: String = "AccessibilityItemLogger"

/**
 * Logs accessibility errors to Logcat.
 */
class AccessibilityItemLogger : AccessibilityAnalyzer.AccessibilityItemEventListener {
    override fun onAccessibilityEventStart() {}

    override fun onAccessibilityEventEnd() {}

    override fun onAccessibilityNodeInfo(node: AccessibilityNodeInfo) {
        if (node.isFocusable && node.text === null && node.contentDescription === null) {
            Log.i(TAG, "Missing text: " + node.className)
        }
    }

    override fun onNonWhitelistedApp() {}
}

package com.quittle.a11yally.analyzer

import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import org.json.JSONObject

private const val TAG: String = "AccessibilityItemLogger"

/**
 * Logs accessibility errors to Logcat.
 */
class AccessibilityItemLogger : AccessibilityAnalyzer.AccessibilityItemEventListener {
    override fun onPause() {}

    override fun onResume() {}

    override fun onAccessibilityEventStart() {}

    override fun onAccessibilityEventEnd() {}

    override fun onAccessibilityNodeInfo(node: AccessibilityNodeInfo) {
        if (isUnlabeledNode(node)) {
            val summaryMap = AccessibilityNodeSummary(node).getSummary()
            val nodeSummary = JSONObject(summaryMap).toString(4)
            Log.i(TAG, "Unlabeled node found: $nodeSummary")
        }
    }

    override fun onNonWhitelistedApp() {}
}

package com.quittle.a11yally.analyzer

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo
import com.quittle.a11yally.analyzer.listeners.AccessibilityNodeSummary
import com.quittle.a11yally.preferences.PreferenceProvider

/**
 * Analyzes accessibility nodes as they are exposed by the OS and reports issues it finds
 */
class AccessibilityNodeIssueAnalyzer(
    context: Context,
    private val mAccessibilityIssueListener: AccessibilityIssueListener
) :
                AccessibilityItemEventListener {
    private val mAccessibilityNodeAnalyzer = AccessibilityNodeAnalyzer(context)
    private val mPreferenceProvider by lazy { PreferenceProvider(context, true) }
    private val mCurrentIssues = mutableListOf<AccessibilityIssue>()

    override fun onAccessibilityEventStart() {
        mCurrentIssues.clear()
    }

    override fun onAccessibilityEventEnd() {
        mAccessibilityIssueListener.onIssues(mCurrentIssues)
    }

    override fun onNonWhitelistedApp() {
        mCurrentIssues.clear()
        mAccessibilityIssueListener.onInvalidateIssues()
    }

    override fun onPause() {
        mPreferenceProvider.onPause()
        mAccessibilityIssueListener.onInvalidateIssues()
    }

    override fun onResume() {
        mPreferenceProvider.onResume()
    }

    override fun onAccessibilityNodeInfo(node: AccessibilityNodeInfo) {
        val nodePosition = mAccessibilityNodeAnalyzer.getBoundsInScreen(node)
        val nodeSummary = AccessibilityNodeSummary(node).getSummary()

        if (mPreferenceProvider.getHighlightMissingLabels() &&
                mAccessibilityNodeAnalyzer.isUnlabeledNode(node)) {
            mCurrentIssues.add(
                    AccessibilityIssue(IssueType.UnlabeledNode, nodePosition, nodeSummary))
        }
        if (mPreferenceProvider.getHighlightSmallTouchTargets() &&
                mAccessibilityNodeAnalyzer.isNodeSmallTouchTarget(node,
                        mPreferenceProvider.getSmallTouchTargetSize())) {
            mCurrentIssues.add(
                    AccessibilityIssue(IssueType.SmallTouchTarget, nodePosition, nodeSummary))
        }
    }
}

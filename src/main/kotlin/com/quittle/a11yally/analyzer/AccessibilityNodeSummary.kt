package com.quittle.a11yally.analyzer

import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import com.quittle.a11yally.isNotNull

/**
 * Provides summaries of accessibility nodes
 * @param node The accessibility node to summarize
 */
class AccessibilityNodeSummary(node: AccessibilityNodeInfo) {
    private val mNode: AccessibilityNodeInfo = node

    /**
     * Provides a summary of all known identifying features of a node.
     * @return A concise summary of the node
     */
    fun getSummary(): Map<String, Any?> {
        var info = mapOf(
                Pair("windowTitle", getWindowTitle()),
                Pair("paneTitle", getPaneTitle()),
                Pair("boundsInScreen", getBoundsInScreen().toShortString()),
                Pair("windowType", getWindowType()),
                Pair("viewIdResourceName", getViewIdResourceName()),
                Pair("tooltipText", getTooltipText()),
                Pair("packageName", getPackageName()),
                Pair("hintText", getHintText()),
                Pair("text", getText()),
                Pair("nodeClassPath", getNodeClassPath()))
        info = info.filterValues { value -> value.isNotNull() }
        return info.toSortedMap()
    }

    private fun getWindowTitle(): CharSequence? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mNode.window?.title
        } else {
            null
        }
    }

    private fun getPaneTitle(): CharSequence? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mNode.paneTitle
        } else {
            null
        }
    }

    private fun getBoundsInScreen(): Rect {
        val rect = Rect()
        mNode.getBoundsInScreen(rect)
        return rect
    }

    private fun getWindowType(): CharSequence? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val windowType = mNode.window?.type
            when (windowType) {
                AccessibilityWindowInfo.TYPE_SYSTEM -> "TYPE_SYSTEM"
                AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY -> "TYPE_ACCESSIBILITY_OVERLAY"
                AccessibilityWindowInfo.TYPE_APPLICATION -> "TYPE_APPLICATION"
                AccessibilityWindowInfo.TYPE_INPUT_METHOD -> "TYPE_INPUT_METHOD"
                AccessibilityWindowInfo.TYPE_SPLIT_SCREEN_DIVIDER -> "TYPE_SPLIT_SCREEN_DIVIDER"
                null -> null
                else -> "Unknown: $windowType"
            }
        } else {
            null
        }
    }

    private fun getViewIdResourceName(): CharSequence? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mNode.viewIdResourceName
        } else {
            null
        }
    }

    private fun getTooltipText(): CharSequence? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mNode.tooltipText
        } else {
            null
        }
    }

    private fun getPackageName(): CharSequence? {
        return mNode.packageName
    }

    private fun getHintText(): CharSequence? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNode.hintText
        } else {
            null
        }
    }

    private fun getText(): CharSequence? {
        return mNode.text
    }

    private fun getNodePath(): List<AccessibilityNodeInfo> {
        val ret = mutableListOf<AccessibilityNodeInfo>()

        var node: AccessibilityNodeInfo? = mNode
        do {
            ret.add(0, node!!)
            node = node.parent
        } while (node.isNotNull())

        return ret
    }

    private fun getNodeClassPath(): List<CharSequence> {
        return getNodePath().map(AccessibilityNodeInfo::getClassName)
    }
}

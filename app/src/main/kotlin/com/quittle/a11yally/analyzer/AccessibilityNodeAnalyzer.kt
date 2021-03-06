package com.quittle.a11yally.analyzer

import android.content.Context
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.quittle.a11yally.base.isNotNull
import com.quittle.a11yally.base.isNull

class AccessibilityNodeAnalyzer(context: Context) {
    private val mScreenDensity = context.resources.displayMetrics.density

    /**
     * Determines if a node is unlabeled when it should be
     */
    fun isUnlabeledNode(node: AccessibilityNodeInfo): Boolean {
        return node.isFocusable &&
            node.childCount == 0 &&
            node.text.isNull() &&
            getContentDescription(node).isNull()
    }

    fun isNodeSmallTouchTarget(node: AccessibilityNodeInfo, minTouchTargetSizeDp: Int): Boolean {
        if (node.isClickable || node.isLongClickable || node.isScrollable) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            return rect.width() / mScreenDensity < minTouchTargetSizeDp ||
                rect.height() / mScreenDensity < minTouchTargetSizeDp
        }
        return false
    }

    /**
     * Determines if a node is likely to be determined focusable by the content
     */
    fun isNodeLikelyFocusable(node: AccessibilityNodeInfo): Boolean {
        return node.text.isNotNull() ||
            (node.isFocusable && node.childCount == 0) ||
            getContentDescription(node).isNotNull()
    }

    fun getContentDescription(node: AccessibilityNodeInfo): CharSequence? {
        return if (node.labeledBy.isNotNull()) {
            node.labeledBy.contentDescription ?: node.labeledBy.text
        } else {
            node.contentDescription
        }
    }

    /**
     * Gets the location of the node on the screen
     */
    fun getBoundsInScreen(node: AccessibilityNodeInfo): Rect {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        return rect
    }
}

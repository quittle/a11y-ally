package com.quittle.a11yally.analyzer

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import com.quittle.a11yally.isNotNull
import com.quittle.a11yally.isNull

class AccessibilityNodeAnalyzer(context: Context) {
    companion object {
        private const val MIN_TOUCH_TARGET_DP = 48
    }

    val mScreenDensity = context.resources.displayMetrics.density

    /**
     * Determines if a node is unlabeled when it should be
     */
    fun isUnlabeledNode(node: AccessibilityNodeInfo): Boolean {
        return node.isFocusable &&
                node.childCount == 0 &&
                node.text.isNull() &&
                getContentDescription(node).isNull()
    }

    fun isNodeSmallTouchTarget(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable || node.isLongClickable || node.isScrollable) {
            val rect = Rect()
            node.getBoundsInParent(rect)
            return rect.width() / mScreenDensity < MIN_TOUCH_TARGET_DP ||
                    rect.height() / mScreenDensity < MIN_TOUCH_TARGET_DP
        }
        return false
    }

    /**
     * Determines if a node is likely to be determined focusable by the content
     */
    fun isNodeLikelyFocusable(node: AccessibilityNodeInfo): Boolean {
        return node.text.isNotNull() ||
                node.isFocusable ||
                getContentDescription(node).isNotNull()
    }

    fun getContentDescription(node: AccessibilityNodeInfo): CharSequence? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                node.labeledBy.isNotNull()) {
            node.labeledBy.contentDescription ?: node.labeledBy.text
        } else {
            node.contentDescription
        }
    }
}

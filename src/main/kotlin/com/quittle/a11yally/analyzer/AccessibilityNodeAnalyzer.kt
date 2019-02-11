package com.quittle.a11yally.analyzer

import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import com.quittle.a11yally.isNotNull
import com.quittle.a11yally.isNull

/**
 * Determines if a node is unlabeled when it should be
 */
fun isUnlabeledNode(node: AccessibilityNodeInfo): Boolean {
    return node.isFocusable &&
            node.childCount == 0 &&
            node.text.isNull() &&
            getContentDescription(node).isNull()
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
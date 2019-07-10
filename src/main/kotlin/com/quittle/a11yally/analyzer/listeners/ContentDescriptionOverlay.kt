@file:Suppress("DEPRECATION")

package com.quittle.a11yally.analyzer.listeners

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.AbsoluteLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.quittle.a11yally.R
import com.quittle.a11yally.analyzer.A11yAllyAccessibilityAnalyzer
import com.quittle.a11yally.analyzer.AccessibilityItemEventListener
import com.quittle.a11yally.analyzer.AccessibilityNodeAnalyzer
import com.quittle.a11yally.analyzer.AccessibilityOverlay
import com.quittle.a11yally.ifNotNull
import com.quittle.a11yally.isNotNull
import com.quittle.a11yally.preferences.PreferenceProvider

/**
 * Minimal set of data needed to represent a content description for displaying
 */
data class ContentDescriptionNode(
    val rect: Rect,
    val text: String
)

/**
 * Displays accessibility info visibly on the screen.
 */
class ContentDescriptionOverlay(accessibilityAnalyzer: A11yAllyAccessibilityAnalyzer) :
        AccessibilityOverlay<AbsoluteLayout>(accessibilityAnalyzer),
        AccessibilityItemEventListener {
    override val mOverlayFlags: Int =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

    private val mContext: Context = accessibilityAnalyzer.applicationContext
    private val mPreferenceProvider = PreferenceProvider(mContext)
    private val mAccessibilityNodeAnalyzer = AccessibilityNodeAnalyzer(mContext)
    private val nodes = mutableListOf<ContentDescriptionNode>()

    init {
        mPreferenceProvider.onResume()
        if (mPreferenceProvider.getServiceEnabled() &&
                mPreferenceProvider.getDisplayContentDescription()) {
            accessibilityAnalyzer.resumeListener(this)
        } else {
            accessibilityAnalyzer.pauseListener(this)
        }
        mPreferenceProvider.onDisplayContentDescriptionUpdate { enabled ->
            val serviceEnabled = mPreferenceProvider.getServiceEnabled()

            if (serviceEnabled && enabled) {
                accessibilityAnalyzer.resumeListener(this)
            } else {
                accessibilityAnalyzer.pauseListener(this)
            }
        }
    }

    override fun onAccessibilityEventStart() {
        emptyRootView()
    }

    override fun onAccessibilityNodeInfo(node: AccessibilityNodeInfo) {
        val description = mAccessibilityNodeAnalyzer.getContentDescription(node)
        if (description.isNotNull()) {
            nodes.add(ContentDescriptionNode(
                    mAccessibilityNodeAnalyzer.getBoundsInScreen(node),
                    description.toString()
            ))
        }
    }

    override fun onAccessibilityEventEnd() {
        rootView.ifNotNull { rootView ->
            val (drawViewOffsetX, drawViewOffsetY) =
                    IntArray(2).apply(rootView::getLocationOnScreen)

            nodes.forEach { node ->
                val rect = node.rect
                rect.offset(-drawViewOffsetX, -drawViewOffsetY)

                (View.inflate(mContext, R.layout.content_description_view, null) as TextView).apply {
                    text = node.text
                    width = rect.width()
                    height = rect.height()
                    x = rect.left.toFloat()
                    y = rect.top.toFloat()
                    rootView.addView(this)
                }
            }
        }
    }

    override fun onNonWhitelistedApp() {
        emptyRootView()
    }

    override fun buildRootView(): AbsoluteLayout {
        return AbsoluteLayout(mContext).apply {
            layoutParams = RelativeLayout.LayoutParams(
                    AbsoluteLayout.LayoutParams.MATCH_PARENT,
                    AbsoluteLayout.LayoutParams.MATCH_PARENT)
        }
    }

    private fun emptyRootView() {
        rootView?.removeAllViews()
        nodes.clear()
    }
}

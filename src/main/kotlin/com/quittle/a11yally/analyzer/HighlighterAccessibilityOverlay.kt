package com.quittle.a11yally.analyzer

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.quittle.a11yally.preferences.PreferenceProvider
import com.quittle.a11yally.R
import com.quittle.a11yally.ifNotNull
import com.quittle.a11yally.isNotNull

/**
 * Displays accessibility info visibly on the screen.
 */
class HighlighterAccessibilityOverlay(accessibilityAnalyzer: A11yAllyAccessibilityAnalyzer) :
        AccessibilityOverlay<RelativeLayout>(accessibilityAnalyzer) {
    override val mOverlayFlags: Int =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

    private val mContext: Context = accessibilityAnalyzer.applicationContext
    private val mPreferenceProvider = PreferenceProvider(mContext)
    private val mAccessibilityNodeAnalyzer = AccessibilityNodeAnalyzer(mContext)
    private val mLayoutInflater = LayoutInflater.from(mContext)

    override fun onAccessibilityEventStart() {
        clearDrawView()
    }

    init {
        mPreferenceProvider.onResume()
        if (mPreferenceProvider.getServiceEnabled() &&
                (mPreferenceProvider.getDisplayContentDescription() ||
                        mPreferenceProvider.getHighlightIssues())) {
            accessibilityAnalyzer.resumeListener(this)
        } else {
            accessibilityAnalyzer.pauseListener(this)
        }
        mPreferenceProvider.onHighlightIssuesUpdate { enabled ->
            val serviceEnabled = mPreferenceProvider.getServiceEnabled()

            if (serviceEnabled && (enabled || mPreferenceProvider.getDisplayContentDescription())) {
                accessibilityAnalyzer.resumeListener(this)
            } else {
                accessibilityAnalyzer.pauseListener(this)
            }
        }
        mPreferenceProvider.onDisplayContentDescriptionUpdate { enabled ->
            val serviceEnabled = mPreferenceProvider.getServiceEnabled()

            if (serviceEnabled && (enabled || mPreferenceProvider.getHighlightIssues())) {
                accessibilityAnalyzer.resumeListener(this)
            } else {
                accessibilityAnalyzer.pauseListener(this)
            }
        }
    }

    override fun onAccessibilityEventEnd() {
        // Nothing to do
    }

    override fun onAccessibilityNodeInfo(node: AccessibilityNodeInfo) {
        rootView.ifNotNull {
            val textView = buildAndAttachHighlightNode(it, node)
            val nodeContentDescription = mAccessibilityNodeAnalyzer.getContentDescription(node)
            val highlightIssues = mPreferenceProvider.getHighlightIssues()

            @DrawableRes val backgroundResourceId: Int
            @ColorRes val textColorResourceId: Int
            if (highlightIssues &&
                    mPreferenceProvider.getHighlightMissingLabels() &&
                    mAccessibilityNodeAnalyzer.isUnlabeledNode(node)) {
                textColorResourceId = R.color.content_description_text
                backgroundResourceId = R.color.highlight_issue_unlabeled_node
            } else if (highlightIssues &&
                    mPreferenceProvider.getHighlightSmallTouchTargets() &&
                    mAccessibilityNodeAnalyzer.isNodeSmallTouchTarget(
                            node, mPreferenceProvider.getSmallTouchTargetSize())) {
                textColorResourceId = R.color.content_description_text
                backgroundResourceId = R.drawable.small_touch_target_background
            } else if (mPreferenceProvider.getDisplayContentDescription() &&
                    nodeContentDescription.isNotNull()) {
                textView.text = nodeContentDescription
                textColorResourceId = R.color.content_description_text
                backgroundResourceId = R.drawable.content_description_background
            } else {
                textColorResourceId = -1
                backgroundResourceId = -1
            }

            if (backgroundResourceId != -1) {
                textView.setBackgroundResource(backgroundResourceId)
            }
            if (textColorResourceId != -1) {
                textView.setTextColor(ResourcesCompat.getColor(
                        mContext.resources, textColorResourceId, mContext.theme))
            }
            textView.refreshDrawableState()
        }
    }

    override fun onNonWhitelistedApp() {
        clearDrawView()
    }

    override fun buildRootView(): RelativeLayout {
        return RelativeLayout(mContext).apply {
            layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT)
        }
    }

    private fun buildAndAttachHighlightNode(parentView: RelativeLayout,
                                            node: AccessibilityNodeInfo): TextView {
        val textView =
                mLayoutInflater.inflate(R.layout.highlight_node_view, parentView, false) as TextView

        // Create the rect and pass it as the out parameter to getBoundsInScreen
        val rect = Rect().apply(node::getBoundsInScreen)

        val params = RelativeLayout.LayoutParams(rect.width(), rect.height()).apply {
            // Create the array, pass it as the out parameter to getLocationOnScreen and destructure
            // it apart
            val (drawViewOffsetX, drawViewOffsetY) =
                    IntArray(2).apply(parentView::getLocationOnScreen)

            leftMargin = rect.left - drawViewOffsetX
            topMargin = rect.top - drawViewOffsetY
        }
        parentView.addView(textView, params)
        return textView
    }

    private fun clearDrawView() {
        rootView?.removeAllViews()
    }
}

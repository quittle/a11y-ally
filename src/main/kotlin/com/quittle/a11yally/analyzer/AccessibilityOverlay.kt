package com.quittle.a11yally.analyzer

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.quittle.a11yally.BuildConfig.TAG
import com.quittle.a11yally.preferences.PreferenceProvider
import com.quittle.a11yally.R
import com.quittle.a11yally.ifNotNull
import com.quittle.a11yally.isNotNull
import com.quittle.a11yally.isNull

/**
 * Displays accessibility info visibly on the screen.
 */
class AccessibilityOverlay(accessibilityAnalyzer: A11yAllyAccessibilityAnalyzer) :
        AccessibilityAnalyzer.AccessibilityItemEventListener {
    private companion object {
        private const val OVERLAY_FLAGS: Int =
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

        private const val PIXEL_FORMAT: Int = PixelFormat.TRANSLUCENT

        @Suppress("deprecation", "TopLevelPropertyNaming")
        private val OVERLAY_TYPE: Int = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
    }

    private var mDrawView: RelativeLayout? = null
    private val mContext: Context = accessibilityAnalyzer.applicationContext
    private val mPreferenceProvider = PreferenceProvider(mContext)
    private val mAccessibilityNodeAnalyzer = AccessibilityNodeAnalyzer(mContext)

    override fun onAccessibilityEventStart() {
        clearDrawView()
    }

    init {
        onResume()
    }

    override fun onAccessibilityEventEnd() {
        // Nothing to do
    }

    override fun onAccessibilityNodeInfo(node: AccessibilityNodeInfo) {
        if (mDrawView.isNull()) {
            return
        }

        val textView = buildHighlightNode(mDrawView!!, node)
        val nodeContentDescription = mAccessibilityNodeAnalyzer.getContentDescription(node)

        val resourceId: Int
        if (mPreferenceProvider.getHighlightIssues() &&
                mPreferenceProvider.getHighlightMissingLabels() &&
                mAccessibilityNodeAnalyzer.isUnlabeledNode(node)) {
                resourceId = R.color.highlight_issue_unlabeled_node
        } else if (mPreferenceProvider.getHighlightIssues() &&
                mPreferenceProvider.getHighlightSmallTouchTargets() &&
                mAccessibilityNodeAnalyzer.isNodeSmallTouchTarget(
                        node, mPreferenceProvider.getSmallTouchTargetSize())) {
            resourceId = R.drawable.small_touch_target_background
        } else if (mPreferenceProvider.getDisplayContentDescription() &&
                nodeContentDescription.isNotNull()) {
            textView.text = nodeContentDescription
            resourceId = R.drawable.content_description_background
        } else {
            resourceId = -1
        }

        if (resourceId != -1) {
            textView.setBackgroundResource(resourceId)
            textView.refreshDrawableState()
        }
    }

    override fun onNonWhitelistedApp() {
        clearDrawView()
    }

    override fun onResume() {
        mPreferenceProvider.onResume()

        mDrawView = buildDrawView()
        val params: WindowManager.LayoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                OVERLAY_TYPE,
                OVERLAY_FLAGS or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PIXEL_FORMAT)
        val windowManager: WindowManager =
                mContext.getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager

        try {
            windowManager.addView(mDrawView, params)
        } catch (e: WindowManager.BadTokenException) {
            Log.e(TAG, "Unable to add overlay view to window manager", e)
        }
    }

    override fun onPause() {
        mPreferenceProvider.onPause()

        mDrawView.ifNotNull {
            (mContext.getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager)
                    .removeView(it)
            mDrawView = null
        }
    }

    private fun buildDrawView(): RelativeLayout {
        val relativeLayout = RelativeLayout(mContext)
        relativeLayout.layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                OVERLAY_TYPE,
                OVERLAY_FLAGS,
                PIXEL_FORMAT)
        return relativeLayout
    }

    private fun buildHighlightNode(parentView: RelativeLayout,
                                   node: AccessibilityNodeInfo): TextView {
        val drawViewOffset = IntArray(2)
        mDrawView?.getLocationOnScreen(drawViewOffset)
        val (drawViewOffsetX, drawViewOffsetY) = drawViewOffset

        val rect = Rect()
        node.getBoundsInScreen(rect)
        val textView = TextView(mContext)
        textView.setTextColor(ResourcesCompat.getColor(
                mContext.resources, R.color.content_description_text, null))
        textView.gravity = Gravity.CENTER
        textView.setShadowLayer(4f, 1f, 1f, R.color.white)
        val params = RelativeLayout.LayoutParams(rect.width(), rect.height())
        params.leftMargin = rect.left - drawViewOffsetX
        params.topMargin = rect.top - drawViewOffsetY
        parentView.addView(textView, params)
        return textView
    }

    private fun clearDrawView() {
        mDrawView?.removeAllViews()
    }
}

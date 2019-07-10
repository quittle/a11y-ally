package com.quittle.a11yally.analyzer.listeners

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.quittle.a11yally.BuildConfig.TAG
import com.quittle.a11yally.R
import com.quittle.a11yally.analyzer.A11yAllyAccessibilityAnalyzer
import com.quittle.a11yally.analyzer.AccessibilityIssue
import com.quittle.a11yally.analyzer.AccessibilityIssueListener
import com.quittle.a11yally.analyzer.AccessibilityOverlay
import com.quittle.a11yally.analyzer.IssueType
import com.quittle.a11yally.clear
import com.quittle.a11yally.ifNotNull
import com.quittle.a11yally.preferences.PreferenceProvider

/**
 * Displays accessibility info visibly on the screen.
 */
class HighlighterAccessibilityOverlay(accessibilityAnalyzer: A11yAllyAccessibilityAnalyzer) :
        AccessibilityOverlay<RelativeLayout>(accessibilityAnalyzer), AccessibilityIssueListener {
    override val mOverlayFlags: Int =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

    private val mContext: Context = accessibilityAnalyzer.applicationContext
    private var mSurfaceView: SurfaceView? = null
    private val mPreferenceProvider = PreferenceProvider(mContext)

    private val mColorUnlabeledNode by lazy {
        ContextCompat.getColor(mContext, R.color.highlight_issue_unlabeled_node)
    }

    private val mColorSmallTouchTarget by lazy {
        ContextCompat.getColor(mContext, R.color.highlight_issue_small_touch_target)
    }

    override fun onAccessibilityEventStart() {
        clearDrawView()
    }

    override fun onAccessibilityNodeInfo(node: AccessibilityNodeInfo) {
        // Unused
    }

    init {
        mPreferenceProvider.onResume()
        if (mPreferenceProvider.getServiceEnabled() &&
                mPreferenceProvider.getHighlightIssues()) {
            accessibilityAnalyzer.resumeListener(this)
        } else {
            accessibilityAnalyzer.pauseListener(this)
        }
        mPreferenceProvider.onHighlightIssuesUpdate { enabled ->
            val serviceEnabled = mPreferenceProvider.getServiceEnabled()

            if (serviceEnabled && enabled) {
                accessibilityAnalyzer.resumeListener(this)
            } else {
                accessibilityAnalyzer.pauseListener(this)
            }
        }
    }

    override fun onAccessibilityEventEnd() {
    }

    override fun onIssues(issues: Collection<AccessibilityIssue>) {
        withLockedCanvas { surfaceView, canvas ->
            val (drawViewOffsetX, drawViewOffsetY) =
                    IntArray(2).apply(surfaceView::getLocationOnScreen)

            val paint = Paint()
            paint.isAntiAlias = true

            canvas.clear()
            issues.forEach { issue ->
                val rect = issue.area
                rect.offset(-drawViewOffsetX, -drawViewOffsetY)
                paint.color = when (issue.type) {
                    IssueType.UnlabeledNode -> mColorUnlabeledNode
                    IssueType.SmallTouchTarget -> mColorSmallTouchTarget
                }

                canvas.drawRect(rect, paint)
            }
        }
    }

    override fun onInvalidateIssues() {
        clearDrawView()
    }

    override fun onNonWhitelistedApp() {
        clearDrawView()
    }

    override fun buildRootView(): RelativeLayout {
        mSurfaceView = SurfaceView(mContext).apply {
            layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT)
            holder.setFormat(PixelFormat.TRANSPARENT)
        }
        return RelativeLayout(mContext).apply {
            layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT)
            addView(mSurfaceView)
        }
    }

    private fun clearDrawView() {
        withLockedCanvas { _, canvas ->
            canvas.clear()
        }
    }

    /**
     * Safely provides the canvas and surfaceView for drawing on
     */
    private fun withLockedCanvas(callback: (SurfaceView, Canvas) -> Unit) {
        mSurfaceView.ifNotNull { surfaceView ->
            surfaceView.holder.lockCanvas().ifNotNull { canvas ->
                try {
                    callback(surfaceView, canvas)
                } finally {
                    try {
                        surfaceView.holder.unlockCanvasAndPost(canvas)
                    } catch (e: IllegalStateException) {
                        Log.w(TAG, "Unable unlock canvas", e)
                    }
                }
            }
        }
    }
}

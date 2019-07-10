package com.quittle.a11yally.analyzer.listeners

import android.R.attr.bottom
import android.R.attr.top
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.PathEffect
import android.graphics.Picture
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.quittle.a11yally.BuildConfig.TAG
import com.quittle.a11yally.R
import com.quittle.a11yally.analyzer.A11yAllyAccessibilityAnalyzer
import com.quittle.a11yally.analyzer.AccessibilityIssue
import com.quittle.a11yally.analyzer.AccessibilityIssueListener
import com.quittle.a11yally.analyzer.AccessibilityOverlay
import com.quittle.a11yally.analyzer.IssueType
import com.quittle.a11yally.clear
import com.quittle.a11yally.ifNotNull
import com.quittle.a11yally.lifecycle.AllTrueLiveData
import com.quittle.a11yally.preferences.PreferenceProvider


/**
 * Displays accessibility info visibly on the screen.
 */
class HighlighterAccessibilityOverlay(accessibilityAnalyzer: A11yAllyAccessibilityAnalyzer) :
        AccessibilityOverlay<RelativeLayout>(accessibilityAnalyzer), AccessibilityIssueListener {
    override val mOverlayFlags: Int =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_SECURE

    private val mContext: Context = accessibilityAnalyzer.applicationContext
    private var mSurfaceView: SurfaceView? = null
    private val mPreferenceProvider = PreferenceProvider(mContext)
    private val mHighlightIssuesLiveData: LiveData<Boolean>

    private val mColorUnlabeledNode by lazy {
        ContextCompat.getColor(mContext, R.color.highlight_issue_unlabeled_node)
    }

    private val mColorSmallTouchTarget by lazy {
        ContextCompat.getColor(mContext, R.color.highlight_issue_small_touch_target)
    }

    private val mColorSmallText by lazy {
        ContextCompat.getColor(mContext, R.color.highlight_issue_small_text)
    }

    override fun onAccessibilityEventStart() {
        clearDrawView()
    }

    override fun onAccessibilityNodeInfo(node: AccessibilityNodeInfo) {
        // Unused
    }

    init {
        mPreferenceProvider.onResume()

        mHighlightIssuesLiveData = AllTrueLiveData(
                mPreferenceProvider.getHighlightIssuesLiveData(),
                mPreferenceProvider.getServiceEnabledLiveData())

        mHighlightIssuesLiveData.observe(accessibilityAnalyzer, Observer { enabled ->
            if (enabled) {
                accessibilityAnalyzer.resumeListener(this)
            } else {
                accessibilityAnalyzer.pauseListener(this)
            }
        })

        if (mHighlightIssuesLiveData.value!!) {
            accessibilityAnalyzer.resumeListener(this)
        } else {
            accessibilityAnalyzer.pauseListener(this)
        }
    }

    override fun onAccessibilityEventEnd() {
    }

    override fun onIssues(issues: Collection<AccessibilityIssue>) {
        withLockedCanvas { surfaceView, canvas ->
            val (drawViewOffsetX, drawViewOffsetY) =
                    IntArray(2).apply(surfaceView::getLocationOnScreen)

            val textSize = 40f
            val paint = Paint()
            paint.isAntiAlias = true
            paint.style = Paint.Style.STROKE
            paint.textSize = textSize
            paint.textAlign = Paint.Align.CENTER

            val textPaint = Paint(paint)
            textPaint.color = Color.parseColor("#ff000000")
            textPaint.strokeWidth = 0f
            textPaint.style = Paint.Style.FILL

            canvas.clear()
            issues.sortedBy { it.area.width() * it.area.height() }.reversed().forEach { issue ->
                val rect = Rect(issue.area)
                rect.offset(-drawViewOffsetX, -drawViewOffsetY)

                growRect(rect, 2)


                paint.style = Paint.Style.STROKE

                paint.strokeWidth = 2f
                growRect(rect, 2)
                paint.color = Color.BLACK
//                paint.pathEffect = DashPathEffect(floatArrayOf(10f, 2f), 0f)
                canvas.drawRect(rect, paint)


                canvas.save()
                rect.left += 8
                rect.right -= 8
                canvas.clipRect(rect)
                canvas.clear()

                rect.left -= 8
                rect.right += 8
                rect.top += 8
                rect.bottom -= 8
                canvas.clipRect(rect)
                canvas.clear()

                canvas.restore()


//                val text = issue.type.name
//                val textWidth = paint.measureText(text)

//                val textOffsetX = textWidth + textSize
//                val textOffsetY = textSize * 1.2f
//                canvas.save()
//                canvas.clipRect(rect)
//                canvas.clear()
//                canvas.rotate(45f, rect.exactCenterX(), rect.exactCenterY())
//                for (i in -10..10) {
//                    for (j in -10..10) {
//                        canvas.drawText(text, (j * textWidth / 3) + rect.exactCenterX() + i * textOffsetX, rect.exactCenterY() + j * textOffsetY, textPaint)
//                    }
//                }
////                val text = ((issue.type.name + " ").repeat(10) + "\n").repeat(10)
////                canvas.drawText(text, rect.exactCenterX(), rect.exactCenterY(), paint)
//                canvas.restore()

//                paint.strokeWidth = 4f
//                growRect(rect, 4)
                paint.color = when (issue.type) {
                    IssueType.UnlabeledNode -> mColorUnlabeledNode
                    IssueType.SmallTouchTarget -> mColorSmallTouchTarget
                    IssueType.SmallText -> mColorSmallText
                }
//                paint.style = Paint.Style.FILL

                // Text size
//                rect.left = rect.right - textWidth.toInt()
//                rect.bottom = rect.top
//                rect.top = rect.bottom - textSize.toInt()

                // Icon size
                rect.left = rect.right - 24
                rect.bottom = rect.top - 2
                rect.top = rect.bottom - 24

                paint.style = Paint.Style.FILL
                val icon = when (issue.type) {
                    IssueType.UnlabeledNode -> R.drawable.icon_no_label
                    IssueType.SmallTouchTarget -> R.drawable.icon_touch
                    IssueType.SmallText -> R.drawable.icon_small_text
                }
                val d: Drawable? = ContextCompat.getDrawable(mContext, icon)
                d?.bounds = rect
//                d?.draw(canvas)
//                canvas.drawPicture(Picture.createFromStream())
//                canvas.drawRect(rect, paint)
//                canvas.drawText(text, rect.exactCenterX(), rect.exactCenterY(), textPaint)


//                paint.strokeWidth = 2f
//                growRect(rect, 2)
//                paint.color = Color.BLACK
//                canvas.drawRect(rect, paint)
            }
        }
    }

    private fun growRect(rect: Rect, amount: Int) {
        rect.top -= amount
        rect.bottom += amount
        rect.left -= amount
        rect.right += amount
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

package com.quittle.a11yally.analyzer

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.quittle.a11yally.R
import com.quittle.a11yally.observer.ServiceLifecycleObserver

@Suppress("deprecation", "TopLevelPropertyNaming")
private val OVERLAY_TYPE: Int = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
} else {
    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
}

private const val OVERLAY_FLAGS: Int =
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

private const val PIXEL_FORMAT: Int = PixelFormat.TRANSLUCENT

private const val TAG: String = "AccessibilityOverlay"

/**
 * Displays accessibility info visibly on the screen.
 */
class AccessibilityOverlay(accessibilityAnalyzer: A11yAllyAccessibilityAnalyzer) :
        AccessibilityAnalyzer.AccessibilityItemEventListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        ServiceLifecycleObserver {
    private var drawView: RelativeLayout? = null
    private var displayContentDescription: Boolean = false
    private val context: Context = accessibilityAnalyzer.applicationContext

    init {
        accessibilityAnalyzer.lifecycle.addObserver(this)
    }

    override fun onAccessibilityEventStart() {
        clearDrawView()
    }

    override fun onAccessibilityEventEnd() {
        // Nothing to do
    }

    override fun onAccessibilityNodeInfo(node: AccessibilityNodeInfo) {
        if (node.isFocusable) {
            val textView = highlightNode(drawView!!, node)
            if (displayContentDescription) {
                textView.text = getContentDescription(node)
            }

            var resourceId: Int = -1
            if (node.text === null && getContentDescription(node) === null &&
                    node.childCount == 0) {
                Log.i(TAG, "Missing text: " + node.className)
                resourceId = R.color.red
            }
            if (displayContentDescription && getContentDescription(node) !== null) {
                resourceId = R.drawable.content_description_background
            }
            if (resourceId != -1) {
                textView.setBackgroundResource(resourceId)
                textView.refreshDrawableState()
            }
        }
    }

    override fun onNonWhitelistedApp() {
        clearDrawView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == context.getString(R.string.pref_display_content_description)) {
            displayContentDescription = sharedPreferences.getBoolean(key, displayContentDescription)
        }
    }

    override fun onServiceCreate() {
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.registerOnSharedPreferenceChangeListener(this)
        displayContentDescription = sharedPref.getBoolean(
                context.getString(R.string.pref_display_content_description),
                displayContentDescription)

        drawView = buildDrawView()
        val params: WindowManager.LayoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                OVERLAY_TYPE,
                OVERLAY_FLAGS or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PIXEL_FORMAT)
        val windowManager: WindowManager =
                context.getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager
        windowManager.addView(drawView, params)
    }

    override fun onServiceDestroy() {
        if (drawView !== null) {
            (context.getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager)
                    .removeView(drawView)
            drawView = null
        }
    }

    private fun buildDrawView(): RelativeLayout {
        val relativeLayout = RelativeLayout(context)
        relativeLayout.layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                OVERLAY_TYPE,
                OVERLAY_FLAGS,
                PIXEL_FORMAT)
        return relativeLayout
    }

    private fun highlightNode(parentView: RelativeLayout, node: AccessibilityNodeInfo): TextView {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        val textView = TextView(context)
        textView.setTextColor(ResourcesCompat.getColor(
                context.resources, R.color.content_description_text, null))
        textView.gravity = Gravity.CENTER
        textView.setShadowLayer(4f, 1f, 1f, R.color.white)
        val params = RelativeLayout.LayoutParams(rect.width(), rect.height())
        params.leftMargin = rect.left
        params.topMargin = rect.top
        parentView.addView(textView, params)
        return textView
    }

    private fun getContentDescription(node: AccessibilityNodeInfo): CharSequence? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                node.labeledBy !== null) {
            node.labeledBy.contentDescription
        } else {
            node.contentDescription
        }
    }

    private fun clearDrawView() {
        drawView?.removeViewsInLayout(0, drawView!!.childCount)
    }
}

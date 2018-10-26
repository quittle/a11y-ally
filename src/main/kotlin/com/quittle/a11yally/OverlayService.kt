package com.quittle.a11yally

import android.accessibilityservice.AccessibilityService
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.preference.PreferenceManager
import android.support.annotation.ColorRes
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.Gravity
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast

@Suppress("deprecation")
private val overlayType: Int = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
} else {
    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
}

private const val OVERLAY_FLAGS: Int =
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

private const val PIXEL_FORMAT: Int = PixelFormat.TRANSLUCENT

/**
 * According to the docs for {@link OnSharedPreferenceChangeListener}, the app must hold a strong
 * reference to the listener to protect against garbage collection.
 * @link SharedPreferences#registerOnSharedPreferenceChangeListener
 */
public class OverlayService : AccessibilityService(), OnSharedPreferenceChangeListener {
    var drawView: RelativeLayout? = null
    var displayContentDescription: Boolean = false

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key.equals(getString(R.string.pref_display_content_description))) {
            displayContentDescription = sharedPreferences.getBoolean(key, displayContentDescription)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        getRootInActiveWindow()?.let {
            drawView?.removeViewsInLayout(0, drawView!!.getChildCount())
            iterateAccessibilityNodeInfos(it, { node: AccessibilityNodeInfo ->
                if (node.isFocusable()) {
                    val textView = highlightNode(drawView!!, node)
                    if (displayContentDescription) {
                        textView.setText(node.getContentDescription())
                    }
                    @ColorRes var resourceId: Int = -1
                    if (node.getContentDescription() == null) {
                        Log.i("OverlayService", "Missing text: " + node.getClassName())
                        resourceId = R.color.red
                    } else if (node.getText() != null) {
                        resourceId = R.color.yellow
                    }
                    if (resourceId != -1) {
                        textView.setBackgroundResource(resourceId)
                        textView.refreshDrawableState()
                    }
                }
            })
            it.recycle()
        }
    }

    private fun highlightNode(parentView: RelativeLayout, node: AccessibilityNodeInfo): TextView {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        val textView = TextView(this)
        textView.setGravity(Gravity.CENTER)
        val params = RelativeLayout.LayoutParams(rect.width(), rect.height())
        params.leftMargin = rect.left
        params.topMargin = rect.top
        parentView.addView(textView, params)
        return textView
    }

    private fun iterateAccessibilityNodeInfos(root: AccessibilityNodeInfo,
                                              onEachCallback: (AccessibilityNodeInfo) -> Unit) {
        onEachCallback(root)
        for (i in 0..root.getChildCount() - 1) {
            root.getChild(i)?.let {
                iterateAccessibilityNodeInfos(it, onEachCallback)
                it.recycle()
            }
        }
    }

    override fun onInterrupt () {}

    override fun onCreate() {
        super.onCreate()

        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPref.registerOnSharedPreferenceChangeListener(this)
        displayContentDescription = sharedPref.getBoolean(
                getString(R.string.pref_display_content_description), displayContentDescription)

        drawView = buildDrawView()
        val params: WindowManager.LayoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                overlayType,
                OVERLAY_FLAGS or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PIXEL_FORMAT)
        params.gravity = Gravity.LEFT or Gravity.TOP
        val windowManager: WindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(drawView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(getBaseContext(), "onDestroy", Toast.LENGTH_LONG).show()
        if (drawView != null) {
            (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(drawView)
            drawView = null
        }
    }

    @Suppress("deprecation")
    private fun buildDrawView(): RelativeLayout {
        val relativeLayout = RelativeLayout(this)
        relativeLayout.setLayoutParams(WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                overlayType,
                OVERLAY_FLAGS,
                PIXEL_FORMAT))
        relativeLayout.setBackgroundColor(getResources().getColor(R.color.red))
        return relativeLayout
    }
}

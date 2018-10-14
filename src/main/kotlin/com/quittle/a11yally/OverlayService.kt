package com.quittle.a11yally

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.Gravity
import android.widget.RelativeLayout
import android.view.View
import android.view.WindowManager
import android.widget.Toast

@Suppress("deprecation")
private val overlayType: Int = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
} else {
    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
}
/*by lazy @Suppress("deprecation") {
    if (Build.VERSION.SDK_INT < 26) {
        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
    } else {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    }
}*/

private const val OVERLAY_FLAGS: Int =
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

private const val PIXEL_FORMAT: Int = PixelFormat.TRANSLUCENT

public class OverlayService : AccessibilityService() {
    var drawView: RelativeLayout? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        getRootInActiveWindow()?.let {
            drawView?.removeViewsInLayout(0, drawView!!.getChildCount())
            iterateAccessibilityNodeInfos(it, { node: AccessibilityNodeInfo ->
                if (node.getChildCount() == 0 &&
                        node.isFocusable() &&
                        node.getText() == null &&
                        node.getContentDescription() == null //&&
                        //node.getHintText() == null &&
                        //node.getLabeledBy() == null
                        ) {
                    highlightNode(drawView!!, node)
                    android.util.Log.i("OverlayService", "Missing text: " + node.getClassName())
                }
            })
            it.recycle()
        }
    }

    private fun highlightNode(parentView: RelativeLayout, node: AccessibilityNodeInfo) {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        val view = View(this)
        view.setBackgroundResource(R.color.yellow)
        val params = RelativeLayout.LayoutParams(rect.width(), rect.height())
        params.leftMargin = rect.left
        params.topMargin = rect.top
        parentView.addView(view, params)
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
        drawView = buildDrawView()
        Toast.makeText(getBaseContext(), "onCreate", Toast.LENGTH_LONG).show()
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

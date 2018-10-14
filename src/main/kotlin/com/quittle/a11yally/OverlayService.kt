package com.quittle.a11yally

import android.accessibilityservice.AccessibilityService
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.IBinder
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.Gravity
import android.widget.RelativeLayout
import android.view.View
import android.view.WindowManager
import android.widget.Toast

private val OVERLAY_TYPE : Int by lazy @Suppress("deprecation") {
    if (Build.VERSION.SDK_INT < 26) {
        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
    } else {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    }
}

private val OVERLAY_FLAGS : Int =
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

private val PIXEL_FORMAT : Int = PixelFormat.TRANSLUCENT

public class OverlayService : AccessibilityService() {
    var drawView : RelativeLayout? = null

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
                    highlightNode(drawView!!, node);
                    android.util.Log.i("OverlayService", "Missing text: " + node.getClassName())
                }
            })
            it.recycle()
        }
    }

    private fun highlightNode(parentView: RelativeLayout, node: AccessibilityNodeInfo) {
        val r = Rect()
        node.getBoundsInScreen(r)
        val v = View(this)
        v.setBackgroundResource(R.color.yellow);
        val params = RelativeLayout.LayoutParams(r.width(), r.height());
        params.leftMargin = r.left;
        params.topMargin = r.top;
        parentView.addView(v, params);
    }

    private fun iterateAccessibilityNodeInfos(root: AccessibilityNodeInfo, onEachCallback: (AccessibilityNodeInfo) -> Unit) {
        onEachCallback(root)
        for (i in 0 .. root.getChildCount() - 1) {
            root.getChild(i)?.let {
                iterateAccessibilityNodeInfos(it, onEachCallback);
                it.recycle()
            }
        }
    }

    override fun onInterrupt () {}

    override fun onCreate() {
        super.onCreate()
        drawView = buildDrawView()
        Toast.makeText(getBaseContext(), "onCreate", Toast.LENGTH_LONG).show()
        val params : WindowManager.LayoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                OVERLAY_TYPE,
                OVERLAY_FLAGS or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PIXEL_FORMAT)
        params.gravity = Gravity.LEFT or Gravity.TOP
        val wm : WindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        wm.addView(drawView, params);
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(getBaseContext(), "onDestroy", Toast.LENGTH_LONG).show()
        if (drawView != null) {
            (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(drawView);
            drawView = null;
        }
    }

    @Suppress("deprecation")
    private fun buildDrawView() : RelativeLayout {
        val v = RelativeLayout(this)
        v.setLayoutParams(WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                0, 0,
                OVERLAY_TYPE, OVERLAY_FLAGS, PIXEL_FORMAT))
        v.setBackgroundColor(getResources().getColor(R.color.red))
        return v
    }
}

package com.quittle.a11yally

import android.accessibilityservice.AccessibilityService
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.Gravity
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

private val FORMAT : Int = PixelFormat.TRANSLUCENT

public class OverlayService : AccessibilityService() {
    var drawView : View? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        getRootInActiveWindow()?.let {
            iterateAccessibilityNodeInfos(it, { node: AccessibilityNodeInfo ->
                if (node.getChildCount() == 0 &&
                        node.isFocusable() &&
                        node.getText() == null &&
                        node.getContentDescription() == null //&&
                        //node.getHintText() == null &&
                        //node.getLabeledBy() == null
                        ) {
                    android.util.Log.i("OverlayService", "Missing text for container: " + node.getClassName())
                }
            })
            it.recycle()
        }
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
        Toast.makeText(getBaseContext(),"onCreate", Toast.LENGTH_LONG).show()
        val params : WindowManager.LayoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                OVERLAY_TYPE,
                OVERLAY_FLAGS,
                FORMAT)
        params.gravity = Gravity.RIGHT or Gravity.TOP
        params.setTitle("Load Average");
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
    private fun buildDrawView() : View {
        val v = View(this)
        v.setLayoutParams(WindowManager.LayoutParams(100, 100, 200, 200, OVERLAY_TYPE, OVERLAY_FLAGS, FORMAT))
        v.setBackgroundColor(getResources().getColor(R.color.red))
        return v
    }
}

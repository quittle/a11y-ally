package com.quittle.a11yally

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
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

public class OverlayService : Service() {
    var drawView : View? = buildDrawView()

    override fun onBind(intent: Intent) : IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
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
        Toast.makeText(getBaseContext(),"onDestroy", Toast.LENGTH_LONG).show()
        if (drawView != null) {
            (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(drawView);
            drawView = null;
        }
    }

    private fun buildDrawView() : View {
        val v = View(this)
        v.setLayoutParams(WindowManager.LayoutParams(100, 100, 200, 200, OVERLAY_TYPE, OVERLAY_FLAGS, FORMAT))
        v.setBackgroundColor(getResources().getColor(R.color.red, getTheme()))
        return v
    }
}

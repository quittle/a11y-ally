package com.quittle.a11yally

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.content.ContextCompat

/**
 * Clears the canvas.
 */
fun Canvas.clear() {
    drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
}

/**
 * Gets the display metrics of the default display.
 */
fun Context.getDisplayMetrics(): DisplayMetrics {
    val windowManager = ContextCompat.getSystemService(this, WindowManager::class.java)
    val display = windowManager!!.defaultDisplay
    return DisplayMetrics().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(this)
        } else {
            display.getMetrics(this)
        }
    }
}

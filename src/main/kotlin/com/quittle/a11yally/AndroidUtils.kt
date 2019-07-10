package com.quittle.a11yally

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff

/**
 * Clears the canvas.
 */
fun Canvas.clear() {
    drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
}

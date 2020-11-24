package com.quittle.a11yally

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.TypedValue
import android.view.Display
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference

/**
 * Clears the canvas.
 */
fun Canvas.clear() {
    drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
}

fun Context.resolveAttributeResourceValue(@AttrRes attribute: Int): Int? {
    val typedValue = TypedValue()
    if (theme.resolveAttribute(attribute, typedValue, true)) {
        return typedValue.data
    } else {
        return null
    }
}

private var sDisplayContext: WeakReference<Context>? = null

fun Context.getDefaultDisplayContext(): Context {
    var context = sDisplayContext?.get()
    if (context != null) {
        return context
    }

    context = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val displayManager: DisplayManager = this.getSystemService(DisplayManager::class.java)
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        createDisplayContext(display)
    } else {
        this.applicationContext
    }
    sDisplayContext = WeakReference(context)
    return context
}

@ColorInt fun Context.getColorCompat(@ColorRes color: Int): Int {
    return ContextCompat.getColor(this, color)
}

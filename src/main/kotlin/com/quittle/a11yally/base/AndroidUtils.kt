package com.quittle.a11yally.base

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.Display
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference
import kotlin.system.measureTimeMillis

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

/**
 * Determines if the application is marked with a flag from [ApplicationInfo]
 * @param flag Must be one of the flags from [ApplicationInfo] that starts with `FLAG_`.
 * @return true if the flag is set for the application, otherwise false
 */
fun ApplicationInfo.flagged(flag: Int): Boolean {
    return flags and flag != 0
}

fun ApplicationInfo.flaggedSystem(): Boolean {
    return flagged(ApplicationInfo.FLAG_SYSTEM)
}

fun ApplicationInfo.flaggedHasCode(): Boolean {
    return flagged(ApplicationInfo.FLAG_HAS_CODE)
}

fun ApplicationInfo.flaggedDebuggable(): Boolean {
    return flagged(ApplicationInfo.FLAG_DEBUGGABLE)
}

/**
 * Times and logs how long an event took.
 * @param tag The Android tag to log with
 * @param formatTemplate The [String.format] message to log. Passes in a single, long literal, which
 *                       is the time in milliseconds it took to run [block].
 * @param block The invocation to time.
 */
fun <T> time(tag: String?, formatTemplate: String, block: () -> T): T {
    var ret: T
    val time = measureTimeMillis {
        ret = block()
    }
    Log.i(tag, formatTemplate.format(time))
    return ret
}

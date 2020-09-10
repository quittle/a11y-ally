package com.quittle.a11yally

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

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

@ColorInt fun Context.getColorCompat(@ColorRes color: Int): Int {
    return ContextCompat.getColor(this, color)
}

package com.quittle.a11yally.analytics.internal

import android.os.Build
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Switch
import android.widget.TextView
import android.widget.ToggleButton
import com.quittle.a11yally.base.orElse

data class ViewVariant(val type: String, val text: CharSequence?)

fun getViewVariant(view: View): ViewVariant {
    return when (view) {
        is Switch -> {
            ViewVariant("switch", view.text?.orElse(view.contentDescription))
        }
        is ToggleButton -> {
            ViewVariant("toggle", view.text?.orElse(view.contentDescription))
        }
        is CheckBox -> {
            ViewVariant("checkbox", view.text?.orElse(view.contentDescription))
        }
        is Button -> {
            ViewVariant("button", view.text?.orElse(view.contentDescription))
        }
        is TextView -> {
            ViewVariant("text", view.text?.orElse(view.contentDescription))
        }
        else -> {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                view.accessibilityClassName.toString()
            } else {
                view.javaClass.simpleName
            }

            ViewVariant(type, view.contentDescription)
        }
    }
}

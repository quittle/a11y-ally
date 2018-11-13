package com.quittle.a11yally.view

import android.app.Activity
import android.os.Bundle
import androidx.annotation.LayoutRes

/**
 * Simple base {@link Activity} for displaying a fixed layout with nothing more.
 */
abstract class FixedContentActivity : Activity() {
    @get:LayoutRes
    abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layoutId)
    }
}

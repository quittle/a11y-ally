package com.quittle.a11yally.activity

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

/**
 * Simple base {@link Activity} for displaying a fixed layout with nothing more.
 */
abstract class FixedContentActivity : AppCompatActivity() {
    @get:LayoutRes
    abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layoutId)
    }
}

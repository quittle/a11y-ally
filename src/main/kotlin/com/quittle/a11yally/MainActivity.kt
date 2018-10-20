package com.quittle.a11yally

import android.app.Activity
import android.content.Intent
import android.os.Bundle

public class MainActivity : Activity() {
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, OverlayService::class.java))
    }
}

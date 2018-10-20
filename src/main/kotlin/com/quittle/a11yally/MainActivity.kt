package com.quittle.a11yally

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ToggleButton

public class MainActivity : Activity() {
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        val overlayServiceIntent = Intent(this, OverlayService::class.java)
        val toggleButton: ToggleButton = findViewById(R.id.enable_service_button)
        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startService(overlayServiceIntent)
            } else {
                stopService(overlayServiceIntent)
            }
        }
    }
}

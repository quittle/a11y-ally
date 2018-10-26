package com.quittle.a11yally

import android.support.v4.app.FragmentActivity
import android.content.Intent
import android.os.Bundle
import android.widget.ToggleButton

public class MainActivity : FragmentActivity() {
    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, FixedPreferenceFragment.newInstance(R.xml.preferences))
                .commit()

        val overlayServiceIntent = Intent(this, OverlayService::class.java)

        val enableServiceToggleButton: ToggleButton = findViewById(R.id.enable_service_button)
        enableServiceToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startService(overlayServiceIntent)
            } else {
                stopService(overlayServiceIntent)
            }
        }
    }
}

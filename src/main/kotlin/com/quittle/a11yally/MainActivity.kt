package com.quittle.a11yally

import androidx.fragment.app.FragmentActivity
import android.content.Intent
import android.os.Bundle
import android.widget.ToggleButton

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.preferences_holder,
                        FixedPreferenceFragment.newInstance(R.xml.preferences))
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

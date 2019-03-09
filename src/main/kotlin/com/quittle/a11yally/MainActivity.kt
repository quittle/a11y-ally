package com.quittle.a11yally

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.annotation.XmlRes
import androidx.appcompat.app.AppCompatActivity
import com.quittle.a11yally.BuildConfig.TAG
import com.quittle.a11yally.analyzer.A11yAllyAccessibilityAnalyzer
import com.quittle.a11yally.view.CheckableCustomCardView
import com.quittle.a11yally.view.CustomCardView
import com.quittle.a11yally.view.MultiAppSelectionDialog

class MainActivity : AppCompatActivity() {
    private companion object {
        private val ANALYZER_CLASS_NAME = A11yAllyAccessibilityAnalyzer::class.simpleName
        private val FEATURE_SETTINGS_ACTIVITY_CLASS = FeatureSettingsActivity::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        // Disable for now to not affect the existing app
        // convertViewIntoFeaturePreferencesButton(R.id.toggle_highlight_issues,
        //                                         R.xml.highlight_issues_preferences)

        findViewById<View>(R.id.permissions_check).setOnClickListener {
            if (checkForAndRequestPermissions()) {
                Toast.makeText(
                        this,
                        resources.getText(R.string.all_permissions_granted),
                        Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.open_unfriendly_activity_button).setOnClickListener {
            startActivity(Intent(this, UnfriendlyActivity::class.java))
        }

        findViewById<CustomCardView>(R.id.toggle_app_selection).setOnClickListener {
            MultiAppSelectionDialog().showNow(supportFragmentManager, null)
        }
    }

    /**
     * Checks if all permissions required are found.
     * @return True if everything is fine, false if some necessary permissions aren't available.
     */
    private fun checkForAndRequestPermissions(): Boolean {
        var noMissingPermissions = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !Settings.canDrawOverlays(this)) {
            noMissingPermissions = false
            startActivityIntent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, true)
        }

        val serviceEnabled = (getSystemService(Context.ACCESSIBILITY_SERVICE) as
                AccessibilityManager)
                .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK)
                .map(AccessibilityServiceInfo::getId)
                .any("$packageName/.analyzer.$ANALYZER_CLASS_NAME"::equals)

        if (!serviceEnabled) {
            noMissingPermissions = false
            try {
                startActivityIntent(Settings.ACTION_ACCESSIBILITY_SETTINGS, true)
            } catch (e: ActivityNotFoundException) {
                try {
                    startActivityIntent(Settings.ACTION_ACCESSIBILITY_SETTINGS, false)
                } catch (e: ActivityNotFoundException) {
                    Log.w(TAG, "Unable to show accessibility settings")
                }
            }
        }
        return noMissingPermissions
    }

    private fun startActivityIntent(action: String, targetPackage: Boolean) {
        val intent = if (targetPackage) {
            Intent(action, Uri.parse("package:$packageName"))
        } else {
            Intent(action)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun convertToFeaturePreferencesButton(view_id: Int, @XmlRes preferences: Int) {
        findViewById<CheckableCustomCardView>(view_id).let { view ->
            view.setOnClickListener {
                startActivity(Intent(applicationContext, FEATURE_SETTINGS_ACTIVITY_CLASS).apply {
                    putExtra(FeatureSettingsActivity.EXTRA_KEY_IMAGE_RESOURCE_ID,
                            view.getImageResource())
                    putExtra(FeatureSettingsActivity.EXTRA_KEY_PREFERENCE_XML_RESOURCE_ID,
                            preferences)
                })
            }
        }
    }
}

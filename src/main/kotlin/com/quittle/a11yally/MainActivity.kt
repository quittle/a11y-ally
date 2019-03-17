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
import android.widget.ImageButton
import androidx.annotation.DrawableRes
import androidx.annotation.XmlRes
import androidx.appcompat.app.AppCompatActivity
import com.quittle.a11yally.BuildConfig.TAG
import com.quittle.a11yally.analyzer.A11yAllyAccessibilityAnalyzer
import com.quittle.a11yally.preferences.PreferenceProvider
import com.quittle.a11yally.view.ButtonSwitch
import com.quittle.a11yally.view.MultiAppSelectionDialog

class MainActivity : AppCompatActivity() {
    private companion object {
        private val ANALYZER_CLASS_NAME = A11yAllyAccessibilityAnalyzer::class.simpleName
        private val FEATURE_SETTINGS_ACTIVITY_CLASS = FeatureSettingsActivity::class.java
    }

    lateinit var mPreferenceProvider: PreferenceProvider
    lateinit var mStatusButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPreferenceProvider = PreferenceProvider(this)

        setContentView(R.layout.main_activity)

        setUpButtonSwitchAsFeaturePreferencesButton(
                R.id.highlight_issues,
                R.xml.highlight_issues_preferences,
                R.drawable.highlight_icon)
        setUpButtonSwitchAsFeaturePreferencesButton(
                R.id.display_content_descriptions,
                R.xml.content_description_preferences,
                R.drawable.display_content_descriptions_icon)

        mStatusButton = findViewById<ImageButton>(R.id.status_button)

        findViewById<View>(R.id.open_unfriendly_activity_button).setOnClickListener {
            startActivity(Intent(this, UnfriendlyActivity::class.java))
        }

        findViewById<View>(R.id.toggle_app_selection).setOnClickListener {
            MultiAppSelectionDialog().showNow(supportFragmentManager, null)
        }
    }

    override fun onPause() {
        super.onPause()
        mPreferenceProvider.onPause()
    }

    override fun onResume() {
        super.onResume()
        mPreferenceProvider.onResume()
        updateStatusButton()
    }

    private fun updateStatusButton() {
        val statusResource: Int
        val statusMessage: Int
        val onClickListener: (View) -> Unit

        when {
            isMissingPermissions() -> {
                statusResource = R.drawable.warning_icon
                statusMessage = R.string.check_permissions_button_label
                onClickListener = {
                    mPreferenceProvider.setServiceEnabled(true)
                    requestPermissions()
                    updateStatusButton()
                }
            }

            mPreferenceProvider.getServiceEnabled() -> {
                statusResource = R.drawable.service_status_icon
                statusMessage = R.string.enable_service_button_enabled
                onClickListener = {
                    mPreferenceProvider.setServiceEnabled(false)
                    updateStatusButton()
                }
            }

            else -> {
                statusResource = R.drawable.service_status_disabled_icon
                statusMessage = R.string.enable_service_button_disabled
                onClickListener = {
                    mPreferenceProvider.setServiceEnabled(true)
                    updateStatusButton()
                }
            }
        }

        mStatusButton.setImageResource(statusResource)
        mStatusButton.contentDescription = getString(statusMessage)
        mStatusButton.setOnClickListener(onClickListener)
    }

    /**
     * Checks if the app is missing a permission to draw over other apps.
     * @return true if there is a missing permission
     */
    private fun isMissingDrawOverlaysPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !Settings.canDrawOverlays(this)
    }

    /**
     * Checks if the app is not set up and enabled as an accessibility service.
     * @return true if the app is not set up as an accessibility service.
     */
    private fun isMissingAccessibilityServicePermission(): Boolean {
        return (getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager)
                .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK)
                .map(AccessibilityServiceInfo::getId)
                .any("$packageName/.analyzer.$ANALYZER_CLASS_NAME"::equals)
                .not()
    }

    /**
     * Checks if there are any permissions missing for the app to run correctly.
     */
    private fun isMissingPermissions(): Boolean {
        return isMissingDrawOverlaysPermission() || isMissingAccessibilityServicePermission()
    }

    /**
     * Checks if all permissions required are found.
     * @return True if everything is fine, false if some necessary permissions aren't available.
     */
    private fun requestPermissions() {
        if (isMissingDrawOverlaysPermission()) {
            startActivityIntent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, true)
        }

        if (isMissingAccessibilityServicePermission()) {
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
    }

    /**
     * Sends a start activity intent.
     * @param action The action of the intent
     * @param targetPackage If true, specifies the A11y Ally package as the uri of the intent
     */
    private fun startActivityIntent(action: String, targetPackage: Boolean) {
        val intent: Intent
        if (targetPackage) {
            intent = Intent(action, Uri.parse("package:$packageName"))
        } else {
            intent = Intent(action)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    /**
     * Helper function for setting up the {@link ButtonSwitch}es to open the preferences panel when
     * clicked.
     */
    private fun setUpButtonSwitchAsFeaturePreferencesButton(view_id: Int,
                                                            @XmlRes preferences: Int,
                                                            @DrawableRes imageResource: Int) {
        findViewById<ButtonSwitch>(view_id).let { view ->
            view.getButton().setOnClickListener {
                val intent = Intent(applicationContext, FEATURE_SETTINGS_ACTIVITY_CLASS)
                intent.putExtra(FeatureSettingsActivity.EXTRA_KEY_IMAGE_RESOURCE_ID, imageResource)
                intent.putExtra(
                        FeatureSettingsActivity.EXTRA_KEY_PREFERENCE_XML_RESOURCE_ID, preferences)
                startActivity(intent)
            }
        }
    }
}

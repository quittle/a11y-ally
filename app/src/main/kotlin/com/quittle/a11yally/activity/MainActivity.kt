package com.quittle.a11yally.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.XmlRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.quittle.a11yally.PermissionsManager
import com.quittle.a11yally.R
import com.quittle.a11yally.preferences.PreferenceProvider
import com.quittle.a11yally.view.ButtonSwitch

class MainActivity : AppCompatActivity() {
    private companion object {
        @JvmField
        val FEATURE_SETTINGS_ACTIVITY_CLASS = FeatureSettingsActivity::class.java
    }

    lateinit var mPreferenceProvider: PreferenceProvider
    lateinit var mPermissionsManager: PermissionsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPreferenceProvider = PreferenceProvider(this)
        mPermissionsManager = PermissionsManager(this)

        setContentView(R.layout.main_activity)

        findViewById<SwitchCompat>(R.id.service_check_box).run {
            isChecked = mPreferenceProvider.getServiceEnabled()
            mPreferenceProvider.onServiceEnabledUpdate(this::setChecked)
            setOnCheckedChangeListener { _, enabled ->
                mPreferenceProvider.setServiceEnabled(enabled)
            }
        }

        setUpButtonSwitchAsFeaturePreferencesButton(
            R.id.highlight_issues,
            R.xml.highlight_issues_preferences,
            R.drawable.highlight_icon
        )
        setUpButtonSwitchAsFeaturePreferencesButton(
            R.id.display_content_descriptions,
            R.xml.content_description_preferences,
            R.drawable.display_content_descriptions_icon
        )
        // Note: The feature preferences screen has an issue where the icon appears below the only
        // entry
        setUpButtonSwitchAsFeaturePreferencesButton(
            R.id.linear_navigation,
            R.xml.linear_navigation_preferences,
            R.drawable.linear_navigation_icon
        )

        findViewById<View>(R.id.open_unfriendly_activity_button).setOnClickListener {
            startActivity(Intent(this, UnfriendlyActivity::class.java))
        }

        findViewById<View>(R.id.toggle_app_selection).setOnClickListener {
            startActivity(Intent(this, MultiAppSelectionActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.learn_more) {
            startActivity(Intent(this, LearnMoreActivity::class.java))
            return true
        }
        return false
    }

    override fun onPause() {
        super.onPause()
        mPreferenceProvider.onPause()
    }

    override fun onResume() {
        super.onResume()
        mPreferenceProvider.onResume()

        if (!mPermissionsManager.hasAllPermissions()) {
            startActivity(Intent(this, PermissionsActivity::class.java))
        }
    }

    /**
     * Helper function for setting up the {@link ButtonSwitch}es to open the preferences panel when
     * clicked.
     */
    private fun setUpButtonSwitchAsFeaturePreferencesButton(
        viewId: Int,
        @XmlRes preferences: Int,
        @DrawableRes imageResource: Int
    ) {
        findViewById<ButtonSwitch>(viewId).let { view ->
            view.getButton().setOnClickListener {
                val intent = Intent(applicationContext, FEATURE_SETTINGS_ACTIVITY_CLASS)
                intent.putExtra(FeatureSettingsActivity.EXTRA_KEY_IMAGE_RESOURCE_ID, imageResource)
                intent.putExtra(
                    FeatureSettingsActivity.EXTRA_KEY_PREFERENCE_XML_RESOURCE_ID, preferences
                )
                startActivity(intent)
            }
        }
    }
}

package com.quittle.a11yally.activity

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.quittle.a11yally.BuildConfig.TAG
import com.quittle.a11yally.R
import com.quittle.a11yally.isNullOrZero
import com.quittle.a11yally.view.FixedPreferenceFragment

/**
 * Manages the settings for a given feature set.
 */
class FeatureSettingsActivity : FixedContentActivity() {
    companion object {
        /**
         * Must be passed as the key in the extras field of the starting intent.
         * This should be the resource id of a preference screen xml.
         */
        const val EXTRA_KEY_PREFERENCE_XML_RESOURCE_ID = "preference xml resource id"

        /**
         * Must be passed as the key in the extras field of the starting intent.
         * This should be the resource id of the hero image for the feature, appearing above the
         * preferences.
         */
        const val EXTRA_KEY_IMAGE_RESOURCE_ID = "image resource id"
    }

    override val layoutId = R.layout.feature_settings_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent?.extras
        val preferenceXmlResourceId = extras?.getInt(EXTRA_KEY_PREFERENCE_XML_RESOURCE_ID)
        val imageResourceId = extras?.getInt(EXTRA_KEY_IMAGE_RESOURCE_ID)

        if (preferenceXmlResourceId.isNullOrZero() || imageResourceId.isNullOrZero()) {
            Log.e(TAG, "Unable to start ${FeatureSettingsActivity::class.simpleName} due to " +
                    "missing argument(s). " +
                    "$EXTRA_KEY_PREFERENCE_XML_RESOURCE_ID: $preferenceXmlResourceId, " +
                    "$EXTRA_KEY_IMAGE_RESOURCE_ID: $imageResourceId")
            finish()
            return
        }

        findViewById<ImageView>(R.id.hero_image).setImageResource(imageResourceId)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_holder,
                        FixedPreferenceFragment.newInstance(preferenceXmlResourceId))
                .commit()
    }
}

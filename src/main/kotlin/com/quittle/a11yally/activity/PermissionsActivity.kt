package com.quittle.a11yally.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.quittle.a11yally.BuildConfig
import com.quittle.a11yally.PermissionsManager
import com.quittle.a11yally.R
import com.quittle.a11yally.ifElse
import com.quittle.a11yally.preferences.withPreferenceProvider

/**
 * Shows the current status of permissions necessary for the app to run effectively
 */
class PermissionsActivity : FixedContentActivity() {
    override val layoutId = R.layout.permissions_activity

    private val mPermissionsManager by lazy { PermissionsManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        withPreferenceProvider(this) {
            if (getShowTutorial()) {
                startActivity(Intent(this@PermissionsActivity, WelcomeActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()

        updateViewsStatuses()

        // When granting overlay permissions, it can take some time between leaving the settings
        // activity (which is when it is updated) and the app getting the permissions. If the user
        // switches back via recents, the activity will likely get the update in time. If they use
        // the back button, however, they are very unlikely to get the permission by the time
        // onResume has been called so to handle this race condition, periodically update the views
        @Suppress("MagicNumber")
        Handler().run {
            val initialDelayMs = 500L
            postDelayed(this@PermissionsActivity::updateViewsStatuses, initialDelayMs)
            postDelayed(this@PermissionsActivity::updateViewsStatuses, initialDelayMs * 2)
            postDelayed(this@PermissionsActivity::updateViewsStatuses, initialDelayMs * 4)
        }
    }

    private fun updateViewsStatuses() {
        updateStatus(
                mPermissionsManager.hasDrawOverlaysPermission(),
                R.id.permission_overlay_wrapper,
                R.id.permission_overlay_image,
                R.id.permission_overlay_status,
                this::onClickFixOverlay)
        updateStatus(
                mPermissionsManager.hasAccessibilityServicePermission(),
                R.id.permission_service_wrapper,
                R.id.permission_service_image,
                R.id.permission_service_status,
                this::onClickFixService)

        findViewById<View>(R.id.continue_button).run {
            isEnabled = mPermissionsManager.hasAllPermissions()
            setOnClickListener {
                startActivity(Intent(this@PermissionsActivity, MainActivity::class.java))
            }
        }
    }

    private fun onClickFixOverlay() {
        if (!mPermissionsManager.hasDrawOverlaysPermission()) {
            startActivityIntent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, true)
        } else {
            updateViewsStatuses()
        }
    }

    private fun onClickFixService() {
        if (!mPermissionsManager.hasAccessibilityServicePermission()) {
            try {
                startActivityIntent(Settings.ACTION_ACCESSIBILITY_SETTINGS, true)
            } catch (e: ActivityNotFoundException) {
                try {
                    startActivityIntent(Settings.ACTION_ACCESSIBILITY_SETTINGS, false)
                } catch (e: ActivityNotFoundException) {
                    Log.w(BuildConfig.TAG, "Unable to show accessibility settings")
                }
            }
        } else {
            updateViewsStatuses()
        }
    }

    private fun updateStatus(
        hasPermission: Boolean,
        wrapperViewId: Int,
        imageViewId: Int,
        statusViewId: Int,
        onClickCallback: () -> Unit
    ) {
        val onClickListener: View.OnClickListener? = hasPermission.ifElse(
                null,
                object : View.OnClickListener {
                    override fun onClick(view: View?) {
                        onClickCallback()
                    }
                })

        findViewById<View>(wrapperViewId).run {
            setOnClickListener(onClickListener)
            isEnabled = !hasPermission
            setBackgroundResource(hasPermission.ifElse(
                    R.color.primary_action_disabled_background, R.color.primary_action_background))
        }

        findViewById<ImageView>(imageViewId).run {
            setImageResource(
                    hasPermission.ifElse(
                            R.drawable.service_status_enabled_icon,
                            R.drawable.warning_icon))
        }

        findViewById<TextView>(statusViewId).run {
            setText(hasPermission.ifElse(
                    R.string.permissions_activity_status_ok,
                    R.string.permissions_activity_status_fix))
            setTextColor(ContextCompat.getColor(this@PermissionsActivity,
                    hasPermission.ifElse(
                            R.color.primary_action_disabled_text,
                            R.color.primary_action_text)))
        }
    }

    /**
     * Sends a start activity intent.
     * @param action The action of the intent
     * @param targetPackage If true, specifies the A11y Ally package as the uri of the intent
     */
    private fun startActivityIntent(action: String, targetPackage: Boolean) {
        val uri = targetPackage.ifElse(Uri.parse("package:$packageName"), null)
        val intent = Intent(action, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}

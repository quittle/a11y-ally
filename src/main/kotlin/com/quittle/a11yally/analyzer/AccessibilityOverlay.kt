package com.quittle.a11yally.analyzer

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.quittle.a11yally.BuildConfig.TAG
import com.quittle.a11yally.ifNotNull
import com.quittle.a11yally.preferences.PreferenceProvider

/**
 * Base class for displaying an overlay on top of the window's content.
 */
abstract class AccessibilityOverlay<T : ViewGroup>(
    accessibilityAnalyzer: A11yAllyAccessibilityAnalyzer
) :
            AccessibilityItemEventListener {
    private companion object {
        private const val PIXEL_FORMAT: Int = PixelFormat.TRANSLUCENT

        private val sOverlayType: Int =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    @Suppress("deprecation")
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
                } else {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                }
    }

    private var mRootView: T? = null
    private val mWindowManager =
            accessibilityAnalyzer.applicationContext
                    .getSystemService(AccessibilityService.WINDOW_SERVICE) as WindowManager
    private val mPreferenceProvider: PreferenceProvider
    private val mServiceEnabledLiveData: LiveData<Boolean>

    protected val rootView get() = mRootView

    /**
     * This should be a bitmask of [WindowManager.LayoutParams]
     */
    protected abstract val mOverlayFlags: Int

    /**
     * Provides the root view that should be attached to the window. This may be called multiple
     * times during the lifecycle of the overlay
     */
    protected abstract fun buildRootView(): T

    private val mWindowLayoutParams by lazy {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                sOverlayType,
                mOverlayFlags,
                PIXEL_FORMAT) }

    init {
        mPreferenceProvider = PreferenceProvider(accessibilityAnalyzer)
        mServiceEnabledLiveData = mPreferenceProvider.getServiceEnabledLiveData()
        mServiceEnabledLiveData.observe(accessibilityAnalyzer, Observer { enabled ->
            if (!enabled) {
                accessibilityAnalyzer.pauseListener(this)
            }
        })
    }

    override fun onResume() {
        mRootView = buildRootView()

        mWindowLayoutParams.verticalMargin = 0f

        try {
            mWindowManager.addView(mRootView, mWindowLayoutParams)
        } catch (e: WindowManager.BadTokenException) {
            Log.e(TAG, "Unable to add overlay view to window manager", e)
        }
    }

    override fun onPause() {
        mRootView.ifNotNull {
            try {
                // It is possible for a race-condition to occur where the drawView isn't null but
                // is not attached to the window. In such cases, removing the view that is not
                // attached throws an exception.
                mWindowManager.removeView(mRootView)
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Unable to remove view from window", e)
            }
            mRootView = null
        }
    }
}

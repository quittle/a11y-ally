package com.quittle.a11yally.analyzer

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.quittle.a11yally.BuildConfig.TAG
import com.quittle.a11yally.lifecycle.LifecycleAccessibilityService

/**
 * Generic base service class for analyzing the accessibility events.
 */
abstract class AccessibilityAnalyzer : LifecycleAccessibilityService() {
    /**
     * Holds the state of if the analyzer is paused due to focus being on a non-whitelisted app
     */
    private var mIsPaused = false

    private val mPausedListeners: MutableSet<AccessibilityItemEventListener> = mutableSetOf()

    private val activeListeners: Collection<AccessibilityItemEventListener>
        get() = listeners.minus(mPausedListeners)

    /**
     * In order to support [LinearNavigationAccessibilityOverlay], accessibility nodes cannot be
     * recycled immediately. Failing to recycle them can lead to a memory leak.
     */
    private val mCachedNodes: MutableCollection<AccessibilityNodeInfo> = mutableListOf()

    fun pauseListener(listener: AccessibilityItemEventListener) {
        if (mPausedListeners.add(listener)) {
            listener.onPause()
        }
    }

    fun resumeListener(listener: AccessibilityItemEventListener) {
        if (mPausedListeners.remove(listener)) {
            if (!mIsPaused) {
                listener.onResume()
            }
        }
    }

    /**
     * Subclasses must provide the listeners registered. This cannot be composed because the way
     * Android services work. They are started by an intent and can only be communicated via IPC.
     * The only alternative would be if the instances were serialized in some way, which is much
     * more complex and dangerous than using an inheritance-based method.
     */
    protected abstract val listeners: Collection<AccessibilityItemEventListener>

    /**
     * Returns the whitelist of apps to analyze. This is used, rather than using
     * {@link #setServiceInfo} to support non-whitelisted app accessibility events. This is a method
     * because it is evaluated on every event to enable dynamic updates, e.g. via preferences.
     * @return The list of app package names to analyze, e.g. {@code com.quittle.a11yally} or null
     *         to analyze all packages.
     */
    protected abstract fun getAppWhitelist(): Iterable<String>?

    protected fun pauseListeners() {
        activeListeners.forEach(AccessibilityItemEventListener::onPause)
    }

    protected fun resumeListeners() {
        activeListeners.forEach(AccessibilityItemEventListener::onResume)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Clear recycle nodes when events start
        mCachedNodes.forEach {
            try {
                it.recycle()
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Unable to recycle node", e)
            }
        }
        mCachedNodes.clear()

        val whitelist = getAppWhitelist()
        if (whitelist !== null && !whitelist.contains(rootInActiveWindow?.packageName)) {
            activeListeners.forEach(AccessibilityItemEventListener::onNonWhitelistedApp)
            if (!mIsPaused) {
                mIsPaused = true
                pauseListeners()
            }
            // Return early if not whitelisted
            return
        }
        if (mIsPaused) {
            mIsPaused = false
            resumeListeners()
        }

        activeListeners.forEach(AccessibilityItemEventListener::onAccessibilityEventStart)

        var rootNode: AccessibilityNodeInfo? = null
        try {
            // rootInActiveWindow may itself be null
            rootNode = rootInActiveWindow
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Unable to get rootInActiveWindow", e)
        }
        rootNode?.let {
            iterateAccessibilityNodeInfos(it, this::onNodeEvent)
            mCachedNodes.add(it)
        }
        activeListeners.forEach(AccessibilityItemEventListener::onAccessibilityEventEnd)
    }

    override fun onInterrupt() {}

    private fun iterateAccessibilityNodeInfos(
        root: AccessibilityNodeInfo,
        onEachCallback: (AccessibilityNodeInfo) -> Unit
    ) {
        onEachCallback(root)
        for (i in 0 until root.childCount) {
            root.getChild(i)?.let {
                iterateAccessibilityNodeInfos(it, onEachCallback)
                mCachedNodes.add(it)
            }
        }
    }

    private fun onNodeEvent(node: AccessibilityNodeInfo) {
        listeners.forEach { listener: AccessibilityItemEventListener ->
            listener.onAccessibilityNodeInfo(node)
        }
    }
}

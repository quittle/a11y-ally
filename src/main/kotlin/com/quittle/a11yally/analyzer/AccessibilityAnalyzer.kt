package com.quittle.a11yally.analyzer

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.quittle.a11yally.BuildConfig.TAG

/**
 * Generic base service class for analyzing the accessibility events.
 */
abstract class AccessibilityAnalyzer : AccessibilityService() {
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
        listeners.forEach(AccessibilityItemEventListener::onPause)
    }

    protected fun resumeListeners() {
        listeners.forEach(AccessibilityItemEventListener::onResume)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val whitelist = getAppWhitelist()
        if (whitelist !== null && !whitelist.contains(rootInActiveWindow?.packageName)) {
            listeners.forEach(AccessibilityItemEventListener::onNonWhitelistedApp)
            // Return early if not whitelisted
            return
        }

        listeners.forEach(AccessibilityItemEventListener::onAccessibilityEventStart)

        var rootNode: AccessibilityNodeInfo? = null
        try {
            // rootInActiveWindow may itself be null
            rootNode = rootInActiveWindow
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Unable to get rootInActiveWindow", e)
        }
        rootNode?.let {
            iterateAccessibilityNodeInfos(it, this::onNodeEvent)
            it.recycle()
        }
        listeners.forEach(AccessibilityItemEventListener::onAccessibilityEventEnd)
    }

    override fun onInterrupt() {}

    private fun iterateAccessibilityNodeInfos(root: AccessibilityNodeInfo,
                                              onEachCallback: (AccessibilityNodeInfo) -> Unit) {
        onEachCallback(root)
        for (i in 0 until root.childCount) {
            root.getChild(i)?.let {
                iterateAccessibilityNodeInfos(it, onEachCallback)
                try {
                    it.recycle()
                } catch (e: IllegalStateException) {
                    Log.w(TAG, "Unable to recycle node", e)
                }
            }
        }
    }

    private fun onNodeEvent(node: AccessibilityNodeInfo) {
        listeners.forEach { listener: AccessibilityItemEventListener ->
            listener.onAccessibilityNodeInfo(node)
        }
    }
}

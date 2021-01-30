package com.quittle.a11yally.analyzer

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Callback class to consume the accessibility events.
 */
interface AccessibilityItemEventListener {
    /**
     * Called on each accessibility node of the current event.
     * @param node The current node being walked
     */
    fun onAccessibilityNodeInfo(node: AccessibilityNodeInfo)

    /**
     * Called when an accessibility event happens, before any calls to
     * {@link onAccessibilityNodeInfo}.
     */
    fun onAccessibilityEventStart()

    /**
     * Called when an accessibility event happens, after all calls to
     * {@link onAccessibilityNodeInfo}.
     */
    fun onAccessibilityEventEnd()

    /**
     * Called when a non-whitelisted, accessibility event occurred. Can be leveraged to clear
     * state when leaving an app of interest.
     */
    fun onNonWhitelistedApp()

    /**
     * Called when the listener should pause what it is doing, hide any visual artifacts and
     * stop providing output or doing work. No more events will be called until after
     * {@link #onResume()} is called.
     */
    fun onPause()

    /**
     * Called when the listener should resume what it is doing, perform setup and be ready for
     * events. This is called only after {@link #onPause()}.
     */
    fun onResume()
}

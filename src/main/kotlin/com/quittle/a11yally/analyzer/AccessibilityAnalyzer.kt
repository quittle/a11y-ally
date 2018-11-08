package com.quittle.a11yally.analyzer

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Generic base service class for analyzing the accessibility events.
 */
abstract class AccessibilityAnalyzer : AccessibilityService() {
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
    }

    /**
     * Subclasses must provide the listeners registered. This cannot be composed because the way
     * Android services work. They are started by an intent and can only be communicated via IPC.
     * The only alternative would be if the instances were serialized in some way, which is much
     * more complex and dangerous than using an inheritance-based method.
     */
    protected abstract val listeners: Collection<AccessibilityItemEventListener>

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        listeners.forEach(AccessibilityItemEventListener::onAccessibilityEventStart)
        rootInActiveWindow?.let {
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
                it.recycle()
            }
        }
    }

    private fun onNodeEvent(node: AccessibilityNodeInfo) {
        listeners.forEach { listener: AccessibilityItemEventListener ->
            listener.onAccessibilityNodeInfo(node)
        }
    }
}

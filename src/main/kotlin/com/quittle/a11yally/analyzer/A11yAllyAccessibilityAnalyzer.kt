package com.quittle.a11yally.analyzer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * App-specific implementation of an {@link AccessibilityAnalyzer}. It implements
 * {@link LifecycleOwner} for {@link AccessibilityOverlay} to consume its events for drawing, using
 * this service's context.
 */
class A11yAllyAccessibilityAnalyzer : AccessibilityAnalyzer(), LifecycleOwner {
    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.markState(Lifecycle.State.CREATED)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED)
    }

    /**
     * {@inheritDoc}
     *
     * This must be lazy because the constructor for {@link AccessibilityOverlay} must run after
     * {@link AccessibilityAnalyzer} is initialized fully.
     */
    override val listeners: Collection<AccessibilityItemEventListener> by lazy { setOf(
            AccessibilityItemLogger(),
            AccessibilityOverlay(this)
    ) }
}

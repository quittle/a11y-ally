package com.quittle.a11yally.lifecycle

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import androidx.annotation.CallSuper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher

/**
 * Forked from LifecycleService:
 * https://android.googlesource.com/platform/frameworks/support/+/a9ac247af2afd4115c3eb6d16c05bc92737d6305/lifecycle/service/src/main/java/androidx/lifecycle/LifecycleService.java
 */
abstract class LifecycleAccessibilityService : AccessibilityService(), LifecycleOwner {
    private val mDispatcher: ServiceLifecycleDispatcher = ServiceLifecycleDispatcher(this)

    @CallSuper
    override fun onCreate() {
        mDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
    }

    @Suppress("deprecation")
    @Deprecated("Deprecated in Java")
    @CallSuper
    override fun onStart(intent: Intent?, startId: Int) {
        mDispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    /**
     * This method is added only to annotate it with @CallSuper.
     * In usual service super.onStartCommand is no-op, but in LifecycleService
     * it results in mDispatcher.onServicePreSuperOnStart() call, because
     * super.onStartCommand calls onStart().
     */
    @CallSuper
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * This is a replacement for [onBind] because it's final in [AccessibilityService].
     */
    @CallSuper
    override fun onServiceConnected() {
        mDispatcher.onServicePreSuperOnBind()
        super.onServiceConnected()
    }

    @CallSuper
    override fun onDestroy() {
        mDispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override fun getLifecycle(): Lifecycle {
        return mDispatcher.lifecycle
    }
}

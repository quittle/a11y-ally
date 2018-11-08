package com.quittle.a11yally.observer

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * Interface to ensure safe consumption of {@link LifecycleObserver} events for a service.
 */
interface ServiceLifeCycleObserver : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onServiceAny() {}

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onServiceCreate()

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onServiceDestroy()
}

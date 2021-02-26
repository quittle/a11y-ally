package com.quittle.a11yally.base

import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.lang.ref.WeakReference

class RefreshableWeakReferenceTest {
    @Test
    fun testRefreshInitial() {
        val reference = RefreshableWeakReference(1, { 2 })
        assertEquals(reference.get(), 1)
    }

    @Test
    fun testRefreshingWorks() {
        val reference = RefreshableWeakReference(1, { 2 })
        val weakReference: WeakReference<*> =
            RefreshableWeakReference::class.java.getDeclaredField("reference").run {
                isAccessible = true
                get(reference) as WeakReference<*>
            }
        weakReference.clear()
        assertEquals(reference.get(), 2)
    }
}

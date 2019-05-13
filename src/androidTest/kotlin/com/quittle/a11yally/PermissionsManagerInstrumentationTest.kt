package com.quittle.a11yally

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class PermissionsManagerInstrumentationTest {
    private lateinit var mPermissionsManager: PermissionsManager

    @Before
    fun setUp() {
        mPermissionsManager =
                PermissionsManager(InstrumentationRegistry.getInstrumentation().targetContext)
        fullyTearDownPermissions()
    }

    @Test
    fun testOverlayPermission() {
        assertFalse(mPermissionsManager.hasDrawOverlaysPermission())

        grantPermissions(Manifest.permission.SYSTEM_ALERT_WINDOW)

        // Wait for the permission to propagate
        sleep(2000)

        assertTrue(mPermissionsManager.hasDrawOverlaysPermission())
        assertFalse(mPermissionsManager.hasAllPermissions())
    }

    @Test
    fun testAccessibilityServicePermission() {
        assertFalse(mPermissionsManager.hasAccessibilityServicePermission())

        enableAccessibilityService()

        assertTrue(mPermissionsManager.hasAccessibilityServicePermission())
        assertFalse(mPermissionsManager.hasAllPermissions())
    }

    @Test
    fun testAllPermissionsGranted() {
        assertFalse(mPermissionsManager.hasAllPermissions())

        grantPermissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
        enableAccessibilityService()

        // Wait for the permission to propagate
        sleep(2000)

        assertTrue(mPermissionsManager.hasAllPermissions())
    }
}

package com.quittle.a11yally

import android.Manifest
import android.os.Build
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.quittle.a11yally.activity.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class PermissionsManagerInstrumentationTest {
    private lateinit var mPermissionsManager: PermissionsManager

    /**
     * This is required to avoid the app potentially being on the `Display over other apps` settings
     * activity. If it is, then setting the permission will not work.
     */
    @get:Rule
    val mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        mPermissionsManager =
            PermissionsManager(InstrumentationRegistry.getInstrumentation().targetContext)
        fullyTearDownPermissions()
    }

    @Test
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.LOLLIPOP_MR1)
    fun testOverlayPermission_beforePermissionsExisted() {
        assertTrue(mPermissionsManager.hasDrawOverlaysPermission())
        assertFalse(mPermissionsManager.hasAllPermissions())
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.M)
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

        // Wait for the permission to propagate
        sleep(2000)

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

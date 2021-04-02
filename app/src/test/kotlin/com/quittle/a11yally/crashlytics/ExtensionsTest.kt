package com.quittle.a11yally.crashlytics

import com.quittle.a11yally.FirebaseRule
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExtensionsTest {
    @get:Rule
    var firebaseRule = FirebaseRule()

    @Test
    fun testCrashlytics() {
        val crashlyticsInstance = crashlytics
        assertNotNull(crashlyticsInstance)
    }

    @Test
    fun testRecordException() {
        // Ensures no exceptions but cannot actually validate the calls do what they are supposed to
        crashlytics.recordException("message")
        crashlytics.recordException("message", RuntimeException("nested"))
    }
}

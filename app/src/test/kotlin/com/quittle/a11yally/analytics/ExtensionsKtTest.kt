package com.quittle.a11yally.analytics

import android.os.Looper.getMainLooper
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ExtensionsKtTest {

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testFirebaseAnalytics() {
        val analytics = firebaseAnalytics
        assertNotNull(analytics)

        shadowOf(getMainLooper()).idle()
    }
}

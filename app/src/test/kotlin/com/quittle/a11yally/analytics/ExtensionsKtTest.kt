package com.quittle.a11yally.analytics

import android.os.Looper.getMainLooper
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.ktx.ParametersBuilder
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ExtensionsKtTest {

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        shadowOf(getMainLooper()).idle()
    }

    @Test
    fun testFirebaseAnalytics() {
        val analytics = firebaseAnalytics
        assertNotNull(analytics)

        shadowOf(getMainLooper()).idle()
    }

    @Test
    fun testParametersBuilderParam() {
        val key = "key"
        val value = "value"
        val builder = ParametersBuilder()

        assertNull(builder.bundle.get(key))

        builder.param(key, null)
        assertNull(builder.bundle.get(key), "Setting to null shouldn't do anything or throw")

        val cs: CharSequence = StringBuffer(value)
        builder.param(key, cs)
        assertEquals(value, builder.bundle.get(key), "Value should be set")

        builder.param(key, null)
        assertEquals(value, builder.bundle.get(key), "Value should not be cleared")
    }
}

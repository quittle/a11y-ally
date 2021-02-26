package com.quittle.a11yally.analytics

import android.os.Looper.getMainLooper
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.ktx.ParametersBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
        assertNull("Setting to null shouldn't do anything or throw", builder.bundle.get(key))

        val cs: CharSequence = StringBuffer(value)
        builder.param(key, cs)
        assertEquals("Value should be set", value, builder.bundle.get(key))

        builder.param(key, null)
        assertEquals("Value should not be cleared", value, builder.bundle.get(key))
    }
}

package com.quittle.a11yally.analytics

import com.google.firebase.analytics.ktx.ParametersBuilder
import com.quittle.a11yally.FirebaseRule
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExtensionsKtTest {
    @get:Rule
    val firebaseRule = FirebaseRule()

    @Test
    fun testFirebaseAnalytics() {
        val analytics = firebaseAnalytics
        assertNotNull(analytics)
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

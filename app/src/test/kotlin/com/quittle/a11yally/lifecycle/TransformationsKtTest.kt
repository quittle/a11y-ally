package com.quittle.a11yally.lifecycle

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class TransformationsKtTest {
    @Test
    fun testNegateLiveData() {
        val liveData = MutableLiveData(true)
        val negated = negateLiveData(liveData)
        val doubleNegated = negateLiveData(negated)

        // Observe so the values aren't null
        doubleNegated.observeForever {}

        assertTrue(liveData.value!!)
        assertFalse(negated.value!!)
        assertTrue(doubleNegated.value!!)

        liveData.value = false

        assertFalse(liveData.value!!)
        assertTrue(negated.value!!)
        assertFalse(doubleNegated.value!!)
    }
}

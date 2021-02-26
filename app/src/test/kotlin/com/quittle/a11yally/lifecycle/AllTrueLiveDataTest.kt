package com.quittle.a11yally.lifecycle

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class AllTrueLiveDataTest {

    @Test
    fun testNoProvidersDefaultsToFalse() {
        assertFalse(AllTrueLiveData().value!!)
    }

    @Test
    fun testSingleProviderUpdates() {
        val mutableLiveData = MutableLiveData(true)
        val allTrueLiveData = AllTrueLiveData(mutableLiveData)
        allTrueLiveData.observeForever {}
        assertTrue(allTrueLiveData.value!!)

        mutableLiveData.value = false
        assertFalse(allTrueLiveData.value!!)

        mutableLiveData.value = true
        assertTrue(allTrueLiveData.value!!)
    }

    @Test
    fun multiProviderUpdate() {
        val mutableLiveData1 = MutableLiveData(true)
        val mutableLiveData2 = MutableLiveData(true)
        val allTrueLiveData = AllTrueLiveData(mutableLiveData1, mutableLiveData2)
        allTrueLiveData.observeForever {}
        assertTrue(allTrueLiveData.value!!, "Starts true")

        mutableLiveData1.value = false
        assertFalse(allTrueLiveData.value!!, "Turns false")

        mutableLiveData2.value = true
        assertFalse(allTrueLiveData.value!!, "Stays false")

        mutableLiveData2.value = false
        mutableLiveData1.value = true
        assertFalse(allTrueLiveData.value!!, "Swapped, but still false")

        mutableLiveData2.value = true
        assertTrue(allTrueLiveData.value!!, "Finally true again")
    }
}

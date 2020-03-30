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
        assertTrue("Starts true", allTrueLiveData.value!!)

        mutableLiveData1.value = false
        assertFalse("Turns false", allTrueLiveData.value!!)

        mutableLiveData2.value = true
        assertFalse("Stays false", allTrueLiveData.value!!)

        mutableLiveData2.value = false
        mutableLiveData1.value = true
        assertFalse("Swapped, but still false", allTrueLiveData.value!!)

        mutableLiveData2.value = true
        assertTrue("Finally true again", allTrueLiveData.value!!)
    }
}

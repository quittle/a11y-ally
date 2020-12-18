package com.quittle.a11yally.base

import android.os.Looper.getMainLooper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.text.MatchesPattern.matchesPattern
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.lang.Thread.sleep
import java.util.regex.Pattern

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [ShadowLog::class])
class AndroidUtilsUnitTest {
    private companion object {
        const val TAG = "foo"
    }

    @Before
    fun setUp() {
        ShadowLog.setupLogging()
    }

    @Test
    fun testTime() {
        val ret = time(TAG, "runtime is %d milliseconds") {
            sleep(100)
            3
        }

        shadowOf(getMainLooper()).idle()

        assertEquals(3, ret)
        val logs = ShadowLog.getLogsForTag(TAG)
        assertEquals(logs.toString(), 1, logs.size)
        assertEquals(TAG, logs[0].tag)
        val message = logs[0].msg

        val pattern = Pattern.compile("^runtime is (\\d+) milliseconds$")
        assertThat(message, matchesPattern(pattern))
        val matcher = pattern.matcher(message)
        assertTrue(matcher.find())
        val duration = matcher.group(1)!!.toLong()
        assertThat(duration, greaterThanOrEqualTo(100))
    }
}

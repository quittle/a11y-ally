package com.quittle.a11yally

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.annotation.RawRes
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onIdle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quittle.a11yally.RecordingService.Companion.START_RECORDING_INTENT_ACTION
import com.quittle.a11yally.RecordingService.Companion.STOP_RECORDING_INTENT_ACTION
import com.quittle.a11yally.test.R as TestR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
@SuppressLint("ApplySharedPref")
class AccessibilityItemLoggerTest {
    private lateinit var targetContext: Context
    private lateinit var testContext: Context
    private lateinit var recordingFile: File

    @Before
    fun setUp() {
        targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        testContext = InstrumentationRegistry.getInstrumentation().context
        recordingFile = File(targetContext.filesDir, "recordings/recording.json")
        recordingFile.delete()
        clearSharedPreferences()
        fullySetUpPermissions()
    }

    @After
    fun tearDown() {
        recordingFile.delete()
        fullyTearDownPermissions()
        clearSharedPreferences()
    }

    @Test
    fun testRecordingEmpty() {
        assertFalse(recordingFile.exists())

        startRecording()
        stopRecording()

        waitForJsonArrayFile(recordingFile)

        assertEquals("[]", recordingFile.readText())
    }

    @Test
    @SuppressLint("RestrictedApi") // Setting preferences of another context is considered forbidden
    fun testRecordingUnfriendlyActivity() {
        assertFalse(recordingFile.exists())

        // Enable the necessary preferences
        PreferenceManager(targetContext).sharedPreferences.edit()
                .putBoolean(targetContext.getString(R.string.pref_service_enabled), true)
                .putBoolean(targetContext.getString(R.string.pref_highlight_issues), true)
                .putBoolean(targetContext.getString(R.string.pref_highlight_missing_labels), true)
                .putBoolean(
                        targetContext.getString(R.string.pref_highlight_small_touch_targets), true)
                .putBoolean(targetContext.getString(R.string.pref_enable_all_apps), false)
                .putStringSet(targetContext.getString(R.string.pref_enabled_apps),
                        setOf(targetContext.applicationInfo.packageName))
                .commit()

        sleep(100)
        onIdle()

        startRecording()

        openUnfriendlyActivity()

        // It is up to the OS to send accessibility events, which do not have a guaranteed
        // "frame rate". Wait until at least one should get triggered.
        sleep(1000)
        onIdle()

        stopRecording()

        waitForJsonArrayFile(recordingFile)

        var actualReport = JSONArray(recordingFile.readText())
        for (i in 0 until actualReport.length()) {
            val entry = actualReport[i] as JSONObject
            entry.remove("timestamp")
        }

        // Multiple may have been triggered but they should all be duplicates. De-dup them as it is
        // okay to receive multiple accessibility events.
        if (actualReport.length() == 6) {
            for (i in 0 until 3) {
                JSONAssert.assertEquals(actualReport.getJSONObject(i),
                                        actualReport.getJSONObject(i + 3),
                                        JSONCompareMode.STRICT)
            }
            actualReport = actualReport.range(0, 3)
        } else if (actualReport.length() == 9) {
            for (i in 0 until 3) {
                JSONAssert.assertEquals(actualReport.getJSONObject(i),
                                        actualReport.getJSONObject(i + 3),
                                        JSONCompareMode.STRICT)
                JSONAssert.assertEquals(actualReport.getJSONObject(i),
                                        actualReport.getJSONObject(i + 6),
                                        JSONCompareMode.STRICT)
            }
            actualReport = actualReport.range(0, 3)
        }

        val expectedReport = readReportToJSONArray(TestR.raw.unfriendly_activity_report)

        JSONAssert.assertEquals(
                actualReport.toString(4), expectedReport, actualReport, JSONCompareMode.STRICT)
    }

    private fun openUnfriendlyActivity(): UnfriendlyActivity {
        return InstrumentationRegistry.getInstrumentation()
                .startActivitySync(
                        Intent(targetContext, UnfriendlyActivity::class.java).apply {
                            flags += FLAG_ACTIVITY_NEW_TASK
                        }
                ) as UnfriendlyActivity
    }

    private fun readReportToJSONArray(@RawRes resourceId: Int): JSONArray {
        return JSONArray(testContext.resources
                .openRawResource(resourceId)
                .readBytes()
                .toString(Charsets.UTF_8))
    }

    private fun startRecording() {
        testContext.startService(Intent(
                START_RECORDING_INTENT_ACTION, null, targetContext, RecordingService::class.java))
        onIdle()
    }

    private fun stopRecording() {
        testContext.startService(Intent(
                STOP_RECORDING_INTENT_ACTION, null, targetContext, RecordingService::class.java))
        onIdle()
    }
}

/**
 * Helper function to extract a subset of the [JSONArray] as a new [JSONArray].
 */
private fun JSONArray.range(start: Int, end: Int): JSONArray {
    val ret = JSONArray()
    for (i in start until end) {
        ret.put(this[i])
    }
    return ret
}

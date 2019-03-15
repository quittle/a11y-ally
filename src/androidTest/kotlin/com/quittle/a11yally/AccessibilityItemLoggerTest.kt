package com.quittle.a11yally

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.annotation.XmlRes
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withText
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
    }

    @After
    fun tearDown() {
        recordingFile.delete()
    }

    @Test
    fun testRecordingEmpty() {
        assertFalse(recordingFile.exists())

        fullySetUpPermissions()

        startRecording()
        stopRecording()

        waitForFile(recordingFile)

        assertEquals("[]", recordingFile.readText())
    }

    @Test
    fun testRecordingUnfriendlyActivity() {
        assertFalse(recordingFile.exists())

        fullySetUpPermissions()
        PreferenceManager(targetContext).sharedPreferences.edit()
                .putBoolean(targetContext.getString(R.string.pref_service_enabled), true)
                .putBoolean(targetContext.getString(R.string.pref_highlight_issues), true)
                .putBoolean(targetContext.getString(R.string.pref_highlight_missing_labels), true)
                .putBoolean(
                        targetContext.getString(R.string.pref_highlight_small_touch_targets), true)
                .commit()

        startRecording()

        openUnfriendlyActivity()

        onView(withText("Explore"))

        // Allow time for the asynchronous accessibility service to send an event to the app
        sleep(1000)
        onIdle()

        stopRecording()

        waitForFile(recordingFile)
        val expectedReport =
                readReportToJSONArray(TestR.raw.unfriendly_activity_report)

        val actualReport = JSONArray(recordingFile.readText())
        for (i in 0 until actualReport.length()) {
            val entry = actualReport[i] as? JSONObject
            entry?.remove("timestamp")
        }

        JSONAssert.assertEquals(expectedReport, actualReport, JSONCompareMode.STRICT)
    }

    private fun openUnfriendlyActivity() {
        InstrumentationRegistry.getInstrumentation()
                .startActivitySync(
                        Intent(targetContext, UnfriendlyActivity::class.java).apply {
                            flags += FLAG_ACTIVITY_NEW_TASK
                        }
                )
    }

    private fun readReportToJSONArray(@XmlRes resourceId: Int): JSONArray {
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

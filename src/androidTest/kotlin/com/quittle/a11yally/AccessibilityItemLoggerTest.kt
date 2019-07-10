package com.quittle.a11yally

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.preference.PreferenceManager
import androidx.test.espresso.Espresso.onIdle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.quittle.a11yally.RecordingService.Companion.START_RECORDING_INTENT_ACTION
import com.quittle.a11yally.RecordingService.Companion.STOP_RECORDING_INTENT_ACTION
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.lang.Thread.sleep
import com.quittle.a11yally.test.R as TestR

class AccessibilityTestActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(TestR.layout.accessibility_test_activity)
    }
}

data class AccessibilityOutput(
    val boundsInScreen: String,
    val nodeClassPath: List<String>,
    val packageName: String,
    val text: String?,
    val issues: List<String>,
    @JsonIgnore
    val timestamp: Int
)

@RunWith(AndroidJUnit4::class)
@SuppressLint("ApplySharedPref")
class AccessibilityItemLoggerTest {
    private lateinit var targetContext: Context
    private lateinit var testContext: Context
    private lateinit var recordingFile: File

    private val outputReader: ObjectReader =
            jacksonObjectMapper().readerFor(object : TypeReference<Set<AccessibilityOutput>>() {})

    @get:Rule
    val mPermissionsRule = PermissionsRule()

    @Before
    fun setUp() {
        targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        testContext = InstrumentationRegistry.getInstrumentation().context
        recordingFile = File(targetContext.filesDir, "recordings/recording.json")
        recordingFile.delete()
        clearSharedPreferences()
    }

    @After
    fun tearDown() {
        recordingFile.delete()
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
                        setOf(testContext.applicationInfo.packageName))
                .putString(targetContext.getString(R.string.pref_small_touch_target_size), "40")
                .commit()

        startRecording()
        testContext.startActivity(
                Intent(testContext, AccessibilityTestActivity::class.java).apply {
                    flags += FLAG_ACTIVITY_NEW_TASK
                })

        // It is up to the OS to send accessibility events, which do not have a guaranteed
        // "frame rate". Wait until at least one should get triggered.
        sleep(2000)

        stopRecording()

        waitForJsonArrayFile(recordingFile)

        val contents: Set<AccessibilityOutput> = outputReader.readValue(recordingFile)

        val expectedReport: Set<AccessibilityOutput> = outputReader.readValue(testContext.resources
                .openRawResource(TestR.raw.unfriendly_activity_report))

        assertEquals(expectedReport, contents)
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

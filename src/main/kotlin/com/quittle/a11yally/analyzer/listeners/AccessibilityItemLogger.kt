package com.quittle.a11yally.analyzer.listeners

import android.content.Context
import android.util.Log
import com.quittle.a11yally.BuildConfig
import com.quittle.a11yally.analyzer.AccessibilityIssue
import com.quittle.a11yally.analyzer.AccessibilityIssueListener

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Logs accessibility errors to Logcat and to a recordings file.
 */
class AccessibilityItemLogger(context: Context) : AccessibilityIssueListener {
    private companion object {
        private const val TAG = "${BuildConfig.TAG}::Report"
        private const val RECORDINGS_DIR = "recordings"
        private const val RECORDING_FILE = "recording.json"
        private const val JSON_INDENTATION = 4
    }

    private var mIsRecording = false
    private val mContext = context
    private val mOngoingRecording = mutableListOf<Map<String, Any>>()

    override fun onIssues(issues: Collection<AccessibilityIssue>) {
        if (!mIsRecording || issues.isEmpty()) {
            return
        }

        issues.forEach { issue ->
            val summaryMap = issue.info +
                    mapOf(
                            "boundsInScreen" to issue.area.toShortString(),
                            "issues" to listOf(issue.type.toString()),
                            "timestamp" to System.currentTimeMillis())
            mOngoingRecording.add(summaryMap)
            val nodeSummary = JSONObject(summaryMap).toString(JSON_INDENTATION)
            Log.i(TAG, "Issue found: $nodeSummary")
        }
    }

    override fun onInvalidateIssues() {
        // Nothing to do here
    }

    fun startRecording() {
        if (mIsRecording) {
            Log.w(TAG, "Restarting recording. Dropping ${mOngoingRecording.size} existing entries")
            mOngoingRecording.clear()
        }
        mIsRecording = true
    }

    fun stopRecording() {
        if (!mIsRecording) {
            Log.w(TAG, "Attempting to stop recording when it was not previously started")
            return
        }

        val recording = JSONArray(mOngoingRecording).toString(JSON_INDENTATION)
        val recordingFile = getRecordingFile()
        recordingFile.writeText(recording, Charsets.UTF_8)

        mIsRecording = false
        mOngoingRecording.clear()

        Log.i(TAG, "Recording successfully saved to ${recordingFile.absolutePath}")
    }

    private fun getRecordingFile(): File {
        val recordingsDir = File(mContext.filesDir, RECORDINGS_DIR)
        recordingsDir.mkdirs()
        return File(recordingsDir, RECORDING_FILE)
    }
}

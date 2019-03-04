package com.quittle.a11yally.analyzer

import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Logs accessibility errors to Logcat.
 */
class AccessibilityItemLogger(context: Context) :
        AccessibilityAnalyzer.AccessibilityItemEventListener {
    private companion object {
        private const val TAG = "${com.quittle.a11yally.BuildConfig.TAG}::Report"
        private const val RECORDINGS_DIR = "recordings"
        private const val RECORDING_FILE = "recording.json"
    }

    private var mIsRecording = false
    private val mContext = context
    private val mAccessibilityNodeAnalyzer = AccessibilityNodeAnalyzer(context)
    private val mOngoingRecording = mutableListOf<Map<String, Any>>()

    override fun onPause() {}

    override fun onResume() {}

    override fun onAccessibilityEventStart() {}

    override fun onAccessibilityEventEnd() {}

    override fun onAccessibilityNodeInfo(node: AccessibilityNodeInfo) {
        if (!mIsRecording) {
            return
        }

        if (mAccessibilityNodeAnalyzer.isUnlabeledNode(node)) {
            val summaryMap = AccessibilityNodeSummary(node).getSummary().toMutableMap()
            summaryMap["timestamp"] = System.currentTimeMillis()
            mOngoingRecording.add(summaryMap)
            val nodeSummary = JSONObject(summaryMap).toString(4)
            Log.i(TAG, "Unlabeled node found: $nodeSummary")
        }
    }

    override fun onNonWhitelistedApp() {}

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

        val recording = JSONArray(mOngoingRecording).toString(4)
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

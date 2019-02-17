package com.quittle.a11yally

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.quittle.a11yally.BuildConfig.TAG
import com.quittle.a11yally.analyzer.A11yAllyAccessibilityAnalyzer

class RecordingService : IntentService(RecordingService::javaClass.name) {
    companion object {
        const val START_RECORDING_INTENT_ACTION = "com.quittle.a11yally.START_RECORDING"
        const val STOP_RECORDING_INTENT_ACTION = "com.quittle.a11yally.STOP_RECORDING"

        private const val START_RECORDING_FAILED_MESSAGE =
                "Unable to start recording as the accessibility service is not running. You may " +
                        "have to start the app and grant it permissions before you can record"
        private const val STOP_RECORDING_FAILED_MESSAGE =
                "Unable to stop recording as the accessibility service is not running. You may " +
                        "have to start the app and grant it permissions before you can record"
    }

    override fun onHandleIntent(intent: Intent?) {
        val action = intent?.action
        when (action) {
            START_RECORDING_INTENT_ACTION -> startRecording()
            STOP_RECORDING_INTENT_ACTION -> stopRecording()
            else -> Log.w(TAG, "Unsupported action received: $action")
        }
        stopSelf()
    }

    private fun startRecording() {
        runServiceOrLog(
                A11yAllyAccessibilityAnalyzer::startRecording, START_RECORDING_FAILED_MESSAGE)
    }

    private fun stopRecording() {
        runServiceOrLog(A11yAllyAccessibilityAnalyzer::stopRecording, STOP_RECORDING_FAILED_MESSAGE)
    }

    /**
     * Runs [func] on the analyzer instance or logs the [errorMessage] if the analyzer isn't running
     * @param func The function to call on the analyzer service
     * @param errorMessage The message print if the service wasn't running
     */
    private fun runServiceOrLog(func: Function1<A11yAllyAccessibilityAnalyzer, Unit>,
                                errorMessage: String) {
        val serviceInstance = A11yAllyAccessibilityAnalyzer.getInstance()

        if (serviceInstance.isNotNull()) {
            func(serviceInstance)
        } else {
            Log.w(TAG, errorMessage)
        }
    }
}

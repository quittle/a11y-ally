package com.quittle.a11yally.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.quittle.a11yally.isNotNull

fun startScreenRecordingActivity(context: Context) {
    context.startActivity(Intent(context, ScreenRecordingActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    })
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ScreenRecordingActivity : AppCompatActivity() {
    companion object {
        var sScreenRecordingCode: Int? = null
        var sScreenRecordingIntent: Intent? = null

        fun hasPermission(): Boolean {
            return sScreenRecordingCode.isNotNull() && sScreenRecordingIntent.isNotNull()
        }

        private const val REQUEST_CODE = 123
        private const val TAG = "ScreenRecordingActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mediaProjectionManager =
                ContextCompat.getSystemService(this, MediaProjectionManager::class.java)
        val intent = mediaProjectionManager?.createScreenCaptureIntent()
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE) {
            Log.w(TAG, "Received unexpected request code: $requestCode")
            finish()
            return
        }

        if (resultCode != Activity.RESULT_OK) {
            Log.w(TAG, "Received non-okay response for recording: $resultCode")
            finish()
            return
        }

        sScreenRecordingCode = resultCode
        sScreenRecordingIntent = data

        finish()
    }
}

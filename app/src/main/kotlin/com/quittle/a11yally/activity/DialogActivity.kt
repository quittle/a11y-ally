package com.quittle.a11yally.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.quittle.a11yally.BuildConfig.TAG
import com.quittle.a11yally.R
import com.quittle.a11yally.base.isNull

/**
 * Helper class for displaying an AlertDialog from anywhere. Use [DialogActivity.show] to display
 * it.
 */
class DialogActivity : FixedContentActivity() {
    companion object {
        fun show(context: Context, @StringRes messageResId: Int) {
            context.startActivity(
                Intent(context, DialogActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(EXTRA_KEY_MESSAGE_ID, messageResId)
                }
            )
        }

        private const val EXTRA_KEY_MESSAGE_ID = "message_res_id"
    }

    override val layoutId = R.layout.empty

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val messageResId = intent.extras?.getInt(EXTRA_KEY_MESSAGE_ID)
        if (messageResId.isNull()) {
            Log.e(TAG, "Unable to show DialogActivity without $EXTRA_KEY_MESSAGE_ID extras key")
            finish()
            return
        }

        AlertDialog.Builder(this)
            .setMessage(messageResId)
            .setOnDismissListener {
                finish()
            }
            .show()
    }
}

package com.quittle.a11yally.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.quittle.a11yally.R
import com.quittle.a11yally.activity.welcome.Welcome2Activity
import com.quittle.a11yally.preferences.withPreferenceProvider

class LearnMoreActivity : FixedContentActivity() {
    override val layoutId = R.layout.learn_more_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val getStartedButton = findViewById<View>(R.id.get_started)
        withPreferenceProvider(this) {
            if (getShowTutorial()) {
                getStartedButton.setOnClickListener {
                    startActivity(
                        Intent(this@LearnMoreActivity, Welcome2Activity::class.java).apply {
                            putExtra(Welcome2Activity.INTENT_LAUNCH_IN_LIST_VIEW, true)
                        }
                    )
                }
            } else {
                getStartedButton.visibility = View.GONE
            }
        }
    }
}

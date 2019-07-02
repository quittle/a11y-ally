package com.quittle.a11yally.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.quittle.a11yally.R
import com.quittle.a11yally.preferences.withPreferenceProvider

class LearnMoreActivity : FixedContentActivity() {
    override val layoutId = R.layout.learn_more_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<View>(R.id.get_started).setOnClickListener {
            withPreferenceProvider(this) { setShowTutorial(false) }

            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}

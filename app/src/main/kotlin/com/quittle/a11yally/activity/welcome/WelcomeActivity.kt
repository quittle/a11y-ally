package com.quittle.a11yally.activity.welcome

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.quittle.a11yally.R
import com.quittle.a11yally.activity.FixedContentActivity
import com.quittle.a11yally.activity.LearnMoreActivity
import com.quittle.a11yally.activity.MainActivity
import com.quittle.a11yally.preferences.withPreferenceProvider

class WelcomeActivity : FixedContentActivity() {
    override val layoutId = R.layout.welcome_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<View>(R.id.get_started).setOnClickListener {
            withPreferenceProvider(this) { setShowTutorial(false) }
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<View>(R.id.learn_more).setOnClickListener {
            startActivity(Intent(this, LearnMoreActivity::class.java))
        }
    }
}

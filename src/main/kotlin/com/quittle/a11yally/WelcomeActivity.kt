package com.quittle.a11yally

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.quittle.a11yally.preferences.withPreferenceProvider
import com.quittle.a11yally.view.FixedContentActivity

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

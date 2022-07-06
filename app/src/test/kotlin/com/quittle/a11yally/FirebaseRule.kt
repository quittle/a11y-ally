package com.quittle.a11yally

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.robolectric.Shadows

class FirebaseRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                base.evaluate()
                Shadows.shadowOf(Looper.getMainLooper()).idle()
            }
        }
}

package com.quittle.a11yally

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.IOException

/**
 * Disables animations during tests
 */
class DisableAnimationsRule : TestRule {
    private companion object {
        class DisableAnimtationsRuleStatement(private val mBase: Statement) : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                disableAnimations()
                try {
                    mBase.evaluate()
                } finally {
                    enableAnimations()
                }
            }

            @Throws(IOException::class)
            private fun disableAnimations() {
                runShellCommand("settings put global transition_animation_scale 0")
                runShellCommand("settings put global window_animation_scale 0")
                runShellCommand("settings put global animator_duration_scale 0")
            }

            @Throws(IOException::class)
            private fun enableAnimations() {
                runShellCommand("settings put global transition_animation_scale 1")
                runShellCommand("settings put global window_animation_scale 1")
                runShellCommand("settings put global animator_duration_scale 1")
            }
        }
    }

    override fun apply(base: Statement, description: Description): Statement {
        return DisableAnimtationsRuleStatement(base)
    }
}

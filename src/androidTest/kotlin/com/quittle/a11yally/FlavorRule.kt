package com.quittle.a11yally

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Enables tests if the build flavor is in [mFlavors]
 * @param mFlavors The flavors the tests should run on
 */
class FlavorRule(vararg private val mFlavors: String) : TestRule {
    private companion object {
        class FlavorRuleStatement(private val mBase: Statement,
                                  private val mFlavors: Array<out String>) : Statement() {
            override fun evaluate() {
                if (mFlavors.contains(BuildConfig.FLAVOR)) {
                    mBase.evaluate()
                }
            }
        }
    }

    override fun apply(base: Statement, description: Description?): Statement {
        return FlavorRuleStatement(base, mFlavors)
    }
}

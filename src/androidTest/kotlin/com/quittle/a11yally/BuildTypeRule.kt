package com.quittle.a11yally

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Enables tests if the build type is in [mBuildTypes]
 * @param mBuildTypes The build types the tests should run on
 */
class BuildTypeRule(private vararg val mBuildTypes: String) : TestRule {
    private companion object {
        class BuildTypeRuleStatement(
            private val mBase: Statement,
            private val mBuildTypes: Array<out String>
        ) : Statement() {
            override fun evaluate() {
                if (mBuildTypes.contains(BuildConfig.BUILD_TYPE)) {
                    mBase.evaluate()
                }
            }
        }
    }

    override fun apply(base: Statement, description: Description?): Statement {
        return BuildTypeRuleStatement(base, mBuildTypes)
    }
}

package com.quittle.a11yally.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API

/*
 * The list of issues that will be checked when running <code>lint</code>.
 */
@Suppress("UnstableApiUsage")
class A11yAllyIssueRegistry : IssueRegistry() {
    override val issues = listOf(MixingColorsAndThemeAttributes.ISSUE)

    override val api = CURRENT_API
}

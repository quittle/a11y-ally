@file:Suppress("MatchingDeclarationName")

package com.quittle.a11yally

import android.view.View
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.any
import org.hamcrest.Matchers.equalTo
import org.hamcrest.TypeSafeMatcher

fun withWidthAndHeight(expectedWidth: Int, expectedHeight: Int) =
    ViewSizeMatcher(equalTo(expectedWidth), equalTo(expectedHeight))
fun withWidthAndHeight(widthMatcher: Matcher<Int>, heightMatcher: Matcher<Int>) =
    ViewSizeMatcher(widthMatcher, heightMatcher)
fun withWidth(expectedWidth: Int) = ViewSizeMatcher(equalTo(expectedWidth), any(Int::class.java))
fun withHeight(expectedHeight: Int) = ViewSizeMatcher(any(Int::class.java), equalTo(expectedHeight))
fun withHeight(matcher: Matcher<Int>) = ViewSizeMatcher(any(Int::class.java), matcher)

/** A matcher for the dimensions of a View */
class ViewSizeMatcher(
    private val expectedWidth: Matcher<Int>,
    private val expectedHeight: Matcher<Int>
) : TypeSafeMatcher<View>(View::class.java) {
    override fun matchesSafely(target: View): Boolean {
        if (!expectedWidth.matches(target.width)) {
            return false
        }
        if (!expectedHeight.matches(target.height)) {
            return false
        }
        return true
    }

    override fun describeTo(description: Description) {
        description.appendText("with ViewSizeMatcher { width: ")
        description.appendDescriptionOf(expectedWidth)
        description.appendText(", height: ")
        description.appendDescriptionOf(expectedHeight)
        description.appendText(" }")
    }
}

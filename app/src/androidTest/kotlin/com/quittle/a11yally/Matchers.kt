@file:Suppress("MatchingDeclarationName")

package com.quittle.a11yally

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import kotlin.reflect.KClass

/**
 * Like hamcrest's built in `instanceOf`, but supports Kotlin's [KClass]es.
 * @param clazz The Kotlin class to match
 * @return A [Matcher] for the underlying java class.
 */
fun <T : Any> instanceOf(clazz: KClass<*>): Matcher<T> {
    return org.hamcrest.Matchers.instanceOf(clazz.java)
}

/**
 * Matches a number between [lowerBound] and [upperBound], exclusively.
 * @param lowerBound A value that must be lower than the actual value to be a match
 * @param upperBound A value that must be greater than the actual value to be a match
 * @return A [BetweenMatcher] using the provided bounds.
 */
fun <T> between(lowerBound: T, upperBound: T) where T : Number, T : Comparable<T> =
    BetweenMatcher(lowerBound, upperBound)

/**
 * Matches a number within exclusive bounds.
 */
class BetweenMatcher<T>(private val mLowerBound: T, private val mUpperBound: T) :
    TypeSafeMatcher<T>() where T : Number, T : Comparable<T> {
    override fun describeTo(description: Description) {
        description.appendText("is between ")
        description.appendValue(mLowerBound)
        description.appendText(" and ")
        description.appendValue(mUpperBound)
    }

    override fun matchesSafely(number: T): Boolean {
        return mLowerBound < number && number < mUpperBound
    }
}

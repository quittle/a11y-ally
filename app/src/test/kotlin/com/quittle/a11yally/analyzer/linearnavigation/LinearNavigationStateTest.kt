package com.quittle.a11yally.analyzer.linearnavigation

import android.view.accessibility.AccessibilityNodeInfo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LinearNavigationStateTest {
    @Mock lateinit var mockAccessibilityNodeInfo1: AccessibilityNodeInfo
    @Mock lateinit var mockAccessibilityNodeInfo2: AccessibilityNodeInfo
    @Mock lateinit var mockAccessibilityNodeInfo3: AccessibilityNodeInfo

    @Test
    fun testNext() {
        val state = LinearNavigationState(
            mutableListOf(
                LinearNavigationEntry("text 1", mockAccessibilityNodeInfo1),
                LinearNavigationEntry("text 2", mockAccessibilityNodeInfo2)
            ),
            mutableListOf(
                LinearNavigationEntry("text 3", mockAccessibilityNodeInfo3),
            ),
            LinearNavigationScrollOffset(1, 2)
        )

        val next = state.next(LinearNavigationScrollOffset(3, 4))

        assertThat(next.entries, empty())
        assertThat(
            next.prevEntries,
            equalTo(
                listOf(
                    LinearNavigationEntry("text 1", mockAccessibilityNodeInfo1),
                    LinearNavigationEntry("text 2", mockAccessibilityNodeInfo2)
                )
            )
        )
        assertThat(next.prevOffset, equalTo(LinearNavigationScrollOffset(3, 4)))
    }

    @Test
    fun testHaveEntriesDiverged() {
        val buildState = { curText: Array<String>, prevText: Array<String> ->
            LinearNavigationState(
                curText.map { text -> LinearNavigationEntry(text, mockAccessibilityNodeInfo1) }
                    .toMutableList(),
                prevText.map { text -> LinearNavigationEntry(text, mockAccessibilityNodeInfo1) }
                    .toMutableList(),
                LinearNavigationScrollOffset(0, 0)
            )
        }
        assertThat(
            "100% off",
            buildState(arrayOf("1"), arrayOf("2")).haveEntriesDiverged(), `is`(true)
        )
        assertThat(
            "100% off",
            buildState(arrayOf("1", "2"), arrayOf("3")).haveEntriesDiverged(), `is`(true)
        )
        assertThat(
            "empty cur",
            buildState(arrayOf(), arrayOf("1")).haveEntriesDiverged(), `is`(true)
        )
        assertThat(
            "empty prev",
            buildState(arrayOf("1"), arrayOf()).haveEntriesDiverged(), `is`(true)
        )
        assertThat(
            "both states empty",
            buildState(arrayOf(), arrayOf()).haveEntriesDiverged(), `is`(false)
        )
        assertThat(
            "50% match",
            buildState(arrayOf("1", "2"), arrayOf("1")).haveEntriesDiverged(), `is`(true)
        )
        assertThat(
            "67% match",
            buildState(arrayOf("1", "2", "3"), arrayOf("1", "2")).haveEntriesDiverged(), `is`(true)
        )
        assertThat(
            "75% match",
            buildState(arrayOf("1", "2", "3", "4"), arrayOf("1", "2", "3")).haveEntriesDiverged(),
            `is`(true)
        )
        assertThat(
            "80% match",
            buildState(
                arrayOf("1", "2", "3", "4", "5"), arrayOf("1", "2", "3", "4")
            ).haveEntriesDiverged(),
            `is`(false)
        )
        assertThat(
            "80% match, order does not matter",
            buildState(
                arrayOf("5", "1", "4", "3", "2"), arrayOf("1", "2", "3", "4")
            ).haveEntriesDiverged(),
            `is`(false)
        )
    }
}

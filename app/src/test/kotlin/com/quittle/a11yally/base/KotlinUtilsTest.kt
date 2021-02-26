package com.quittle.a11yally.base

import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Assert.fail
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.concurrent.thread

class KotlinUtilsTest {
    @Test
    fun compilerCheckTest() {
        val nullable: Double? = null
        var nonNull: Double = 100.0

        // The following would cause a compiler failure
        // nonNull = nullable

        if (!nullable.isNullOrZero()) {
            nonNull = nullable
        }

        // This should fail to compile if Kotlin fully implemented contracts as it should detect
        // that it is impossible for z to be null and may be 0. So if the if statement returned true
        // then it should risk throwing a division by zero exception
        val z: Int = if (Math.random() > 2) 0 else 1
        if (z.isNullOrZero()) {
            1 / z
        }

        if (!nullable.isNull()) {
            nonNull = nullable
        }

        if (nullable.isNotNull()) {
            nonNull = nullable
        }

        nullable.ifNotNull { value: Double ->
            nonNull = value
        }

        nonNull = nullable.orElse(nonNull)

        // Do something with the value to stop warnings
        nonNull.dec()
    }

    @Test
    fun isNullOrZero() {
        assertTrue(0.isNullOrZero())
        assertTrue(0.toDouble().isNullOrZero())
        assertFalse(1.isNullOrZero())
        assertFalse(1f.isNullOrZero())
        assertFalse((-100).isNullOrZero())
        assertFalse((-100f).isNullOrZero())

        assertTrue(null.isNullOrZero())
    }

    @Test
    fun isNull() {
        assertTrue(null.isNull())
        val value = null
        assertTrue(value.isNull())

        assertFalse(0.isNull())
        assertFalse(false.isNull())
        assertFalse("".isNull())
    }

    @Test
    fun isNotNull() {
        assertTrue(0.isNotNull())
        assertTrue(false.isNotNull())
        assertTrue("".isNotNull())

        assertFalse(null.isNotNull())
        val value = null
        assertFalse(value.isNotNull())
    }

    @Test
    fun ifNotNull() {
        null.ifNotNull { fail() }

        "value".ifNotNull { v -> assertEquals("value", v) }

        var value: String? = "value"
        var afterRan = false

        var testThread: Thread? = null
        value.ifNotNull {
            testThread = thread {
                Thread.sleep(1)
                assertTrue(afterRan)
                assertNull(value)
                assertNotNull(it)
            }
        }
        value = null
        afterRan = true
        testThread!!.join()
    }

    @Test
    fun testForEach() {
        var builder = ""
        forEach("a", "b", "c") {
            builder += it
        }
        assertEquals("abc", builder)
    }

    @Test
    fun `testOrElse literal`() {
        assertTrue(null.orElse(true))
        assertNull(null.orElse(null))
        assertTrue(true.orElse(false))
        assertFalse(false.orElse(true))
    }

    @Test
    fun `testOrElse generator not suspending`() {
        // Must be declared externally to avoid ambiguity
        // See: https://stackoverflow.com/a/60247582/1554990
        val rTrue = { true }
        val rFalse = { false }
        val rNull = { null }

        assertTrue(null.orElse(rTrue))
        assertNull(null.orElse(rNull))
        assertTrue(true.orElse(rFalse))
        assertFalse(false.orElse(rTrue))
    }

    @Test
    fun `testOrElse generator suspending`() {
        runBlocking {
            assertTrue(null.orElse(suspend { true }))
            assertNull(null.orElse(suspend { null }))
            assertTrue(true.orElse(suspend { false }))
            assertFalse(false.orElse(suspend { true }))
        }
    }

    @Test
    fun testIfElse() {
        val o1 = Object()
        val o2 = Object()
        assertNotSame(o1, o2)

        assertSame(o1, true.ifElse(o1, o2))
        assertSame(o2, false.ifElse(o1, o2))
        assertNull(null?.ifElse(o1, o2))
    }

    @Test
    fun testMapArray() {
        val arr = arrayOf(1, 2, 3)
        val newArr = arr.mapArray { v -> v * 2 }

        assertThat(arr, not(`is`(newArr)))
        assertThat(arr, equalTo(arrayOf(1, 2, 3)))
        assertThat(newArr, equalTo(arrayOf(2, 4, 6)))
    }
}

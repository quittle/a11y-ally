package com.quittle.a11yally

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.concurrent.thread

class KotlinUtilsTest {
    @Test
    fun compilerCheckTest() {
        val nullable: String? = null
        var nonNull: String = "value"

        // The following would cause a compiler failure
        // nonNull = nullable

        if (!nullable.isNull()) {
            nonNull = nullable
        }

        if (nullable.isNotNull()) {
            nonNull = nullable
        }

        nullable.ifNotNull { value: String ->
            nonNull = value
        }

        // Do something with the value to stop warnings
        nonNull.length
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
}

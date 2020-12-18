package com.quittle.a11yally.base

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quittle.a11yally.test.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer

@RunWith(AndroidJUnit4::class)
class AndroidUtilsTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun testClearCanvas() {
        val zero: Byte = 0

        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        assertEquals(zero, getFirstByte(bitmap))

        val canvas = Canvas(bitmap)
        assertEquals(zero, getFirstByte(bitmap))

        canvas.drawRGB(0x12, 0x34, 0x56)
        assertEquals(0x12.toByte(), getFirstByte(bitmap))

        canvas.clear()
        assertEquals(zero, getFirstByte(bitmap))
    }

    private fun getFirstByte(bitmap: Bitmap): Byte {
        val buffer = ByteBuffer.allocate(100)
        bitmap.copyPixelsToBuffer(buffer)
        buffer.rewind()
        return buffer.get(0)
    }

    @Test
    fun testGetColorCompat() {
        val colorValue = context.getColorCompat(R.color.test_color)

        assertEquals(0xff123456.toInt(), colorValue)
    }

    @Test
    fun testResolveAttributeResourceValue() {
        context.setTheme(R.style.Theme_Test)
        val attrValue = context.resolveAttributeResourceValue(R.attr.test_attribute)

        assertEquals(0xff123456.toInt(), attrValue)
    }
}

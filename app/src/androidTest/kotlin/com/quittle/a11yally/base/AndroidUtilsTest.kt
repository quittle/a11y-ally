package com.quittle.a11yally.base

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.quittle.a11yally.test.R
import com.quittle.a11yally.testContext
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer

@RunWith(AndroidJUnit4::class)
class AndroidUtilsTest {

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
        val colorValue = testContext().getColorCompat(R.color.test_color)

        assertEquals(0xff123456.toInt(), colorValue)
    }

    @Test
    fun testResolveAttributeResourceValue() {
        // Complicated setting in order to ensure the attributes are immediately available on older
        // versions of Android
        val theme = testContext().resources.newTheme()
        theme.applyStyle(R.style.Theme_Test, true)
        testContext().theme.setTo(theme)

        val attrValue = testContext().resolveAttributeResourceValue(R.attr.test_attribute)

        assertEquals(0xff123456.toInt(), attrValue)
    }
}

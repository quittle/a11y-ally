package com.quittle.a11yally.analyzer.firebase

import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.TypedValue
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.quittle.a11yally.getDisplayMetrics

/**
 * Other potential uses also include checking the text contrast on its background
 */
class FirebaseTextAnalyzer(context: Context) {
    private val displayMetrics: DisplayMetrics = context.getDisplayMetrics()

    fun doesTextBlockContainSmallText(
            textBlock: FirebaseVisionText.TextBlock, minTextSizeSp: Float): Boolean {
        val minTextSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, minTextSizeSp, displayMetrics)
        return textBlock.lines
                .mapNotNull(FirebaseVisionText.Line::getBoundingBox)
                .map(Rect::height)
                .any { it < minTextSizePx }
    }
}

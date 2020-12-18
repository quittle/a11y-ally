package com.quittle.a11yally.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.quittle.a11yally.R

open class CustomCardView : LinearLayout {
    private data class Attributes(
        val text: CharSequence,
        @DrawableRes val imageResource: Int,
        @ColorInt val imageBackgroundColor: Int
    )

    private var mTextView: TextView? = null
    private var mImageView: ImageView? = null
    private val mText: CharSequence
    @DrawableRes private val mImageResource: Int
    @ColorInt private val mImageBackgroundColor: Int

    constructor(context: Context) : super(context) {
        initializeViews(context)
        mText = ""
        mImageResource = 0
        mImageBackgroundColor = 0
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initializeViews(context)

        with(extractAttributes(context, attrs)) {
            mText = text
            mImageResource = imageResource
            mImageBackgroundColor = imageBackgroundColor
        }
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        initializeViews(context)

        with(extractAttributes(context, attrs)) {
            mText = text
            mImageResource = imageResource
            mImageBackgroundColor = imageBackgroundColor
        }
    }

    private fun extractAttributes(context: Context, attrs: AttributeSet): Attributes {
        val text: CharSequence
        @DrawableRes val imageResource: Int
        @ColorInt val imageBackgroundColor: Int
        with(context.obtainStyledAttributes(attrs, R.styleable.CustomCardView)) {
            text = getText(R.styleable.CustomCardView_text)
            imageResource = getResourceId(R.styleable.CustomCardView_image, 0)
            imageBackgroundColor = getColor(R.styleable.CustomCardView_imageBackgroundColor, 0)
            recycle()
        }
        return Attributes(text, imageResource, imageBackgroundColor)
    }

    fun setText(text: CharSequence) {
        mTextView?.text = text
    }

    fun setImageResource(@DrawableRes resId: Int) {
        mImageView?.setImageResource(resId)
    }

    fun setImageBackgroundColor(@ColorInt colorInt: Int) {
        mImageView?.setBackgroundColor(colorInt)
    }

    private fun initializeViews(context: Context) {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.custom_card_view, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        mTextView = findViewById(R.id.text)
        mImageView = findViewById(R.id.image)

        setText(mText)
        setImageResource(mImageResource)
        setImageBackgroundColor(mImageBackgroundColor)
    }
}

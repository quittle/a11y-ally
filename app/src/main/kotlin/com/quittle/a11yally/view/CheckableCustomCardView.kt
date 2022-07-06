package com.quittle.a11yally.view

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.preference.PreferenceManager
import com.quittle.a11yally.R
import com.quittle.a11yally.base.isNotNull

class CheckableCustomCardView : Checkable, CustomCardView {
    @FunctionalInterface
    fun interface OnCheckedChangeListener {
        fun onCheckedChanged(view: CheckableCustomCardView, isChecked: Boolean)
    }

    private val mListeners: MutableList<OnCheckedChangeListener> = mutableListOf()
    @DrawableRes
    private val mImageResource: Int
    @ColorInt
    private val mImageBackgroundColor: Int
    @DrawableRes
    private val mImageResourceUnchecked: Int
    @ColorInt
    private val mImageBackgroundColorUnchecked: Int
    private var mIsChecked: Boolean = false
    private val mPreferenceKey: String?

    constructor(context: Context) : super(context) {
        mImageResource = 0
        mImageBackgroundColor = 0
        mImageResourceUnchecked = 0
        mImageBackgroundColorUnchecked = 0
        mPreferenceKey = null
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        with(extractAttributes(context, attrs)) {
            mImageResource = imageResource
            mImageBackgroundColor = imageBackgroundColor
            mImageResourceUnchecked = imageResourceUnchecked
            mImageBackgroundColorUnchecked = imageBackgroundColorUnchecked
            mIsChecked = checked
            mPreferenceKey = preferenceKey
        }
    }

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        with(extractAttributes(context, attrs)) {
            mImageResource = imageResource
            mImageBackgroundColor = imageBackgroundColor
            mImageResourceUnchecked = imageResourceUnchecked
            mImageBackgroundColorUnchecked = imageBackgroundColorUnchecked
            mIsChecked = checked
            mPreferenceKey = preferenceKey
        }
    }

    private companion object {
        private data class Attributes(
            @DrawableRes val imageResource: Int,
            @ColorInt val imageBackgroundColor: Int,
            @DrawableRes val imageResourceUnchecked: Int,
            @ColorInt val imageBackgroundColorUnchecked: Int,
            val checked: Boolean,
            val preferenceKey: String?
        )

        @JvmField
        val ON_TOGGLE = OnCheckedChangeListener { view, isChecked ->
            if (isChecked) {
                view.setImageResource(view.mImageResource)
                view.setImageBackgroundColor(view.mImageBackgroundColor)
            } else {
                view.setImageResource(view.mImageResourceUnchecked)
                view.setImageBackgroundColor(view.mImageBackgroundColorUnchecked)
            }

            view.mPreferenceKey?.let { preferenceKey ->
                PreferenceManager.getDefaultSharedPreferences(view.context).edit()
                    .putBoolean(preferenceKey, isChecked)
                    ?.apply()
            }
        }

        private fun extractAttributes(context: Context, attrs: AttributeSet): Attributes {
            @DrawableRes val imageResource: Int
            @ColorInt val imageBackgroundColor: Int
            with(context.obtainStyledAttributes(attrs, R.styleable.CustomCardView)) {
                imageResource = getResourceId(R.styleable.CustomCardView_image, 0)
                imageBackgroundColor = getColor(R.styleable.CustomCardView_imageBackgroundColor, 0)
            }

            @DrawableRes val imageResourceUnchecked: Int
            @ColorInt val imageBackgroundColorUnchecked: Int
            val checked: Boolean
            val preferenceKey: String?
            with(context.obtainStyledAttributes(attrs, R.styleable.CheckableCustomCardView)) {
                imageResourceUnchecked =
                    getResourceId(R.styleable.CheckableCustomCardView_imageUnchecked, 0)
                imageBackgroundColorUnchecked =
                    getColor(
                        R.styleable.CheckableCustomCardView_imageBackgroundColorUnchecked,
                        0
                    )
                checked = getBoolean(R.styleable.CheckableCustomCardView_checked, false)
                preferenceKey = getString(R.styleable.CheckableCustomCardView_preference_key)
                recycle()
            }

            return Attributes(
                imageResource,
                imageBackgroundColor,
                imageResourceUnchecked,
                imageBackgroundColorUnchecked,
                checked,
                preferenceKey
            )
        }
    }

    init {
        setOnCheckedChangeListener(ON_TOGGLE)

        setOnClickListener {
            toggle()
        }
    }

    @DrawableRes
    fun getImageResource(): Int {
        return mImageResource
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        isChecked = if (mPreferenceKey.isNotNull()) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(mPreferenceKey, mIsChecked)
        } else {
            mIsChecked
        }
    }

    private fun setOnCheckedChangeListener(listener: OnCheckedChangeListener) {
        mListeners.add(listener)
    }

    override fun isChecked(): Boolean {
        return mIsChecked
    }

    override fun setChecked(isChecked: Boolean) {
        mIsChecked = isChecked
        mListeners.iterator().forEach {
            it.onCheckedChanged(this, isChecked)
        }
    }

    override fun toggle() {
        isChecked = !isChecked
    }
}

package com.quittle.a11yally.view

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.SwitchCompat
import com.quittle.a11yally.R
import com.quittle.a11yally.isNotNull
import com.quittle.a11yally.preferences.PreferenceProvider

/**
 * View representing controls that look something like the following.
 *
 *    +--------------------+
 *    |                    |
 *    |            |       |
 *    | Title Text | (==o) |
 *    |            |       |
 *    |                    |
 *    +--------------------+
 *
 * The intention is that the switch controls the master on/off preference and the text is a button
 * to enter more in-depth, preference controls.
 */
class ButtonSwitch : LinearLayout {
    private val mText: CharSequence?
    private val mPreference: String?
    private val mLayoutPadding: Int
    private val mDividerPadding: Int
    private val mDividerWidth: Int
    @DrawableRes private val mDividerDrawableResource: Int
    private val mSharedPreferences: SharedPreferences

    private lateinit var mTitleButton: Button
    private lateinit var mDivider: LinearLayout
    private lateinit var mSwitchCompat: SwitchCompat
    private val mSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener?

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) :
            super(context, attrs, defStyle) {
        if (attrs.isNotNull()) {
            with(context.obtainStyledAttributes(attrs, R.styleable.ButtonSwitch, defStyle, 0)) {
                mText = getText(R.styleable.ButtonSwitch_text)
                mPreference = getString(R.styleable.ButtonSwitch_switchPreference)
                mLayoutPadding = getDimensionPixelSize(R.styleable.ButtonSwitch_layoutPadding, 0)
                mDividerPadding = getDimensionPixelSize(R.styleable.ButtonSwitch_dividerPadding, 0)
                mDividerWidth = getDimensionPixelSize(R.styleable.ButtonSwitch_dividerWidth, 0)
                mDividerDrawableResource =
                        getResourceId(R.styleable.ButtonSwitch_dividerDrawable, 0)
            }
        } else {
            mText = null
            mPreference = null
            mLayoutPadding = 0
            mDividerPadding = 0
            mDividerWidth = 0
            mDividerDrawableResource = 0
        }

        mSharedPreferences = PreferenceProvider(context).sharedPreferences

        if (mPreference.isNotNull()) {
            mSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener {
                    sharedPreferences, key ->
                if (key == mPreference) {
                    mSwitchCompat.isChecked = sharedPreferences.getBoolean(mPreference, false)
                }
            }
        } else {
            mSharedPreferenceChangeListener = null
        }

        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.button_switch, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mSharedPreferences.registerOnSharedPreferenceChangeListener(
                mSharedPreferenceChangeListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(
                mSharedPreferenceChangeListener)
    }

    fun getButton(): Button {
        return mTitleButton
    }

    fun getPreference(): String? {
        return mPreference
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        mTitleButton = findViewById(R.id.button)
        mDivider = findViewById(R.id.divider)
        mSwitchCompat = findViewById(R.id.switch_compat)

        mTitleButton.apply {
            if (mText.isNotNull()) {
                text = mText
            }
        }

        mDivider.apply {
            (layoutParams as? LinearLayout.LayoutParams)
                    ?.setMargins(mDividerPadding, 0, mDividerPadding, 0)
            layoutParams.width = mDividerWidth
            if (mDividerDrawableResource != 0) {
                setBackgroundResource(mDividerDrawableResource)
            }
        }
        findViewById<View>(R.id.wrapper).apply {
            setPadding(mLayoutPadding, mLayoutPadding, mLayoutPadding, mLayoutPadding)
        }

        mSwitchCompat.apply {
            if (mPreference.isNotNull()) {
                isChecked = mSharedPreferences.getBoolean(mPreference, false)

                setOnCheckedChangeListener { _, isChecked: Boolean ->
                    mSharedPreferences.edit()
                            .putBoolean(mPreference, isChecked)
                            .apply()
                }
            }
        }
    }
}

package com.quittle.a11yally.view

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * A wrapper around [ConstraintLayout] that adds support for checkable states.
 */
class CheckableConstraintLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    ConstraintLayout(context, attrs, defStyleAttr),
    CheckedChangeRegister<CheckableConstraintLayout>,
    Checkable {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private val mCheckableHelper = CheckabilityHelper(this)

    override fun onCreateDrawableState(extraSpace: Int): IntArray? {
        return mCheckableHelper.onCreateDrawableState(
            extraSpace,
            { space -> super.onCreateDrawableState(space) },
            { baseState, additionalState -> mergeDrawableStates(baseState, additionalState) }
        )
    }

    override fun setOnCheckedChangedListener(
        listener: CheckedChangeListener<CheckableConstraintLayout>?
    ) {
        mCheckableHelper.setOnCheckedChangedListener(listener)
    }

    override fun setChecked(checked: Boolean) {
        mCheckableHelper.isChecked = checked
    }

    override fun isChecked(): Boolean {
        return mCheckableHelper.isChecked
    }

    override fun toggle() {
        mCheckableHelper.toggle()
    }
}

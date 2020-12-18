package com.quittle.a11yally.view

import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.Checkable
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.quittle.a11yally.base.ifElse

/**
 * Helper class to enable making an arbitrary View checkable. To consume
 * <ol>
 * <li> Create a subclass of the View you want to be made checkable. </li>
 * <li> Add a CheckabilityHelper member instance to the subclass, passing in `this`. </li>
 * <li> Override onCreateDrawableState and delegate to this class's implementation. </li>
 * </ol>
 */
class CheckabilityHelper<T>(private val mView: T) : Checkable
        where T : View, T : CheckedChangeRegister<T> {
    private companion object {
        val STATE_CHECKED = intArrayOf(android.R.attr.state_checkable, android.R.attr.state_checked)
        val STATE_UNCHECKED = intArrayOf(android.R.attr.state_checkable)
    }

    private var mIsChecked = false
    private var mCheckedChangeListener: CheckedChangeListener<T>? = null

    init {
        mView.isClickable = true
        mView.isFocusable = true

        mView.setOnClickListener {
            toggle()
        }

        ViewCompat.setAccessibilityDelegate(
            mView,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityEvent(host: View, event: AccessibilityEvent) {
                    super.onInitializeAccessibilityEvent(host, event)
                    event.isChecked = isChecked
                }

                override fun onInitializeAccessibilityNodeInfo(
                    host: View,
                    info: AccessibilityNodeInfoCompat
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isCheckable = true
                    info.isChecked = isChecked
                }
            }
        )
    }

    /**
     * From a View's onCreateDrawableState method, simply delegate to this method. Here is an
     * example of how to delegate:
     *
     *     return mCheckableHelper.onCreateDrawableState(
     *         extraSpace,
     *         { space -> super.onCreateDrawableState(space) },
     *         { baseState, additionalState -> mergeDrawableStates(baseState, additionalState) })
     */
    fun onCreateDrawableState(
        extraSpace: Int,
        superFn: (Int) -> IntArray?,
        mergeDrawableStates: (IntArray?, IntArray?) -> IntArray?
    ): IntArray? {
        val stateList = mIsChecked.ifElse(STATE_CHECKED, STATE_UNCHECKED)
        val state = superFn(extraSpace + stateList.size)
        return mergeDrawableStates(state, stateList)
    }

    /**
     * Call this from implementations of [CheckedChangeRegister.setOnCheckedChangedListener].
     */
    fun setOnCheckedChangedListener(listener: CheckedChangeListener<T>) {
        mCheckedChangeListener = listener
    }

    override fun setChecked(checked: Boolean) {
        if (mIsChecked != checked) {
            toggle()
        }
    }

    override fun isChecked(): Boolean {
        return mIsChecked
    }

    override fun toggle() {
        mIsChecked = !mIsChecked
        mCheckedChangeListener?.onCheckedChanged(mView, mIsChecked)
        mView.refreshDrawableState()
    }
}

/**
 * Generic register for a checked change listener.
 */
interface CheckedChangeRegister<T : View> {
    /**
     * Set's a callback for a checked change listener. Calling this twice should result in the the
     * initial callback being discarded.
     */
    fun setOnCheckedChangedListener(listener: CheckedChangeListener<T>)
}

/**
 * Called when a view's checked status changes.
 */
fun interface CheckedChangeListener<T : View> {
    /**
     * Called upon a change in state
     * @param view The view who's state was changed
     * @param isChecked True if the [view] is now checked
     */
    fun onCheckedChanged(view: T, isChecked: Boolean)
}

package com.quittle.a11yally.analyzer

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.lifecycle.LiveData
import com.quittle.a11yally.R
import com.quittle.a11yally.analyzer.linearnavigation.LinearNavigationEntry
import com.quittle.a11yally.analyzer.linearnavigation.LinearNavigationScrollOffset
import com.quittle.a11yally.analyzer.linearnavigation.LinearNavigationState
import com.quittle.a11yally.base.getDefaultDisplayContext
import com.quittle.a11yally.base.ifNotNull
import com.quittle.a11yally.base.isNull
import com.quittle.a11yally.base.orElse
import com.quittle.a11yally.lifecycle.AllTrueLiveData
import com.quittle.a11yally.preferences.PreferenceProvider

/**
 * Displays accessibility issues visibly on the screen.
 */
class LinearNavigationAccessibilityOverlay(accessibilityAnalyzer: A11yAllyAccessibilityAnalyzer) :
    AccessibilityOverlay<ViewGroup>(accessibilityAnalyzer) {
    override val mOverlayFlags: Int = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

    private var mListView: LinearLayout? = null
    private var mScrollView: ScrollView? = null
    private val mContext: Context = accessibilityAnalyzer.getDefaultDisplayContext()
    private val mAccessibilityNodeAnalyzer = AccessibilityNodeAnalyzer(mContext)
    private val mTextEntries: MutableList<String> = mutableListOf()
    private val mLayoutInflator = LayoutInflater.from(mContext)
    private val mPreferenceProvider = PreferenceProvider(mContext)
    private val mLinearNavigationLiveData: LiveData<Boolean>

    private var mLinearNavigationState: LinearNavigationState

    init {
        mPreferenceProvider.onResume()

        mLinearNavigationLiveData = AllTrueLiveData(
            mPreferenceProvider.getLinearNavigationLiveData(),
            mPreferenceProvider.getServiceEnabledLiveData()
        )
        mLinearNavigationState = LinearNavigationState()
        mLinearNavigationLiveData.observe(
            accessibilityAnalyzer,
            { enabled ->
                if (enabled) {
                    accessibilityAnalyzer.resumeListener(this)
                } else {
                    accessibilityAnalyzer.pauseListener(this)
                }
            }
        )

        if (mLinearNavigationLiveData.value!!) {
            accessibilityAnalyzer.resumeListener(this)
        } else {
            accessibilityAnalyzer.pauseListener(this)
        }
    }

    override fun onAccessibilityEventStart() {
        val x = mScrollView?.scrollX.orElse(0)
        val y = mScrollView?.scrollY.orElse(0)
        mLinearNavigationState = mLinearNavigationState.next(LinearNavigationScrollOffset(x, y))
        clear()
    }

    override fun onAccessibilityEventEnd() {
        mListView.ifNotNull { listView ->
            listView.setOnScrollChangeListener ((v, scrollX, scrollY, oldScrollX, oldScrollY) -> { })
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            mLinearNavigationState.entries.forEach { entry ->
                val textView = mLayoutInflator.inflate(
                    R.layout.linear_navigation_entry, mListView, false
                ) as TextView
                textView.text = entry.text

                textView.setOnClickListener {
                    findClickableAccessibilityNode(entry.node)
                        ?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                textView.setOnLongClickListener {
                    findLongClickableAccessibilityNode(entry.node)
                        ?.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
                    false
                }
//                textView.setOnTouchListener { _, _ ->
//                    findFocusableAccessibilityNode(entry.node)
//                        ?.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
//                    false
//                }
                listView.addView(textView, params)
            }
        }
        if (!mLinearNavigationState.haveEntriesDiverged()) {
            mScrollView?.scrollX = mLinearNavigationState.prevOffset.x
            mScrollView?.scrollY = mLinearNavigationState.prevOffset.y
        }
        // Nothing to do
    }

    @SuppressLint("SetTextI18n")
    override fun onAccessibilityNodeInfo(node: AccessibilityNodeInfo) {
        if (mAccessibilityNodeAnalyzer.isNodeLikelyFocusable(node)) {
            val text = getNodeText(node)
            mLinearNavigationState.entries.add(LinearNavigationEntry(text, node))
        }
    }

    override fun onNonWhitelistedApp() {
        clear()
    }

    override fun onResume() {
        super.onResume()
        mListView = rootView?.findViewById(R.id.navigation_items_holder)
        mScrollView = rootView?.findViewById(R.id.scroll_view)
        clear()
    }

    override fun onPause() {
        super.onPause()
        clear()
        mListView = null
        mScrollView = null
    }

    @SuppressLint("InflateParams")
    override fun buildRootView(): ViewGroup {
        return (
            mLayoutInflator.inflate(R.layout.linear_navigation_overlay, null, false)
                as ViewGroup
            ).apply {
            findViewById<View>(R.id.disable).setOnClickListener {
                mPreferenceProvider.setLinearNavigationEnabled(false)
            }
        }
    }

    private fun getNodeText(node: AccessibilityNodeInfo): String {
        val descriptionText = mAccessibilityNodeAnalyzer
            .getContentDescription(node)
            .orElse(node.text)
            .orElse("[[No Text]]")
            .toString()
        val descriptors = getDescriptors(node)
        if (descriptors.isEmpty()) {
            return descriptionText
        }
        return descriptionText + "-" + descriptors.joinToString(", ")
    }

    private fun clear() {
        mTextEntries.clear()
        mListView?.removeAllViews()
        mScrollView?.scrollTo(0, 0)
        mLinearNavigationState.entries.clear()
    }

    private fun findClickableAccessibilityNode(
        node: AccessibilityNodeInfo?
    ): AccessibilityNodeInfo? = findAccessibilityNodeByCase(node, AccessibilityNodeInfo::isClickable)

    private fun findLongClickableAccessibilityNode(
        node: AccessibilityNodeInfo?
    ): AccessibilityNodeInfo? = findAccessibilityNodeByCase(node, AccessibilityNodeInfo::isLongClickable)

    private fun findFocusableAccessibilityNode(
        node: AccessibilityNodeInfo?
    ): AccessibilityNodeInfo? = findAccessibilityNodeByCase(node, AccessibilityNodeInfo::isFocusable)

    /**
     * Recursively finds the first node that matches [case] in the hierarchy of a node and its parents
     * @param node The base node
     * @param case Called with each node in the hierarchy, returning true if it's a match.
     * @return The first clickable node, which is either [node] or a parent of it. Returns `null` if
     * there is no clickable parent.
     */
    private fun findAccessibilityNodeByCase(
        node: AccessibilityNodeInfo?,
        case: (AccessibilityNodeInfo) -> Boolean,
    ): AccessibilityNodeInfo? {
        return when {
            node.isNull() -> null
            case(node) -> node
            else -> findAccessibilityNodeByCase(node.parent, case)
        }
    }

    private fun getDescriptors(node: AccessibilityNodeInfo): List<String> {
        val ret = mutableListOf<String>()
        if (node.isCheckable) {
            ret += "Checkable [${if (node.isChecked) "Checked" else "Unchecked"}]"
        }
        if (node.isSelected) {
            ret += "Selected"
        }
        if (node.isFocused) {
            ret += "Focused"
        }
        return ret
    }
}

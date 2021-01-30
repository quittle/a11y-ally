package com.quittle.a11yally.activity.welcome

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.quittle.a11yally.base.RefreshableWeakReference
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.lessThan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.LooperMode

/** Kotlin `Function*` interfaces are not mockable due to classloader constraints */
interface MockableFunction3<P1, P2, P3, R> : Function3<P1, P2, P3, R>

@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class AppInfoRecyclerViewAdapterTest {
    private companion object {
        val DRAWABLE = RefreshableWeakReference<Drawable> { mock() }
    }

    lateinit var activity: Activity
    lateinit var rootView: ViewGroup

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().resume().get()
        rootView = activity.window.decorView.rootView as ViewGroup
    }

    @Test
    fun testGetItemCount() {
        val size = 10
        val adapter =
            AppInfoRecyclerViewAdapter(activity, List(size) { genEmptyCheckableAppInfo() })
        assertEquals(size, adapter.itemCount)
    }

    @Test
    fun testOnCreateViewHolder() {
        val adapter = AppInfoRecyclerViewAdapter(activity, listOf(genEmptyCheckableAppInfo()))
        val viewHolder = adapter.onCreateViewHolder(rootView, 0)
        assertFalse(viewHolder.wrapper.isChecked)
        assertNotNull(viewHolder.icon)
        assertEquals("", viewHolder.title.text)
        assertEquals("", viewHolder.subTitle.text)
        assertThat(viewHolder.divider.height, lessThan(10))
    }

    @Test
    fun testOnBindViewHolder_overridesAllValues() {
        val emptyAppInfo = genEmptyCheckableAppInfo()
        val fullAppInfo = CheckableAppInfo(
            AppInfo("label", "package name", 0xf1a65, DRAWABLE),
            true
        )
        val adapter = AppInfoRecyclerViewAdapter(activity, listOf(emptyAppInfo, fullAppInfo))
        val viewHolder = adapter.onCreateViewHolder(rootView, 0)
        adapter.onBindViewHolder(viewHolder, 0)
        assertFalse(viewHolder.wrapper.isChecked)
        assertEquals("", viewHolder.title.text)
        assertEquals("", viewHolder.subTitle.text)
        assertEquals(View.VISIBLE, viewHolder.divider.visibility)

        adapter.onBindViewHolder(viewHolder, 1)

        assertTrue(viewHolder.wrapper.isChecked)
        assertEquals("label", viewHolder.title.text)
        assertEquals("package name", viewHolder.subTitle.text)
        assertEquals(View.GONE, viewHolder.divider.visibility)
    }

    @Test
    fun testOnBindViewHolder_handlesDuplicateLabelAndPackageName() {
        val appInfo = CheckableAppInfo(
            AppInfo("package.name", "package.name", 0xf1a65, DRAWABLE),
            true
        )
        val adapter = AppInfoRecyclerViewAdapter(activity, listOf(appInfo))
        val viewHolder = adapter.onCreateViewHolder(rootView, 0)
        adapter.onBindViewHolder(viewHolder, 0)
        assertEquals("package.name", viewHolder.title.text)
        assertEquals("", viewHolder.subTitle.text)
    }

    @Test
    fun testOnBindViewHolder_lastDividerHidden() {
        val adapter = AppInfoRecyclerViewAdapter(activity, List(3) { genEmptyCheckableAppInfo() })
        val viewHolder = adapter.onCreateViewHolder(rootView, 0)
        adapter.onBindViewHolder(viewHolder, 0)
        assertEquals(View.VISIBLE, viewHolder.divider.visibility)
        adapter.onBindViewHolder(viewHolder, 1)
        assertEquals(View.VISIBLE, viewHolder.divider.visibility)
        adapter.onBindViewHolder(viewHolder, 2)
        assertEquals(View.GONE, viewHolder.divider.visibility)
    }

    @Test
    fun testOnBindViewHolder_checkedChangedListener() {
        val appInfos = List(2) { genEmptyCheckableAppInfo() }
        val checkedChangeListener: MockableFunction3<List<CheckableAppInfo>, Int, Boolean, Unit> =
            mock()
        val adapter = AppInfoRecyclerViewAdapter(activity, appInfos, checkedChangeListener)
        val viewHolder = adapter.onCreateViewHolder(rootView, 0)

        // Bind and click first app
        adapter.onBindViewHolder(viewHolder, 0)
        viewHolder.wrapper.performClick()

        // Verify it clicked and had expected result
        verify(checkedChangeListener).invoke(appInfos, 0, true)
        assertTrue(viewHolder.wrapper.isChecked)
        assertTrue(appInfos[0].isChecked)

        // Bind second app
        adapter.onBindViewHolder(viewHolder, 1)

        // Verify the first app is still checked and the second app isn't
        assertTrue(appInfos[0].isChecked)
        assertFalse(viewHolder.wrapper.isChecked)
        assertFalse(appInfos[1].isChecked)
        // A bug previously caused the checked change listener to be trigger when re-using the view
        // holder, resulting in app infos becoming unchecked.
        verifyNoMoreInteractions(checkedChangeListener)

        // Click second app
        viewHolder.wrapper.performClick()

        // Verify that checked the second app
        verify(checkedChangeListener).invoke(appInfos, 1, true)
        assertTrue(viewHolder.wrapper.isChecked)
        assertTrue(appInfos[1].isChecked)

        // Click it again to disable second app
        viewHolder.wrapper.performClick()
        assertFalse(viewHolder.wrapper.isChecked)
        assertFalse(appInfos[1].isChecked)

        // Switch back to first app and verify it remained checked
        adapter.onBindViewHolder(viewHolder, 0)
        assertTrue(viewHolder.wrapper.isChecked)
        assertTrue(appInfos[0].isChecked)
    }

    private fun genEmptyCheckableAppInfo(): CheckableAppInfo {
        return CheckableAppInfo(
            AppInfo("", "", 0, DRAWABLE),
            false
        )
    }
}

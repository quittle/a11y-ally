package com.quittle.a11yally.analytics

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.nhaarman.mockitokotlin2.argForWhich
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.quittle.a11yally.BuildConfig
import com.quittle.a11yally.FirebaseRule
import com.quittle.a11yally.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@SuppressLint("SetTextI18n")
class LoggersTest {
    lateinit var mockFirebaseAnalytics: FirebaseAnalytics

    @get:Rule
    val firebaseRule = FirebaseRule()

    @Before
    fun setUp() {
        mockFirebaseAnalytics = mock()
    }

    @Test
    fun testLogSelectContentEvent() {
        mockFirebaseAnalytics.logSelectContentEvent(ContentType.APP_TO_ANALYZE, "item id") {
            param("key", "value")
        }

        verify(mockFirebaseAnalytics).logEvent(
            eq("select_content"),
            argForWhich {
                this.toString() == bundleOf(
                    "content_type" to "APP_TO_ANALYZE",
                    "item_id" to "item id",
                    "key" to "value",
                ).toString()
            }
        )
    }

    @Test
    fun testLogSelectItems() {
        mockFirebaseAnalytics.logSelectItems(
            arrayOf(
                AnalyticsSelectItem(
                    itemId = "item id",
                    itemName = "item name",
                    index = 123
                )
            ),
            listId = "list id",
            listName = "list name",
            contentType = ContentType.APP_TO_ANALYZE
        ) {
            param("key", "value")
        }

        verify(mockFirebaseAnalytics).logEvent(
            eq("select_item"),
            argForWhich {
                this.toString() == bundleOf(
                    "items" to arrayOf(
                        bundleOf(
                            "item_id" to "item id",
                            "item_name" to "item name",
                            "index" to 123
                        )
                    ),
                    "content_type" to "APP_TO_ANALYZE",
                    "item_list_id" to "list id",
                    "item_list_name" to "list name",
                    "key" to "value",
                ).toString()
            }
        )
    }

    @Test
    fun testLogClick_minimalView() {
        val view = View(ApplicationProvider.getApplicationContext())

        mockFirebaseAnalytics.logClick(view)

        verify(mockFirebaseAnalytics).logEvent(
            eq("UX"),
            argForWhich {
                this.toString() == bundleOf(
                    "ux_action" to "CLICK",
                    "item_category" to "android.view.View",
                ).toString()
            }
        )
    }

    @Test
    fun testLogClick_basicView() {
        val view = View(ApplicationProvider.getApplicationContext())
        view.id = R.id.button_wrapper
        view.contentDescription = "content description"

        mockFirebaseAnalytics.logClick(view)

        verify(mockFirebaseAnalytics).logEvent(
            eq("UX"),
            argForWhich {
                this.toString() == bundleOf(
                    "ux_action" to "CLICK",
                    "item_id" to "${BuildConfig.APPLICATION_ID}:id/button_wrapper",
                    "item_name" to "content description",
                    "item_category" to "android.view.View",
                ).toString()
            }
        )
    }

    @Test
    fun testLogClick_textView() {
        val textView = TextView(ApplicationProvider.getApplicationContext())
        textView.id = R.id.button_wrapper
        textView.text = "text"
        textView.contentDescription = "content description"

        mockFirebaseAnalytics.logClick(textView)

        verify(mockFirebaseAnalytics).logEvent(
            eq("UX"),
            argForWhich {
                this.toString() == bundleOf(
                    "ux_action" to "CLICK",
                    "item_id" to "${BuildConfig.APPLICATION_ID}:id/button_wrapper",
                    "item_name" to "text",
                    "item_category" to "text",
                ).toString()
            }
        )
    }
}

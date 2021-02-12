@file:Suppress("MatchingDeclarationName")
package com.quittle.a11yally.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.ParametersBuilder
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.quittle.a11yally.base.mapArray

val firebaseAnalytics = Firebase.analytics

fun FirebaseAnalytics.logSelectContentEvent(
    contentType: ContentType,
    itemId: String,
    block: (ParametersBuilder.() -> Unit)? = null
) {
    logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
        param(FirebaseAnalytics.Param.CONTENT_TYPE, contentType.toString())
        param(FirebaseAnalytics.Param.ITEM_ID, itemId)
        block?.invoke(this)
    }
}

data class AnalyticsSelectItem(val itemId: String, val itemName: String, val index: Int)

fun FirebaseAnalytics.logSelectItems(
    items: Array<AnalyticsSelectItem>,
    listId: String = "",
    listName: String,
    contentType: ContentType,
    block: (ParametersBuilder.() -> Unit)? = null
) {
    val parcelableItems = items.mapArray { item ->
        Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, item.itemId)
            putString(FirebaseAnalytics.Param.ITEM_NAME, item.itemName)
            putInt(FirebaseAnalytics.Param.INDEX, item.index)
        }
    }
    logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
        param(FirebaseAnalytics.Param.ITEMS, parcelableItems)
        param(FirebaseAnalytics.Param.CONTENT_TYPE, contentType.toString())
        param(FirebaseAnalytics.Param.ITEM_LIST_ID, listId)
        param(FirebaseAnalytics.Param.ITEM_LIST_NAME, listName)
        block?.invoke(this)
    }
}

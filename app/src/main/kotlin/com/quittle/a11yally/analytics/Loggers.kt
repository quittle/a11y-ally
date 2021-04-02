@file:Suppress("MatchingDeclarationName")
package com.quittle.a11yally.analytics

import android.os.Bundle
import android.view.View
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.ParametersBuilder
import com.google.firebase.analytics.ktx.logEvent
import com.quittle.a11yally.analytics.internal.getViewVariant
import com.quittle.a11yally.base.mapArray

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

fun FirebaseAnalytics.logClick(
    view: View
) {
    val viewVariant = getViewVariant(view)
    val viewId = if (view.id != -1) { view.resources.getResourceName(view.id) } else { null }
    logEvent(ParamEvent.UX.toString()) {
        param(EVENT_UX_ACTION, EventUxAction.CLICK.toString())
        param(FirebaseAnalytics.Param.ITEM_ID, viewId)
        param(FirebaseAnalytics.Param.ITEM_NAME, viewVariant.text)
        param(FirebaseAnalytics.Param.ITEM_CATEGORY, viewVariant.type)
    }
}

fun FirebaseAnalytics.logPreferenceChange(
    preferenceName: String,
    preferenceValue: String,
) {
    logPreferenceChange(preferenceName, EventPreferenceChange.SET) {
        param(FirebaseAnalytics.Param.VALUE, preferenceValue)
    }
}

fun FirebaseAnalytics.logPreferenceChange(
    preferenceName: String,
    preferenceValue: Number,
) {
    logPreferenceChange(preferenceName, EventPreferenceChange.SET) {
        param(FirebaseAnalytics.Param.VALUE, preferenceValue.toLong())
    }
}

fun FirebaseAnalytics.logPreferenceChange(
    preferenceName: String,
    preferenceValue: Boolean,
) {
    logPreferenceChange(preferenceName, EventPreferenceChange.SET) {
        param(FirebaseAnalytics.Param.VALUE, preferenceValue.toString())
    }
}

fun FirebaseAnalytics.logPreferenceRemoval(
    preferenceName: String,
) {
    logPreferenceChange(preferenceName, EventPreferenceChange.REMOVE) {}
}

private fun FirebaseAnalytics.logPreferenceChange(
    preferenceName: String,
    change: EventPreferenceChange,
    setValue: (ParametersBuilder.() -> Unit)
) {
    logEvent(ParamEvent.PREFERENCE.toString()) {
        param(EVENT_PREFERENCE_CHANGE, change.toString())
        param(FirebaseAnalytics.Param.ITEM_ID, preferenceName)
        setValue(this)
    }
}

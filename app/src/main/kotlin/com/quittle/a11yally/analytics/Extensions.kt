package com.quittle.a11yally.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.ParametersBuilder
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

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

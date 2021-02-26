@file:Suppress("MatchingDeclarationName")
package com.quittle.a11yally.analytics

import com.google.firebase.analytics.ktx.ParametersBuilder
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

val firebaseAnalytics = Firebase.analytics

fun ParametersBuilder.param(key: String, value: CharSequence?) {
    if (value !== null) {
        this.param(key, value.toString())
    }
}

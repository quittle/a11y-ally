package com.quittle.a11yally.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * A singleton instance of the Crashlytics client
 */
val crashlytics = FirebaseCrashlytics.getInstance()

/**
 * Helper function for recording exceptions that may not be obvious to report as a Throwable
 */
fun FirebaseCrashlytics.recordException(exception: String, throwable: Throwable? = null) {
    recordException(Throwable(exception, throwable))
}

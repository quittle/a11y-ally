package com.quittle.a11yally.preferences

import android.content.Context
import android.content.SharedPreferences

internal class PreferenceProviderBooleanMember(
    context: Context,
    prefKeyId: Int,
    defaultValue: Boolean = false
) :
    PreferenceProviderMember<Boolean>(
        context, prefKeyId, defaultValue,
        SharedPreferences::getBoolean, SharedPreferences.Editor::putBoolean
    )
